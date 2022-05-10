package io.neow3j.compiler;

import io.neow3j.devpack.Notification;
import io.neow3j.devpack.Storage;
import io.neow3j.devpack.StorageContext;
import io.neow3j.devpack.annotations.ContractHash;
import io.neow3j.devpack.annotations.DisplayName;
import io.neow3j.devpack.annotations.Instruction;
import io.neow3j.devpack.annotations.OnVerification;
import io.neow3j.devpack.annotations.Safe;
import io.neow3j.devpack.contracts.ContractInterface;
import io.neow3j.devpack.contracts.FungibleToken;
import io.neow3j.devpack.events.Event1Arg;
import io.neow3j.script.OpCode;
import org.junit.Test;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static io.neow3j.compiler.Compiler.CLASS_VERSION_SUPPORTED;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.Assert.assertThrows;

public class CompilerExceptionsTest {

    @Test
    public void throwExceptionIfNonDefaultExceptionInstanceIsUsed() {
        CompilerException thrown = assertThrows(CompilerException.class,
                () -> new Compiler().compile(UnsupportedException.class.getName()));
        assertThat(thrown.getMessage(), stringContainsInOrder(asList(
                IllegalArgumentException.class.getCanonicalName(),
                Exception.class.getCanonicalName())));
    }

    @Test
    public void throwExceptionIfNonDefaultExceptionIsUsedInCatchClause() {
        CompilerException thrown = assertThrows(CompilerException.class,
                () -> new Compiler().compile(UnsupportedExceptionInCatchClause.class.getName()));
        assertThat(thrown.getMessage(), stringContainsInOrder(asList("catch",
                RuntimeException.class.getCanonicalName(),
                Exception.class.getCanonicalName())));
    }

    @Test
    public void throwExceptionIfExceptionWithMoreThanOneArgumentIsUsed() {
        CompilerException thrown = assertThrows(CompilerException.class,
                () -> new Compiler().compile(UnsupportedNumberOfExceptionArguments.class.getName()));
        assertThat(thrown.getMessage(), containsString("You provided 2 arguments."));
    }

    @Test
    public void throwExceptionIfExceptionWithANonStringArgumentIsUsed() {
        CompilerException thrown = assertThrows(CompilerException.class,
                () -> new Compiler().compile(UnsupportedExceptionArgument.class.getName()));
        assertThat(thrown.getMessage(), containsString("You provided a non-string argument."));
    }

    @Test
    public void throwExceptionIfTwoEventsAreGivenTheSameName() {
        CompilerException thrown = assertThrows(CompilerException.class,
                () -> new Compiler().compile(DuplicateUseOfEventDisplayName.class.getName()));
        assertThat(thrown.getMessage(), stringContainsInOrder(asList("Two events", "transfer")));
    }

    @Test
    public void throwExceptionIfContractInterfaceClassHasInvalidScriptHash() {
        CompilerException thrown = assertThrows(CompilerException.class,
                () -> new Compiler().compile(InvalidScriptHashContractInterfaceContract.class.getName()));
        assertThat(thrown.getMessage(), stringContainsInOrder(asList("Script hash", "8", "CustomContractInterface")));
    }

    @Test
    public void testGetLastInstructionWithNoInstructionsPresent() throws IOException {
        ClassNode asmClass = AsmHelper.getAsmClass(
                InstructionAnnotationWithWrongSizeOperandContract.class.getName(),
                CompilerTest.class.getClassLoader());

        MethodNode method = asmClass.methods.get(1);
        NeoMethod neoMethod = new NeoMethod(method, asmClass);
        CompilerException thrown = assertThrows(CompilerException.class, neoMethod::getLastInstruction);
        assertThat(thrown.getMessage(), is("Could not find any instruction in this NeoMethod."));
    }

    @Test
    public void testAddInstructionsFromAnnotationWithWrongSizeOperand() throws IOException {
        ClassNode asmClass = AsmHelper.getAsmClass(
                InstructionAnnotationWithWrongSizeOperandContract.class.getName(),
                CompilerTest.class.getClassLoader());

        MethodNode method = asmClass.methods.get(1);
        NeoMethod neoMethod = new NeoMethod(method, asmClass);

        CompilerException thrown = assertThrows(CompilerException.class,
                () -> Compiler.processInstructionAnnotations(method, neoMethod));
        assertThat(thrown.getMessage(), stringContainsInOrder(asList("223344", "3", OpCode.PUSHINT16.name(), "2")));
    }

