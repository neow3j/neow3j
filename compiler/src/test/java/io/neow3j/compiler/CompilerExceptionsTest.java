package io.neow3j.compiler;

import static java.util.Arrays.asList;

import io.neow3j.constants.OpCode;
import io.neow3j.devpack.ContractInterface;
import io.neow3j.devpack.annotations.ContractHash;
import io.neow3j.devpack.annotations.DisplayName;
import io.neow3j.devpack.annotations.Instruction;
import io.neow3j.devpack.events.Event1Arg;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.hamcrest.core.StringContains;
import org.hamcrest.text.StringContainsInOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class CompilerExceptionsTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void failOnInstantiatingInheritingClass() throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage(new StringContainsInOrder(asList(
                "Found call to super constructor of", ArrayList.class.getCanonicalName())));
        new Compiler().compileClass(UnsupportedInheritanceInConstructor.class.getName());
    }

    @Test
    public void throwExceptionIfNonDefaultExceptionInstanceIsUsed() throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage(new StringContainsInOrder(asList(
                IllegalArgumentException.class.getCanonicalName(),
                Exception.class.getCanonicalName())));
        new Compiler().compileClass(UnsupportedException.class.getName());
    }

    @Test
    public void throwExceptionIfNonDefaultExceptionIsUsedInCatchClause() throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage(new StringContainsInOrder(asList("catch",
                RuntimeException.class.getCanonicalName(),
                Exception.class.getCanonicalName())));
        new Compiler().compileClass(UnsupportedExceptionInCatchClause.class.getName());
    }

    @Test
    public void throwExceptionIfExceptionWithMoreThanOneArgumentIsUsed() throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage(new StringContains("You provided 2 arguments."));
        new Compiler().compileClass(UnsupportedNumberOfExceptionArguments.class.getName());
    }

    @Test
    public void throwExceptionIfExceptionWithANonStringArgumentIsUsed() throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage(new StringContains("You provided a non-string argument."));
        new Compiler().compileClass(UnsupportedExceptionArgument.class.getName());
    }

    @Test
    public void throwExceptionIfTwoEventsAreGivenTheSameName() throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage(new StringContainsInOrder(asList("Two events", "transfer")));
        new Compiler().compileClass(DuplicateUseOfEventDisplayName.class.getName());
    }

    @Test
    public void throwExceptionIfContractInterfaceClassHasInvalidScriptHash() throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage(new StringContainsInOrder(asList("Script hash", "8",
                "ContractHash", "CustomContractInterface")));
        new Compiler().compileClass(InvalidScriptHashContractInterfaceContract.class.getName());
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
                OpCode.PUSHDATA1.name(),"needs an operand prefix of size", "1", "2")));
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
            event1.notify("notification");
            event2.notify("notification");
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

}

