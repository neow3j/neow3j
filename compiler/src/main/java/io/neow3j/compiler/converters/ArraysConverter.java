package io.neow3j.compiler.converters;

import static io.neow3j.compiler.AsmHelper.getClassNodeForInternalName;
import static io.neow3j.compiler.Compiler.addPushNumber;
import static io.neow3j.compiler.Compiler.getFieldIndex;
import static java.lang.String.format;

import io.neow3j.compiler.CompilationUnit;
import io.neow3j.compiler.CompilerException;
import io.neow3j.compiler.JVMOpcode;
import io.neow3j.compiler.NeoInstruction;
import io.neow3j.compiler.NeoMethod;
import io.neow3j.constants.OpCode;
import java.io.IOException;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IntInsnNode;

public class ArraysConverter implements Converter {

    private static final int BYTE_ARRAY_TYPE_CODE = 8;

    @Override
    public AbstractInsnNode convert(AbstractInsnNode insn, NeoMethod neoMethod,
            CompilationUnit compUnit) throws IOException {

        JVMOpcode opcode = JVMOpcode.get(insn.getOpcode());
        switch (opcode) {
            case NEWARRAY:
            case ANEWARRAY:
                if (isByteArrayInstantiation(insn)) {
                    neoMethod.addInstruction(new NeoInstruction(OpCode.NEWBUFFER));
                } else {
                    neoMethod.addInstruction(new NeoInstruction(OpCode.NEWARRAY));
                }
                break;
            case BASTORE:
            case IASTORE:
            case AASTORE:
            case CASTORE:
            case LASTORE:
            case SASTORE:
                // Store an element in an array. Before calling this OpCode an array references
                // and an index must have been pushed onto the operand stack. JVM opcodes
                // `DASTORE` and `FASTORE` are not covered because NeoVM does not support
                // doubles and floats.
                neoMethod.addInstruction(new NeoInstruction(OpCode.SETITEM));
                break;
            case AALOAD:
            case BALOAD:
            case CALOAD:
            case IALOAD:
            case LALOAD:
            case SALOAD:
                // Load an element from an array. Before calling this OpCode an array references
                // and an index must have been pushed onto the operand stack. JVM and NeoVM both
                // place the loaded element onto the operand stack. JVM opcodes `DALOAD` and
                // `FALOAD` are not covered because NeoVM does not support doubles and floats.
                neoMethod.addInstruction(new NeoInstruction(OpCode.PICKITEM));
                break;
            case PUTFIELD:
                // Sets a value for a field variable on an object. The compiler doesn't
                // support non-static variables in the smart contract, but we currently handle this
                // JVM opcode because we support instantiation of simple objects like the
                // `StorageMap`.
                addSetItem(insn, neoMethod);
                break;
            case GETFIELD:
                // Get a field variable from an object. The index of the field inside the
                // object is given with the instruction and the object itself must be on top
                // of the operand stack.
                addGetField(insn, neoMethod, compUnit);
                break;
            case MULTIANEWARRAY:
                throw new CompilerException(format("Instruction %s in %s is not supported.",
                        opcode.name(), neoMethod.getSourceMethodName()));
        }
        return insn;
    }

    private static boolean isByteArrayInstantiation(AbstractInsnNode insn) {
        IntInsnNode intInsn = (IntInsnNode) insn;
        return intInsn.operand == BYTE_ARRAY_TYPE_CODE;
    }

    private static void addSetItem(AbstractInsnNode insn, NeoMethod neoMethod) {
        // NeoVM doesn't support objects but can imitate them by using  arrays or structs. The
        // field variables of an object then simply becomes an index in the array. This is done
        // with the SETITEM opcode.
        FieldInsnNode fieldInsn = (FieldInsnNode) insn;
        int idx = getFieldIndex(fieldInsn, neoMethod.getOwnerClass());
        addPushNumber(idx, neoMethod);
        // SETITEM expects the item to be on top of the stack (item -> index -> array)
        neoMethod.addInstruction(new NeoInstruction(OpCode.SWAP));
        neoMethod.addInstruction(new NeoInstruction(OpCode.SETITEM));
    }

    private static void addGetField(AbstractInsnNode insn, NeoMethod neoMethod,
            CompilationUnit compUnit) throws IOException {
        // NeoVM gets fields of objects simply by calling PICKITEM. The operand stack has to have
        // an index on top that is used by PICKITEM. We get this index from the class to which the
        // field belongs to.
        FieldInsnNode fieldInsn = (FieldInsnNode) insn;
        ClassNode classNode = getClassNodeForInternalName(fieldInsn.owner,
                compUnit.getClassLoader());
        int idx = getFieldIndex(fieldInsn, classNode);
        addPushNumber(idx, neoMethod);
        neoMethod.addInstruction(new NeoInstruction(OpCode.PICKITEM));
    }

}