    @Test
    public void testAddInstructionsFromAnnotationWithWrongSizeOperandPrefix() throws IOException {
        ClassNode asmClass = AsmHelper.getAsmClass(
                InstructionAnnotationWithWrongSizeOperandPrefixContract.class.getName(),
                CompilerTest.class.getClassLoader());

        MethodNode method = asmClass.methods.get(1);
        NeoMethod neoMethod = new NeoMethod(method, asmClass);

        CompilerException thrown = assertThrows(CompilerException.class,
                () -> Compiler.processInstructionAnnotations(method, neoMethod));
        assertThat(thrown.getMessage(), stringContainsInOrder(
                asList(OpCode.PUSHDATA1.name(), "needs an operand prefix of size", "1", "2")));
    }

    @Test
    public void testAddInstructionsFromAnnotationWithWrongSizeOperandAccordingToCorrectPrefix() throws IOException {
        ClassNode asmClass = AsmHelper.getAsmClass(
                InstructionAnnotationWithWrongSizeOperandAccordingToCorrectPrefixContract.class.getName(),
                CompilerTest.class.getClassLoader());

        MethodNode method = asmClass.methods.get(1);
        NeoMethod neoMethod = new NeoMethod(method, asmClass);

        CompilerException thrown = assertThrows(CompilerException.class,
                () -> Compiler.processInstructionAnnotations(method, neoMethod));
        assertThat(thrown.getMessage(), stringContainsInOrder(asList("Operand prefix", "1", "2")));
    }

    @Test
    public void testAddInstructionsFromAnnotationThatDoesntTakeAnOperand() throws IOException {
        ClassNode asmClass = AsmHelper.getAsmClass(
                InstructionAnnotationWithOpcodeThatDoesntTakeAnOperand.class.getName(),
                CompilerTest.class.getClassLoader());

        MethodNode method = asmClass.methods.get(1);
        NeoMethod neoMethod = new NeoMethod(method, asmClass);

        CompilerException thrown =
                assertThrows(CompilerException.class, () -> Compiler.processInstructionAnnotations(method, neoMethod));
        assertThat(thrown.getMessage(), stringContainsInOrder(
                asList("1122", OpCode.ASSERT.name(), "doesn't take any operands.")));
    }

    @Test
    public void testNonPublicMethodsMarkedWithSafeAnnotation() {
        CompilerException thrown = assertThrows(CompilerException.class,
                () -> new Compiler().compile(PrivateMethodMarkedAsSafe.class.getName()));
        assertThat(thrown.getMessage(),
                stringContainsInOrder(asList(this.getClass().getSimpleName() + ".java", "privateMethod", "safe")));

        thrown = assertThrows(CompilerException.class,
                () -> new Compiler().compile(ProtectedMethodMarkedAsSafe.class.getName()));
        assertThat(thrown.getMessage(),
                stringContainsInOrder(asList(this.getClass().getSimpleName() + ".java", "protectedMethod", "safe")));

        thrown = assertThrows(CompilerException.class,
                () -> new Compiler().compile(PackagePrivateMethodMarkedAsSafe.class.getName()));
        assertThat(thrown.getMessage(), stringContainsInOrder(
                asList(this.getClass().getSimpleName() + ".java", "packagePrivateMethod", "safe")));
    }

    @Test
    public void failIfLocalVariablesAreUsedInStaticConstructor() {
        CompilerException thrown = assertThrows(CompilerException.class,
                () -> new Compiler().compile(LocalVariableInStaticConstructorContract.class.getName()));
        assertThat(thrown.getMessage(), stringContainsInOrder(asList(CompilerExceptionsTest.class.getSimpleName(),
                "Local variables are not supported in the static constructor")));
    }

    @Test
    public void failIfInstanceOfIsUsedOnUnsupportedType() {
        CompilerException thrown = assertThrows(CompilerException.class,
                () -> new Compiler().compile(InstanceOfContract.class.getName()));
        assertThat(thrown.getMessage(), stringContainsInOrder(
                asList(Notification.class.getName(), "is not supported for the instanceof operation.")));
    }

    @Test
    public void failCallingAContractInterfaceWithoutContractHashAnnotation() {
        CompilerException thrown = assertThrows(CompilerException.class,
                () -> new Compiler().compile(ContractInterfaceWithoutHash.class.getName()));
        assertThat(thrown.getMessage(), stringContainsInOrder(
                asList("Contract interface", FungibleToken.class.getSimpleName(),
                        "needs to be annotated with the 'ContractHash' annotation to be usable.")));
    }

