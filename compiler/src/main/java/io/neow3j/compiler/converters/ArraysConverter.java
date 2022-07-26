package io.neow3j.compiler.converters;

import io.neow3j.compiler.CompilationUnit;
import io.neow3j.compiler.CompilerException;
import io.neow3j.compiler.JVMOpcode;
import io.neow3j.compiler.NeoInstruction;
import io.neow3j.compiler.NeoMethod;
import io.neow3j.script.OpCode;
import io.neow3j.types.StackItemType;
import io.neow3j.utils.BigIntegers;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;

import java.io.IOException;
import java.math.BigInteger;

import static io.neow3j.compiler.AsmHelper.getFieldIndex;
import static io.neow3j.compiler.Compiler.addPushNumber;
import static io.neow3j.compiler.Compiler.buildPushDataInsn;
import static io.neow3j.compiler.Compiler.mapTypeToStackItemType;
import static io.neow3j.compiler.JVMOpcode.BASTORE;
import static io.neow3j.compiler.JVMOpcode.BIPUSH;
import static io.neow3j.compiler.JVMOpcode.DUP;
import static io.neow3j.compiler.JVMOpcode.ICONST_0;
import static io.neow3j.compiler.JVMOpcode.ICONST_5;
import static java.lang.String.format;

public class ArraysConverter implements Converter {

    private static final int BYTE_ARRAY_TYPE_CODE = 8;

    @Override
    public AbstractInsnNode convert(AbstractInsnNode insn, NeoMethod neoMethod, CompilationUnit compUnit)
            throws IOException {

        JVMOpcode opcode = JVMOpcode.get(insn.getOpcode());
        switch (opcode) {
            case NEWARRAY:
            case ANEWARRAY:
                if (isByteArrayInstantiation(insn)) {
                    insn = handleNewByteArray(insn, neoMethod);
                } else {
                    handleNewArray(insn, neoMethod);
                }
                break;
            case BASTORE:
            case IASTORE:
            case AASTORE:
            case CASTORE:
            case LASTORE:
            case SASTORE:
                // Store an element in an array. Before calling this OpCode an array references and an index must
                // have been pushed onto the operand stack. JVM opcodes `DASTORE` and `FASTORE` are not covered
                // because NeoVM does not support doubles and floats.
                neoMethod.addInstruction(new NeoInstruction(OpCode.SETITEM));
                break;
            case AALOAD:
            case BALOAD:
            case CALOAD:
            case IALOAD:
            case LALOAD:
            case SALOAD:
                // Load an element from an array. Before calling this OpCode an array references and an index must
                // have been pushed onto the operand stack. JVM and NeoVM both place the loaded element onto the
                // operand stack. JVM opcodes `DALOAD` and `FALOAD` are not covered because NeoVM does not support
                // doubles and floats.
                neoMethod.addInstruction(new NeoInstruction(OpCode.PICKITEM));
                break;
            case PUTFIELD:
                // Sets a value for a field variable on an object. The compiler doesn't support non-static variables
                // in the smart contract, but we currently handle this JVM opcode because we support instantiation of
                // simple objects like the `StorageMap`.
                addSetItem(insn, neoMethod, compUnit);
                break;
            case GETFIELD:
                // Get a field variable from an object. The index of the field inside the object is given with the
                // instruction and the object itself must be on top of the operand stack.
                addGetField(insn, neoMethod, compUnit);
                break;
            case MULTIANEWARRAY:
                throw new CompilerException(neoMethod, "Only the first dimension of a multi-dimensional array " +
                        "declaration can be defined, e.g., new int[10][].");
        }
        return insn;
    }

    private void handleNewArray(AbstractInsnNode insn, NeoMethod neoMethod) {
        StackItemType stackItemType = null;
        if (insn instanceof IntInsnNode) {
            IntInsnNode intInsn = (IntInsnNode) insn;
            switch (intInsn.operand) {
                case Opcodes.T_BOOLEAN:
                    stackItemType = StackItemType.BOOLEAN;
                    break;
                case Opcodes.T_CHAR:
                case Opcodes.T_BYTE:
                case Opcodes.T_SHORT:
                case Opcodes.T_INT:
                case Opcodes.T_LONG:
                    stackItemType = StackItemType.INTEGER;
                    break;
            }
        } else if (insn instanceof TypeInsnNode) {
            TypeInsnNode typeInsn = (TypeInsnNode) insn;
            stackItemType = mapTypeToStackItemType(Type.getObjectType(typeInsn.desc));
        }
        neoMethod.addNewArrayInstruction(stackItemType);
    }

