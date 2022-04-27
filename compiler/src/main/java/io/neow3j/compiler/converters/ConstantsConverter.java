package io.neow3j.compiler.converters;

import io.neow3j.compiler.CompilationUnit;
import io.neow3j.compiler.JVMOpcode;
import io.neow3j.compiler.NeoInstruction;
import io.neow3j.compiler.NeoMethod;
import io.neow3j.script.OpCode;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import static io.neow3j.compiler.Compiler.addLoadConstant;
import static io.neow3j.compiler.Compiler.addPushNumber;
import static io.neow3j.compiler.Compiler.isAssertionDisabledStaticField;
import static io.neow3j.utils.ClassUtils.getFullyQualifiedNameForInternalName;

public class ConstantsConverter implements Converter {

    private static final String DESIRED_ASSERTION_STATUS = "desiredAssertionStatus";

    @Override
    public AbstractInsnNode convert(AbstractInsnNode insn, NeoMethod neoMethod, CompilationUnit compUnit) {
        JVMOpcode opcode = JVMOpcode.get(insn.getOpcode());
        switch (opcode) {
            case ICONST_M1:
            case ICONST_0:
            case ICONST_1:
            case ICONST_2:
            case ICONST_3:
            case ICONST_4:
            case ICONST_5:
                addPushNumber(opcode.getOpcode() - 3, neoMethod);
                break;
            case LCONST_0:
                addPushNumber(0, neoMethod);
                break;
            case LCONST_1:
                addPushNumber(1, neoMethod);
                break;
            case LDC:
            case LDC_W:
            case LDC2_W:
                if (isDesiredAssertionStatusConst(insn)) {
                    // Ignore instructions until static variable $assertionDisabled is loaded
                    while (!isAssertionDisabledStaticField(insn)) {
                        insn = insn.getNext();
                    }
                    insn = insn.getNext();
                    break;
                }
                addLoadConstant(insn, neoMethod);
                break;
            case ACONST_NULL:
                neoMethod.addInstruction(new NeoInstruction(OpCode.PUSHNULL));
                break;
            case BIPUSH: // Has an operand with an int value from -128 to 127.
            case SIPUSH: // Has an operand with an int value from -32768 to 32767.
                addPushNumber(((IntInsnNode) insn).operand, neoMethod);
                break;
        }
        return insn;
    }

    private boolean isDesiredAssertionStatusConst(AbstractInsnNode insn) {
        if (insn.getNext().getType() != AbstractInsnNode.METHOD_INSN) {
            return false;
        }
        MethodInsnNode methodInsn = (MethodInsnNode) insn.getNext();
        return methodInsn.name.equals(DESIRED_ASSERTION_STATUS) &&
                getFullyQualifiedNameForInternalName(methodInsn.owner).equals(Class.class.getCanonicalName());
    }

}