    @Test
    public void failCallingAContractInterfaceWithoutContractHashAnnotationAndMultipleInheritance() {
        CompilerException thrown = assertThrows(CompilerException.class,
                () -> new Compiler().compile(ContractInterfaceWithoutHashAndMultipleInheritance.class.getName()));
        assertThat(thrown.getMessage(), stringContainsInOrder(
                asList("Contract interface", CustomFungibleToken.class.getSimpleName(),
                        "needs to be annotated with the 'ContractHash' annotation to be usable.")));
    }

    @Test
    public void failUsingConstructorOnAnEvent() {
        CompilerException thrown = assertThrows(CompilerException.class,
                () -> new Compiler().compile(EventConstructorMisuse.class.getName()));
        assertThat(thrown.getMessage(), containsString("Events must not be initialized by calling their constructor."));
    }

    @Test
    public void throwOnTokenContractInterfaceMissingHashAnnotation() {
        CompilerException thrown = assertThrows(CompilerException.class,
                () -> new Compiler().compile(TokenContractMissingHashAnnotation.class.getName()));
        assertThat(thrown.getMessage(), stringContainsInOrder(
                asList(TokenContractWithoutHashAnnotation.class.getSimpleName(), ContractHash.class.getSimpleName())));
    }

    @Test
    public void throwOnContractInterfaceMissingHashAnnotation() {
        CompilerException thrown = assertThrows(CompilerException.class,
                () -> new Compiler().compile(ContractMissingHashAnnotation.class.getName()));
        assertThat(thrown.getMessage(), stringContainsInOrder(
                asList(ContractWithoutHashAnnotation.class.getSimpleName(), ContractHash.class.getSimpleName())));
    }

    @Test
    public void throwOnContractMissingContractInterface() {
        CompilerException thrown = assertThrows(CompilerException.class,
                () -> new Compiler().compile(ContractMissingContractInterface.class.getName()));
        assertThat(thrown.getMessage(), stringContainsInOrder(
                asList(ContractWithoutContractInterface.class.getSimpleName(), ContractHash.class.getSimpleName(),
                        ContractInterface.class.getSimpleName())));
    }

    @Test
    public void throwOnWrongClassCompatibility() {
        ClassNode c = new ClassNode();
        c.name = ContractWithWrongClassCompatibility.class.getSimpleName();
        c.version = 51;

        CompilerException thrown = assertThrows(CompilerException.class, () -> new Compiler().compile(c));
        assertThat(thrown.getMessage(), stringContainsInOrder(
                asList(ContractWithWrongClassCompatibility.class.getSimpleName(), "51",
                        Integer.toString(CLASS_VERSION_SUPPORTED))));
    }

    @Test
    public void throwOnStaticVariableInNonContractClass() {
        CompilerException thrown = assertThrows(CompilerException.class,
                () -> new Compiler().compile(ContractClassWithReferenceToStaticVariable.class.getName()));
        assertThat(thrown.getMessage(),
                containsString("Static variables are not allowed outside the main contract class if "));
    }

    @Test
    public void throwOnEventDeclarationInNonContractClass() {
        CompilerException thrown = assertThrows(CompilerException.class,
                () -> new Compiler().compile(ContractClassWithReferenceToEventInOtherClass.class.getName()));
        assertThat(thrown.getMessage(), containsString("Couldn't find triggered event in list of events."));
    }

    @Test
    public void throwOnEventFiredInVerifyMethod() {
        CompilerException thrown = assertThrows(CompilerException.class,
                () -> new Compiler().compile(VerificationWithEvent.class.getName()));
        assertThat(thrown.getMessage(), containsString("The verify method is not allowed to fire any event."));
    }

    @Test
    public void multiDimensionalArraySize() {
        CompilerException thrown = assertThrows(CompilerException.class,
                () -> new Compiler().compile(MultiDimensionalArraySize.class.getName()));
        assertThat(thrown.getMessage(),
                containsString("Only the first dimension of a multi-dimensional array declaration can be defined,"));
    }

    static class UnsupportedInheritanceInConstructor {
        public static void method() {
            List<String> l = new ArrayList<>();
        }
    }

    static class UnsupportedException {
        public static boolean method() {
            throw new IllegalArgumentException("Not allowed.");
        }
    }

    static class UnsupportedExceptionInCatchClause {
        public static boolean method(int i) {
            try {
                if (i == 0) {
                    throw new Exception("hello");
                }
            } catch (RuntimeException e) {
                return true;
            } catch (Exception e) {
                return false;
            }
            return true;
        }
    }

    static class UnsupportedNumberOfExceptionArguments {
        public static boolean method() throws Exception {
            throw new Exception("Not allowed.", new Exception());
        }
    }

    static class UnsupportedExceptionArgument {
        public static boolean method() throws Exception {
            throw new Exception(new Exception());
        }
    }

