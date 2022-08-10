package io.neow3j.compiler;

import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.ECPoint;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.Hash256;
import io.neow3j.devpack.Iterator;
import io.neow3j.devpack.List;
import io.neow3j.devpack.Map;
import io.neow3j.devpack.Transaction;
import io.neow3j.devpack.annotations.ContractSourceCode;
import io.neow3j.devpack.annotations.Instruction;
import io.neow3j.script.OpCode;
import io.neow3j.types.ContractParameterType;
import io.neow3j.types.StackItemType;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class CompilerTest {

    @Test
    public void mapTypeToParameterTypeShouldReturnTheCorrectTypes() {
        // Integers
        assertClassIsMappedToType(byte.class, ContractParameterType.INTEGER);
        assertClassIsMappedToType(Byte.class, ContractParameterType.INTEGER);
        assertClassIsMappedToType(char.class, ContractParameterType.INTEGER);
        assertClassIsMappedToType(Character.class, ContractParameterType.INTEGER);
        assertClassIsMappedToType(short.class, ContractParameterType.INTEGER);
        assertClassIsMappedToType(Short.class, ContractParameterType.INTEGER);
        assertClassIsMappedToType(int.class, ContractParameterType.INTEGER);
        assertClassIsMappedToType(Integer.class, ContractParameterType.INTEGER);
        assertClassIsMappedToType(long.class, ContractParameterType.INTEGER);
        assertClassIsMappedToType(Long.class, ContractParameterType.INTEGER);

        // Bools
        assertClassIsMappedToType(boolean.class, ContractParameterType.BOOLEAN);
        assertClassIsMappedToType(Boolean.class, ContractParameterType.BOOLEAN);

        // Strings
        assertClassIsMappedToType(String.class, ContractParameterType.STRING);

        // Void
        assertClassIsMappedToType(void.class, ContractParameterType.VOID);
        assertClassIsMappedToType(Void.class, ContractParameterType.VOID);

        // Byte arrays
        assertClassIsMappedToType(byte[].class, ContractParameterType.BYTE_ARRAY);
        assertClassIsMappedToType(Byte[].class, ContractParameterType.BYTE_ARRAY);
        assertClassIsMappedToType(ByteString.class, ContractParameterType.BYTE_ARRAY);

        // Arrays
        assertClassIsMappedToType(String[].class, ContractParameterType.ARRAY);
        assertClassIsMappedToType(int[].class, ContractParameterType.ARRAY);
        assertClassIsMappedToType(Integer[].class, ContractParameterType.ARRAY);
        assertClassIsMappedToType(boolean[].class, ContractParameterType.ARRAY);
        assertClassIsMappedToType(byte[][].class, ContractParameterType.ARRAY);
        assertClassIsMappedToType(List.class, ContractParameterType.ARRAY);
        assertClassIsMappedToType(Iterator.Struct.class, ContractParameterType.ARRAY);

        // Public Key
        assertClassIsMappedToType(ECPoint.class, ContractParameterType.PUBLIC_KEY);

        // Map
        assertClassIsMappedToType(Map.class, ContractParameterType.MAP);

        // Hash
        assertClassIsMappedToType(Hash160.class, ContractParameterType.HASH160);
        assertClassIsMappedToType(Hash256.class, ContractParameterType.HASH256);

        // Others
        assertClassIsMappedToType(Iterator.class, ContractParameterType.INTEROP_INTERFACE);
        assertClassIsMappedToType(Transaction.class, ContractParameterType.ANY);
        assertClassIsMappedToType(Object.class, ContractParameterType.ANY);
        assertClassIsMappedToType(CompilerTest.class, ContractParameterType.ANY);
    }

    @Test
    public void mapTypeToStackItemTypeShouldReturnTheCorrectTypes() {
        // Integers
        assertClassIsMappedToType(byte.class, StackItemType.INTEGER);
        assertClassIsMappedToType(Byte.class, StackItemType.INTEGER);
        assertClassIsMappedToType(char.class, StackItemType.INTEGER);
        assertClassIsMappedToType(Character.class, StackItemType.INTEGER);
        assertClassIsMappedToType(short.class, StackItemType.INTEGER);
        assertClassIsMappedToType(Short.class, StackItemType.INTEGER);
        assertClassIsMappedToType(int.class, StackItemType.INTEGER);
        assertClassIsMappedToType(Integer.class, StackItemType.INTEGER);
        assertClassIsMappedToType(long.class, StackItemType.INTEGER);
        assertClassIsMappedToType(Long.class, StackItemType.INTEGER);

        // Bools
        assertClassIsMappedToType(boolean.class, StackItemType.BOOLEAN);
        assertClassIsMappedToType(Boolean.class, StackItemType.BOOLEAN);

        // Byte Strings
        assertClassIsMappedToType(String.class, StackItemType.BYTE_STRING);
        assertClassIsMappedToType(ECPoint.class, StackItemType.BYTE_STRING);
        assertClassIsMappedToType(Hash160.class, StackItemType.BYTE_STRING);
        assertClassIsMappedToType(Hash256.class, StackItemType.BYTE_STRING);

        // Byte arrays
        assertClassIsMappedToType(byte[].class, StackItemType.BUFFER);
        assertClassIsMappedToType(Byte[].class, StackItemType.BUFFER);
        assertClassIsMappedToType(ByteString.class, StackItemType.BYTE_STRING);

        // Arrays
        assertClassIsMappedToType(String[].class, StackItemType.ARRAY);
        assertClassIsMappedToType(int[].class, StackItemType.ARRAY);
        assertClassIsMappedToType(Integer[].class, StackItemType.ARRAY);
        assertClassIsMappedToType(boolean[].class, StackItemType.ARRAY);
        assertClassIsMappedToType(byte[][].class, StackItemType.ARRAY);
        assertClassIsMappedToType(List.class, StackItemType.ARRAY);
        assertClassIsMappedToType(Iterator.Struct.class, StackItemType.STRUCT);

        // Map
        assertClassIsMappedToType(Map.class, StackItemType.MAP);

        // Others
        assertClassIsMappedToType(Transaction.class, StackItemType.ANY);
        assertClassIsMappedToType(Iterator.class, StackItemType.ANY);
        assertClassIsMappedToType(Object.class, StackItemType.ANY);
    }

    private void assertClassIsMappedToType(Class<?> clazz, ContractParameterType type) {
        Type t = Type.getType(clazz);
        assertThat(Compiler.mapTypeToParameterType(t), is(type));
    }

    private void assertClassIsMappedToType(Class<?> clazz, StackItemType type) {
        Type t = Type.getType(clazz);
        assertThat(Compiler.mapTypeToStackItemType(t), is(type));
    }

    @Test
    public void testAddInstructionsFromAnnotationWithOperandPrefixAndOperand() throws IOException {
        assertThatAnnotationLeadsToCorrectOpcode(
                InstructionAnnotationWithOperandPrefixAndOperandContract.class.getName(),
                OpCode.PUSHDATA1,
                new byte[]{0x03},
                new byte[]{0x01, 0x02, 0x03});
    }

    @Test
    public void testAddInstructionsFromAnnotationWithOperand() throws IOException {
        assertThatAnnotationLeadsToCorrectOpcode(
                InstructionAnnotationWithOperandContract.class.getName(),
                OpCode.PUSHINT16,
                new byte[]{},
                new byte[]{0x22, 0x33});
    }

    private void assertThatAnnotationLeadsToCorrectOpcode(String className, OpCode opcode,
            byte[] operandPrefix, byte[] operand) throws IOException {

        ClassNode asmClass = AsmHelper.getAsmClass(className, CompilerTest.class.getClassLoader());
        MethodNode method = asmClass.methods.get(1);

        NeoMethod neoMethod = new NeoMethod(method, asmClass);
        Compiler.processInstructionAnnotations(method, neoMethod);

        NeoInstruction insn = neoMethod.getInstructions().values().toArray(
                new NeoInstruction[]{})[0];
        assertThat(insn.getOpcode(), is(opcode));
        assertThat(insn.getOperandPrefix(), is(operandPrefix));
        assertThat(insn.getOperand(), is(operand));
    }

    @Test
    public void sourceCodeUrlIsAdded() throws IOException {
        CompilationUnit res = new Compiler().compile(SourceUrlContract.class.getName());
        assertThat(res.getNefFile().getSourceUrl(), is("https://github.com/neow3j/neow3j"));
    }

    static class InstructionAnnotationWithOperandPrefixAndOperandContract {

        @Instruction(opcode = OpCode.PUSHDATA1, operandPrefix = 0x03, operand = {0x01, 0x02, 0x03})
        public static native void annotatedMethod();

    }

    static class InstructionAnnotationWithOperandContract {

        @Instruction(opcode = OpCode.PUSHINT16, operand = {0x22, 0x33})
        public static native void annotatedMethod();

    }

    @ContractSourceCode("https://github.com/neow3j/neow3j")
    static class SourceUrlContract {

        public static String method() {
            return "hello, world";
        }
    }

}
