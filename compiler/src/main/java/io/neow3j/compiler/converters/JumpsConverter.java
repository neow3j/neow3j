package io.neow3j.compiler.converters;

import io.neow3j.compiler.CompilationUnit;
import io.neow3j.compiler.CompilerException;
import io.neow3j.compiler.JVMOpcode;
import io.neow3j.compiler.NeoInstruction;
import io.neow3j.compiler.NeoJumpInstruction;
import io.neow3j.compiler.NeoMethod;
import io.neow3j.script.OpCode;

import java.util.List;

import org.objectweb.asm.Label;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;

import static io.neow3j.compiler.Compiler.addPushNumber;
import static java.lang.String.format;

public class JumpsConverter implements Converter {

    @Override
    public AbstractInsnNode convert(AbstractInsnNode insn, NeoMethod neoMethod, CompilationUnit compUnit) {

        // Java jump addresses are restricted to 2 bytes, i.e. there are no 4-byte jump addresses as in NeoVM. It is
        // simpler for the compiler implementation to always use the 4-byte NeoVM jump opcodes and then optimize (to
        // 1-byte addresses) in a second step. This is how the dotnet-devpack handles it too.

        JVMOpcode opcode = JVMOpcode.get(insn.getOpcode());
        switch (opcode) {
            // region ### OBJECT COMPARISON ###
            case IF_ACMPEQ:
                neoMethod.addInstruction(new NeoInstruction(OpCode.EQUAL));
                addJumpInstruction(neoMethod, insn, OpCode.JMPIF_L);
                break;
            case IF_ACMPNE:
                neoMethod.addInstruction(new NeoInstruction(OpCode.NOTEQUAL));
                addJumpInstruction(neoMethod, insn, OpCode.JMPIF_L);
                break;
            // endregion ### OBJECT COMPARISON ###

            // region ### INTEGER COMPARISON ###
            case IF_ICMPEQ:
                addJumpInstruction(neoMethod, insn, OpCode.JMPEQ_L);
                break;
            case IF_ICMPNE: // integer comparison
                addJumpInstruction(neoMethod, insn, OpCode.JMPNE_L);
                break;
            case IF_ICMPLT:
                addJumpInstruction(neoMethod, insn, OpCode.JMPLT_L);
                break;
            case IF_ICMPGT:
                addJumpInstruction(neoMethod, insn, OpCode.JMPGT_L);
                break;
            case IF_ICMPLE:
                addJumpInstruction(neoMethod, insn, OpCode.JMPLE_L);
                break;
            case IF_ICMPGE:
                addJumpInstruction(neoMethod, insn, OpCode.JMPGE_L);
                break;
            // endregion ### INTEGER COMPARISON ###

            // region ### INTEGER COMPARISON WITH ZERO ###
            // These opcodes operate on boolean, byte, char, short, and int. In the latter four cases (IFLT, IFLE,
            // IFGT, and IFGE) the NeoVM opcode is switched, e.g., from GT to LE because zero value will be on top of
            // the stack and not the integer value.
            case IFEQ: // Tests if the value on the stack is equal to zero.
                addJumpInstruction(neoMethod, insn, OpCode.JMPIFNOT_L);
                break;
            case IFNULL: // Object comparison with null.
                neoMethod.addInstruction(new NeoInstruction(OpCode.ISNULL));
                addJumpInstruction(neoMethod, insn, OpCode.JMPIF_L);
                break;
            case IFNE: // Tests if the value on the stack is not equal to zero.
                addJumpInstruction(neoMethod, insn, OpCode.JMPIF_L);
                break;
            case IFNONNULL: // Object comparison with null.
                neoMethod.addInstruction(new NeoInstruction(OpCode.ISNULL));
                addJumpInstruction(neoMethod, insn, OpCode.JMPIFNOT_L);
                break;
            case IFLT: // Tests if the value on the stack is less than zero.
                addPushNumber(0, neoMethod);
                addJumpInstruction(neoMethod, insn, OpCode.JMPLT_L);
                break;
            case IFLE: // Tests if the value on the stack is less than or equal to zero.
                addPushNumber(0, neoMethod);
                addJumpInstruction(neoMethod, insn, OpCode.JMPLE_L);
                break;
            case IFGT: // Tests if the value on the stack is greater than zero.
                addPushNumber(0, neoMethod);
                addJumpInstruction(neoMethod, insn, OpCode.JMPGT_L);
                break;
            case IFGE: // Tests if the value on the stack is greater than or equal to zero.
                addPushNumber(0, neoMethod);
                addJumpInstruction(neoMethod, insn, OpCode.JMPGE_L);
                break;
            // endregion ### INTEGER COMPARISON WITH ZERO ###

            case LCMP:
                // Comparison of two longs resulting in an integer with value -1, 0, or 1.
                // This opcode has no direct counterpart in NeoVM because NeoVM does not differentiate between int
                // and long.
                insn = handleLongComparison(neoMethod, insn);
                break;
            case GOTO:
            case GOTO_W:
                // Unconditionally branch of to another code location.
                neoMethod.addInstruction(new NeoJumpInstruction(OpCode.JMP_L, ((JumpInsnNode) insn).label.getLabel()));
                break;
            case LOOKUPSWITCH:
                handleLookupSwitch(neoMethod, insn);
                break;
            case TABLESWITCH:
                handleTableSwitch(neoMethod, insn);
                break;
            case JSR:
            case RET:
            case JSR_W:
                throw new CompilerException(neoMethod, format("JVM opcode %s is not supported.", opcode.name()));
        }
        return insn;
    }