    private AbstractInsnNode handleNewByteArray(AbstractInsnNode insn, NeoMethod neoMethod) {
        // The last added instruction contains the size of the array.
        BigInteger arraySize = extractPushedNumber(neoMethod.getLastInstruction());
        if (arraySize == null) {
            // The byte array is initialized with a dynamic size.
            neoMethod.addInstruction(new NeoInstruction(OpCode.NEWBUFFER));
            return insn;
        }
        // We take the intValue of the BigInteger because it is unreasonable to create an array larger than max int
        // size.
        byte[] bytes = new byte[arraySize.intValueExact()];
        while (settingOfArrayElementFollows(insn)) {
            // If there is an instruction sequence for setting an array element, we retrieve that element and skip
            // the instruction sequence.
            insn = insn.getNext().getNext(); // The instruction that pushed the index to the stack.
            int idx = getPushedByte(insn, neoMethod);
            insn = insn.getNext(); // The instruction that pushes the element's value to the stack.
            int value = getPushedByte(insn, neoMethod);
            bytes[idx] = (byte) value;
            insn = insn.getNext(); // Skip to the BASTORE instruction.
        }

        // Replace the array size instruction with the PUSHDATA instruction and convert it to BUFFER which is a byte
        // array in neo-vm.
        neoMethod.replaceLastInstruction(buildPushDataInsn(bytes));
        neoMethod.addInstruction(new NeoInstruction(OpCode.CONVERT, new byte[]{StackItemType.BUFFER.byteValue()}));
        return insn;
    }

    private boolean settingOfArrayElementFollows(AbstractInsnNode insn) {
        if (!nextInsnIsOpCode(insn, DUP)) {
            return false;
        }
        insn = insn.getNext();
        if (!nextInsnIsPushByte(insn)) {
            return false;
        }
        insn = insn.getNext();
        if (!nextInsnIsPushByte(insn)) {
            return false;
        }
        insn = insn.getNext();
        return nextInsnIsOpCode(insn, BASTORE);
    }

    private boolean nextInsnIsOpCode(AbstractInsnNode insn, JVMOpcode opcode) {
        if (insn.getNext() == null) {
            return false;
        }
        return insn.getNext().getOpcode() == opcode.getOpcode();
    }

    private boolean nextInsnIsPushByte(AbstractInsnNode insn) {
        if (insn.getNext() == null) {
            return false;
        }
        insn = insn.getNext();
        return insn.getOpcode() >= ICONST_0.getOpcode() && insn.getOpcode() <= ICONST_5.getOpcode() ||
                insn.getOpcode() == BIPUSH.getOpcode();
    }

    private static int getPushedByte(AbstractInsnNode insn, NeoMethod neoMethod) {
        if (insn.getOpcode() >= ICONST_0.getOpcode() && insn.getOpcode() <= ICONST_5.getOpcode()) {
            return insn.getOpcode() - ICONST_0.getOpcode();
        } else if (insn.getOpcode() == BIPUSH.getOpcode()) {
            return ((IntInsnNode) insn).operand;
        }
        throw new CompilerException(neoMethod, format("Unexpected instruction with opcode %s.", insn.getOpcode()));
    }

    private static boolean isByteArrayInstantiation(AbstractInsnNode insn) {
        return insn instanceof IntInsnNode && ((IntInsnNode) insn).operand == BYTE_ARRAY_TYPE_CODE;
    }

    // Returns -2 if the given instruction is not a PUSH instruction.
    private BigInteger extractPushedNumber(NeoInstruction insn) {
        if (insn.getOpcode().getCode() <= OpCode.PUSHINT256.getCode()) {
            return BigIntegers.fromLittleEndianByteArray(insn.getOperand());
        }
        if (insn.getOpcode().getCode() >= OpCode.PUSHM1.getCode() &&
                insn.getOpcode().getCode() <= OpCode.PUSH16.getCode()) {
            return BigInteger.valueOf(insn.getOpcode().getCode() - OpCode.PUSHM1.getCode() - 1);
        }
        return null; // If the instruction is not a PUSH opcode.
    }

    private static void addSetItem(AbstractInsnNode insn, NeoMethod neoMethod, CompilationUnit compUnit)
            throws IOException {
        // NeoVM doesn't support objects but can imitate them by using  arrays or structs. The field variables of an
        // object then simply becomes an index in the array. This is done with the SETITEM opcode.
        FieldInsnNode fieldInsn = (FieldInsnNode) insn;
        int idx = getFieldIndex(fieldInsn, compUnit);
        addPushNumber(idx, neoMethod);
        // SETITEM expects the item to be on top of the stack (item -> index -> array)
        neoMethod.addInstruction(new NeoInstruction(OpCode.SWAP));
        neoMethod.addInstruction(new NeoInstruction(OpCode.SETITEM));
    }

    private static void addGetField(AbstractInsnNode insn, NeoMethod neoMethod, CompilationUnit compUnit)
            throws IOException {
        // NeoVM gets fields of objects simply by calling PICKITEM. The operand stack has to have an index on top
        // that is used by PICKITEM. We get this index from the class to which the field belongs to.
        FieldInsnNode fieldInsn = (FieldInsnNode) insn;
        int idx = getFieldIndex(fieldInsn, compUnit);
        addPushNumber(idx, neoMethod);
        neoMethod.addInstruction(new NeoInstruction(OpCode.PICKITEM));
    }

}