    static class DuplicateUseOfEventDisplayName {
        @DisplayName("transfer")
        private static Event1Arg<String> event1;

        @DisplayName("transfer")
        private static Event1Arg<String> event2;

        public static boolean method() throws Exception {
            event1.fire("notification");
            event2.fire("notification");
            return true;
        }
    }

    static class InvalidScriptHashContractInterfaceContract {
        public static void getScriptHashOfContractInterface() {
            CustomContractInterface.getHash();
        }

        @ContractHash("8")
        static class CustomContractInterface extends ContractInterface {
        }
    }

    static class InstructionAnnotationWithWrongSizeOperandContract {
        @Instruction(opcode = OpCode.PUSHINT16, operand = {0x22, 0x33, 0x44})
        public static native void annotatedMethod();
    }

    static class InstructionAnnotationWithWrongSizeOperandPrefixContract {
        @Instruction(opcode = OpCode.PUSHDATA1, operandPrefix = {0x00, 0x03}, operand = {0x11, 0x22, 0x33})
        public static native void annotatedMethod();
    }

    static class InstructionAnnotationWithWrongSizeOperandAccordingToCorrectPrefixContract {
        @Instruction(opcode = OpCode.PUSHDATA1, operandPrefix = {0x01}, operand = {0x11, 0x22})
        public static native void annotatedMethod();
    }

    static class InstructionAnnotationWithOpcodeThatDoesntTakeAnOperand {
        @Instruction(opcode = OpCode.ASSERT, operand = {0x11, 0x22})
        public static native void annotatedMethod();
    }

    static class PrivateMethodMarkedAsSafe {
        @Safe
        private static void privateMethod() {
        }
    }

    static class ProtectedMethodMarkedAsSafe {
        @Safe
        private static void protectedMethod() {
        }
    }

    static class PackagePrivateMethodMarkedAsSafe {
        @Safe
        private static void packagePrivateMethod() {
        }
    }

    static class LocalVariableInStaticConstructorContract {
        private static int number;

        static {
            int i = 1;
            number = i * 2;
        }

        public static int method() {
            return number;
        }
    }

    static class MethodOfClassMissingDebugInformation {
        public static int stringCompareTo(String s1, String s2) {
            return s1.compareTo(s2);
        }
    }

    static class InstanceOfContract {
        public static boolean method(Object obj) {
            return obj instanceof Notification;
        }
    }

    static class ContractInterfaceWithoutHash {
        public static String method() {
            return FungibleToken.symbol();
        }
    }

    static class ContractInterfaceWithoutHashAndMultipleInheritance {
        public static String method() {
            return CustomFungibleToken.symbol();
        }
    }

    static class CustomFungibleToken extends FungibleToken {
    }

    static class EventConstructorMisuse {
        static Event1Arg<String> event = new Event1Arg<>();

        public static void method() {
            String s;
            event.fire("test");
        }
    }

    static class TokenContractMissingHashAnnotation {
        public static String method() {
            return TokenContractWithoutHashAnnotation.symbol();
        }
    }

    static class TokenContractWithoutHashAnnotation extends FungibleToken {
    }

    static class ContractMissingHashAnnotation {
        public static String method() {
            return ContractWithoutHashAnnotation.symbol();
        }
    }

    static class ContractWithoutHashAnnotation extends ContractInterface {
        public static native String symbol();
    }

    static class ContractMissingContractInterface {
        public static String method() {
            return ContractWithoutContractInterface.symbol();
        }
    }

    @ContractHash("ef4073a0f2b305a38ec4050e4d3d28bc40ea63f5") // some hash
    static class ContractWithoutContractInterface {
        public static native String symbol();
    }

    static class ContractWithWrongClassCompatibility {
    }

    static class ContractClassWithReferenceToStaticVariable {
        public static void method() {
            StorageContext storageContext = NonContractClassWithStaticVariable.ctx.asReadOnly();
        }
    }

    static class NonContractClassWithStaticVariable {
        public static final StorageContext ctx = Storage.getStorageContext();
    }

    static class ContractClassWithReferenceToEventInOtherClass {
        public static void method() {
            NonContractClassWithEventDeclaration.event.fire("Hello, world!");
        }
    }

    static class NonContractClassWithEventDeclaration {
        public static Event1Arg<String> event;
    }

    static class VerificationWithEvent {
        static Event1Arg<String> e;

        @OnVerification
        public static boolean verif() {
            e.fire("neowww");
            return true;
        }
    }

    static class MultiDimensionalArraySize {
        public static String[][] method() {
            return new String[10][4];
        }
    }

}
