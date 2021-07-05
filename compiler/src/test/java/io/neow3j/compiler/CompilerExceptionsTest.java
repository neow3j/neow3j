package io.neow3j.compiler;

import io.neow3j.devpack.contracts.FungibleToken;
import io.neow3j.devpack.events.Event5Args;
import io.neow3j.script.OpCode;
import io.neow3j.devpack.contracts.ContractInterface;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.annotations.ContractHash;
import io.neow3j.devpack.annotations.DisplayName;
import io.neow3j.devpack.annotations.Instruction;
import io.neow3j.devpack.annotations.Safe;
import io.neow3j.devpack.events.Event1Arg;
import org.hamcrest.core.StringContains;
import org.hamcrest.text.StringContainsInOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;

public class CompilerExceptionsTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void throwExceptionIfNonDefaultExceptionInstanceIsUsed() throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage(new StringContainsInOrder(asList(
                IllegalArgumentException.class.getCanonicalName(),
                Exception.class.getCanonicalName())));
        new Compiler().compile(UnsupportedException.class.getName());
    }

    @Test
    public void throwExceptionIfNonDefaultExceptionIsUsedInCatchClause() throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage(new StringContainsInOrder(asList("catch",
                RuntimeException.class.getCanonicalName(),
                Exception.class.getCanonicalName())));
        new Compiler().compile(UnsupportedExceptionInCatchClause.class.getName());
    }

    @Test
    public void throwExceptionIfExceptionWithMoreThanOneArgumentIsUsed() throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage(new StringContains("You provided 2 arguments."));
        new Compiler().compile(UnsupportedNumberOfExceptionArguments.class.getName());
    }

    @Test
    public void throwExceptionIfExceptionWithANonStringArgumentIsUsed() throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage(new StringContains("You provided a non-string argument."));
        new Compiler().compile(UnsupportedExceptionArgument.class.getName());
    }

    @Test
    public void throwExceptionIfTwoEventsAreGivenTheSameName() throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage(new StringContainsInOrder(asList("Two events", "transfer")));
        new Compiler().compile(DuplicateUseOfEventDisplayName.class.getName());
    }

    @Test
    public void throwExceptionIfContractInterfaceClassHasInvalidScriptHash() throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage(new StringContainsInOrder(asList("Script hash", "8",
                "CustomContractInterface")));
        new Compiler().compile(InvalidScriptHashContractInterfaceContract.class.getName());
    }

    @Test
    public void testAddInstructionsFromAnnotationWithWrongSizeOperand() throws IOException {
        ClassNode asmClass = AsmHelper.getAsmClass(
                InstructionAnnotationWithWrongSizeOperandContract.class.getName(),
                CompilerTest.class.getClassLoader());

        MethodNode method = asmClass.methods.get(1);
        NeoMethod neoMethod = new NeoMethod(method, asmClass);

        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage(new StringContainsInOrder(Arrays.asList(
                "223344", "3", OpCode.PUSHINT16.name(), "2")));
        Compiler.addInstructionsFromAnnotation(method, neoMethod);
    }

    @Test
    public void testAddInstructionsFromAnnotationWithWrongSizeOperandPrefix() throws IOException {
        ClassNode asmClass = AsmHelper.getAsmClass(
                InstructionAnnotationWithWrongSizeOperandPrefixContract.class.getName(),
                CompilerTest.class.getClassLoader());

        MethodNode method = asmClass.methods.get(1);
        NeoMethod neoMethod = new NeoMethod(method, asmClass);

        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage(new StringContainsInOrder(Arrays.asList(
                OpCode.PUSHDATA1.name(), "needs an operand prefix of size", "1", "2")));
        Compiler.addInstructionsFromAnnotation(method, neoMethod);
    }

    @Test
    public void testAddInstructionsFromAnnotationWithWrongSizeOperandAccordingToCorrectPrefix()
            throws IOException {

        ClassNode asmClass = AsmHelper.getAsmClass(
                InstructionAnnotationWithWrongSizeOperandAccordingToCorrectPrefixContract.class
                        .getName(), CompilerTest.class.getClassLoader());

        MethodNode method = asmClass.methods.get(1);
        NeoMethod neoMethod = new NeoMethod(method, asmClass);

        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage(new StringContainsInOrder(Arrays.asList(
                "Operand prefix", "1", "2")));
        Compiler.addInstructionsFromAnnotation(method, neoMethod);
    }

    @Test
    public void testAddInstructionsFromAnnotationThatDoesntTakeAnOperand() throws IOException {
        ClassNode asmClass = AsmHelper.getAsmClass(
                InstructionAnnotationWithOpcodeThatDoesntTakeAnOperand.class
                        .getName(), CompilerTest.class.getClassLoader());

        MethodNode method = asmClass.methods.get(1);
        NeoMethod neoMethod = new NeoMethod(method, asmClass);

        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage(new StringContainsInOrder(Arrays.asList(
                "1122", OpCode.ASSERT.name(), "doesn't take any operands.")));
        Compiler.addInstructionsFromAnnotation(method, neoMethod);
    }

    @Test
    public void testNonPublicMethodsMarkedWithSafeAnnotation() throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage(new StringContainsInOrder(asList(
                this.getClass().getSimpleName() + ".java", // the file name
                "privateMethod", "safe")));
        new Compiler().compile(PrivateMethodMarkedAsSafe.class.getName());

        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage(new StringContainsInOrder(asList(
                this.getClass().getSimpleName() + ".java", // the file name
                "protectedMethod", "safe")));
        new Compiler().compile(ProtectedMethodMarkedAsSafe.class.getName());

        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage(new StringContainsInOrder(asList(
                this.getClass().getSimpleName() + ".java", // the file name
                "packagePrivateMethod", "safe")));
    }

    @Test
    public void failIfLocalVariablesAreUsedInStaticConstructor() throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage(new StringContainsInOrder(asList(
                CompilerExceptionsTest.class.getSimpleName(),
                "Local variables are not supported in the static constructor")));
        new Compiler().compile(LocalVariableInStaticConstructorContract.class.getName());
    }

    @Test
    public void failIfInstanceOfIsUsedOnUnsupportedType() throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage(new StringContainsInOrder(asList(
                Hash160.class.getName(), "is not supported for the instanceof operation.")));
        new Compiler().compile(InstanceOfContract.class.getName());
    }

    @Test
    public void failCallingAContractInterfaceWithoutContractHashAnnotation() throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage(new StringContainsInOrder(asList("Contract interface",
                FungibleToken.class.getSimpleName(),
                "needs to be annotated with the 'ContractHash' annotation to be usable.")));
        new Compiler().compile(ContractInterfaceWithoutHash.class.getName());
    }

    @Test
    public void failCallingAContractInterfaceWithoutContractHashAnnotationAndMultipleInheritance()
            throws IOException {

        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage(new StringContainsInOrder(asList("Contract interface",
                CustomFungibleToken.class.getSimpleName(),
                "needs to be annotated with the 'ContractHash' annotation to be usable.")));
        new Compiler().compile(ContractInterfaceWithoutHashAndMultipleInheritance.class.getName());
    }

    @Test
    public void failUsingConstructorOnAnEvent() throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage(new StringContains("Events must not be initialized by " +
                "calling their constructor."));
        new Compiler().compile(EventConstructorMisuse.class.getName());
  
    public void throwOnTokenContractInterfaceMissingHashAnnotation() throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage(new StringContainsInOrder(asList(
                TokenContractWithoutHashAnnotation.class.getSimpleName(),
                ContractHash.class.getSimpleName())));
        new Compiler().compile(TokenContractMissingHashAnnotation.class.getName());
    }

    @Test
    public void throwOnContractInterfaceMissingHashAnnotation() throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage(new StringContainsInOrder(asList(
                ContractWithoutHashAnnotation.class.getSimpleName(),
                ContractHash.class.getSimpleName())));
        new Compiler().compile(ContractMissingHashAnnotation.class.getName());
    }

    @Test
    public void throwOnContractMissingContractInterface() throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage(new StringContainsInOrder(asList(
                ContractWithoutContractInterface.class.getSimpleName(),
                ContractHash.class.getSimpleName(), ContractInterface.class.getSimpleName())));
        new Compiler().compile(ContractMissingContractInterface.class.getName());
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

        @Instruction(opcode = OpCode.PUSHDATA1, operandPrefix = {0x00, 0x03}, operand = {0x11, 0x22,
                0x33})
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
            return obj instanceof Hash160;
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

}

