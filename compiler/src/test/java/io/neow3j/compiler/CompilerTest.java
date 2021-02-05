package io.neow3j.compiler;

import static io.neow3j.model.types.ContractParameterType.ANY;
import static io.neow3j.model.types.ContractParameterType.ARRAY;
import static io.neow3j.model.types.ContractParameterType.BOOLEAN;
import static io.neow3j.model.types.ContractParameterType.BYTE_ARRAY;
import static io.neow3j.model.types.ContractParameterType.HASH160;
import static io.neow3j.model.types.ContractParameterType.HASH256;
import static io.neow3j.model.types.ContractParameterType.INTEGER;
import static io.neow3j.model.types.ContractParameterType.INTEROP_INTERFACE;
import static io.neow3j.model.types.ContractParameterType.MAP;
import static io.neow3j.model.types.ContractParameterType.PUBLIC_KEY;
import static io.neow3j.model.types.ContractParameterType.STRING;
import static io.neow3j.model.types.ContractParameterType.VOID;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import io.neow3j.devpack.ECPoint;
import io.neow3j.devpack.Map;
import io.neow3j.constants.OpCode;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.Hash256;
import io.neow3j.devpack.List;
import io.neow3j.devpack.annotations.Instruction;
import io.neow3j.devpack.neo.Iterator;
import io.neow3j.devpack.neo.Transaction;
import io.neow3j.model.types.ContractParameterType;
import java.io.IOException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class CompilerTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void mapTypeToParameterTypeShouldReturnTheCorrectTypes() {
        // Integers
        assertClassIsMappedToType(byte.class, INTEGER);
        assertClassIsMappedToType(Byte.class, INTEGER);
        assertClassIsMappedToType(char.class, INTEGER);
        assertClassIsMappedToType(Character.class, INTEGER);
        assertClassIsMappedToType(short.class, INTEGER);
        assertClassIsMappedToType(Short.class, INTEGER);
        assertClassIsMappedToType(int.class, INTEGER);
        assertClassIsMappedToType(Integer.class, INTEGER);
        assertClassIsMappedToType(long.class, INTEGER);
        assertClassIsMappedToType(Long.class, INTEGER);

        // Bools
        assertClassIsMappedToType(boolean.class, BOOLEAN);
        assertClassIsMappedToType(Boolean.class, BOOLEAN);

        // Strings
        assertClassIsMappedToType(String.class, STRING);

        // Void
        assertClassIsMappedToType(void.class, VOID);
        assertClassIsMappedToType(Void.class, VOID);

        // Byte arrays
        assertClassIsMappedToType(byte[].class, BYTE_ARRAY);
        assertClassIsMappedToType(Byte[].class, BYTE_ARRAY);

        // Arrays
        assertClassIsMappedToType(String[].class, ARRAY);
        assertClassIsMappedToType(int[].class, ARRAY);
        assertClassIsMappedToType(Integer[].class, ARRAY);
        assertClassIsMappedToType(boolean[].class, ARRAY);
        assertClassIsMappedToType(byte[][].class, ARRAY);
        assertClassIsMappedToType(List.class, ARRAY);

        // Public Key
        assertClassIsMappedToType(ECPoint.class, PUBLIC_KEY);

        // Map
        assertClassIsMappedToType(Map.class, MAP);

        // Hash
        assertClassIsMappedToType(Hash160.class, HASH160);
        assertClassIsMappedToType(Hash256.class, HASH256);

        // Others
        assertClassIsMappedToType(Transaction.class, INTEROP_INTERFACE);
        assertClassIsMappedToType(Iterator.class, INTEROP_INTERFACE);
        assertClassIsMappedToType(Object.class, ANY);
        assertClassIsMappedToType(CompilerTest.class, ANY);
    }

    private void assertClassIsMappedToType(Class<?> clazz, ContractParameterType type) {
        Type t = Type.getType(clazz);
        assertThat(Compiler.mapTypeToParameterType(t), is(type));
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
        Compiler.addInstructionsFromAnnotation(method, neoMethod);

        NeoInstruction insn = neoMethod.getInstructions().values().toArray(
                new NeoInstruction[]{})[0];
        assertThat(insn.getOpcode(), is(opcode));
        assertThat(insn.getOperandPrefix(), is(operandPrefix));
        assertThat(insn.getOperand(), is(operand));
    }

    static class InstructionAnnotationWithOperandPrefixAndOperandContract {

        @Instruction(opcode = OpCode.PUSHDATA1, operandPrefix = 0x03, operand = {0x01, 0x02, 0x03})
        public static native void annotatedMethod();

    }

    static class InstructionAnnotationWithOperandContract {

        @Instruction(opcode = OpCode.PUSHINT16, operand = {0x22, 0x33})
        public static native void annotatedMethod();

    }



}
