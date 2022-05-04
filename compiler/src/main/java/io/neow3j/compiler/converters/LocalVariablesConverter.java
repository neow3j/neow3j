package io.neow3j.compiler.converters;

import io.neow3j.compiler.CompilationUnit;
import io.neow3j.compiler.JVMOpcode;
import io.neow3j.compiler.NeoMethod;

import java.io.IOException;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import static io.neow3j.compiler.LocalVariableHelper.addLoadLocalVariable;
import static io.neow3j.compiler.LocalVariableHelper.addStoreLocalVariable;

public class LocalVariablesConverter implements Converter {

    @Override
    public AbstractInsnNode convert(AbstractInsnNode insn, NeoMethod neoMethod, CompilationUnit compUnit)
            throws IOException {

        JVMOpcode opcode = JVMOpcode.get(insn.getOpcode());
        switch (opcode) {
            case ASTORE:
            case ASTORE_0:
            case ASTORE_1:
            case ASTORE_2:
            case ASTORE_3:
            case ISTORE:
            case ISTORE_0:
            case ISTORE_1:
            case ISTORE_2:
            case ISTORE_3:
            case LSTORE:
            case LSTORE_0:
            case LSTORE_1:
            case LSTORE_2:
            case LSTORE_3:
                addStoreLocalVariable(((VarInsnNode) insn).var, neoMethod);
                break;
            case ALOAD:
            case ALOAD_0:
            case ALOAD_1:
            case ALOAD_2:
            case ALOAD_3:
            case ILOAD:
            case ILOAD_0:
            case ILOAD_1:
            case ILOAD_2:
            case ILOAD_3:
            case LLOAD:
            case LLOAD_0:
            case LLOAD_1:
            case LLOAD_2:
            case LLOAD_3:
                // Load a variable from the local variable pool. Such a variable can be a method parameter or a
                // normal variable in the method body. The index of the variable in the pool is given in the
                // instruction and not on the operand stack.
                addLoadLocalVariable(((VarInsnNode) insn).var, neoMethod);
                break;
        }
        return insn;
    }

}