    private static void addJumpInstruction(NeoMethod neoMethod, AbstractInsnNode insn, OpCode jmpOpcode) {
        Label jmpLabel = ((JumpInsnNode) insn).label.getLabel();
        neoMethod.addInstruction(new NeoJumpInstruction(jmpOpcode, jmpLabel));
    }

    private static AbstractInsnNode handleLongComparison(NeoMethod neoMethod, AbstractInsnNode insn) {
        JumpInsnNode jumpInsn = (JumpInsnNode) insn.getNext();
        JVMOpcode jvmOpcode = JVMOpcode.get(jumpInsn.getOpcode());
        assert jvmOpcode != null : "Opcode of jump instruction was not set.";
        switch (jvmOpcode) {
            case IFEQ:
                addJumpInstruction(neoMethod, jumpInsn, OpCode.JMPEQ_L);
                break;
            case IFNE:
                addJumpInstruction(neoMethod, jumpInsn, OpCode.JMPNE_L);
                break;
            case IFLT:
                addJumpInstruction(neoMethod, jumpInsn, OpCode.JMPLT_L);
                break;
            case IFGT:
                addJumpInstruction(neoMethod, jumpInsn, OpCode.JMPGT_L);
                break;
            case IFLE:
                addJumpInstruction(neoMethod, jumpInsn, OpCode.JMPLE_L);
                break;
            case IFGE:
                addJumpInstruction(neoMethod, jumpInsn, OpCode.JMPGE_L);
                break;
            default:
                throw new CompilerException(neoMethod, format("Unexpected JVM opcode %s following long comparison " +
                        "(%s)", jvmOpcode.name(), JVMOpcode.LCMP.name()));
        }
        return jumpInsn;
    }

    private static void handleLookupSwitch(NeoMethod neoMethod, AbstractInsnNode insn) {
        LookupSwitchInsnNode switchNode = (LookupSwitchInsnNode) insn;
        for (int i = 0; i < switchNode.keys.size(); i++) {
            int key = switchNode.keys.get(i);
            processCase(i, key, switchNode.labels, switchNode.dflt.getLabel(), neoMethod);
        }
        // After handling the `LookupSwitchInsnNode` the compiler can continue processing all the case branches in
        // its `handleInsn(...)` method.
    }

    private static void processCase(int i, int key, List<LabelNode> labels, Label defaultLabel, NeoMethod neoMethod) {
        // The nextCaseLabel is used to connect the current case with the next case. If the current case is not
        // successful, then the process will jump to the next case marked with this label.
        Label nextCaseLabel;
        boolean isLastCase = isLastCase(i, labels, defaultLabel);
        if (isLastCase) {
            // If this is the last case statement (before the `default`) then we don't need to duplicate the value
            // and the next label is the one of the default body.
            nextCaseLabel = defaultLabel;
        } else {
            nextCaseLabel = new Label();
            // The value being compared in the switch needs to be duplicated before each case.
            neoMethod.addInstruction(new NeoInstruction(OpCode.DUP));
        }
        addPushNumber(key, neoMethod);
        neoMethod.addInstruction(new NeoJumpInstruction(OpCode.JMPNE_L, nextCaseLabel));

        if (!isLastCase) {
            // If the case is the right one we need to drop the value duplicated before.
            // But not for the last `case` statement (before the default).
            neoMethod.addInstruction(new NeoInstruction(OpCode.DROP));
        }
        Label jmpLabel = labels.get(i).getLabel();
        neoMethod.addInstruction(new NeoJumpInstruction(OpCode.JMP_L, jmpLabel));
        // Set the nextCaseLabel on the NeoMethod so that the next added instruction becomes the jump target.
        neoMethod.setCurrentLabel(nextCaseLabel);
    }

    // Checks if the given index marks the last case in the given list of case statements (i.e. labels of the cases'
    // jump targets). If the case is only followed by cases that target the default case it is still considered to be
    // last.
    private static boolean isLastCase(int i, List<LabelNode> labelNodes, Label defaultLbl) {
        assert i < labelNodes.size() && i >= 0 : "Index was outside of the list of label nodes.";
        if (i == labelNodes.size() - 1 && !(labelNodes.get(i).getLabel() == defaultLbl)) {
            return true;
        }
        for (; i < labelNodes.size(); i++) {
            LabelNode labelNode = labelNodes.get(i);
            if (labelNode.getLabel() != defaultLbl) {
                return false;
            }
        }
        return true;
    }

    private static void handleTableSwitch(NeoMethod neoMethod, AbstractInsnNode insn) {
        TableSwitchInsnNode switchNode = (TableSwitchInsnNode) insn;
        for (int i = 0; i < switchNode.labels.size(); i++) {
            if (switchNode.labels.get(i).getLabel() == switchNode.dflt.getLabel()) {
                // We don't handle the cases that the Java compiler only to reach sequential values.
                continue;
            }
            int key = switchNode.min + i;
            processCase(i, key, switchNode.labels, switchNode.dflt.getLabel(), neoMethod);
        }
        // After handling the `TableSwitchInsnNode` the compiler can continue processing all the case branches in its
        // `handleInsn(...)` method.
    }

}
