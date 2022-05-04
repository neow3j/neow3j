package io.neow3j.compiler;

import io.neow3j.script.OpCode;
import org.objectweb.asm.Label;

public class NeoJumpInstruction extends NeoInstruction {

    // The target label of this jump instruction. It is used to find the corresponding instruction that is labeled
    // identically and that is this instruction's target.
    private Label label;

    public NeoJumpInstruction(OpCode opcode, byte[] operand) {
        super(opcode, operand);
    }

    // Constructs a new jump instruction with a 4-byte, zero-valued operand. The operand is the target address of
    // this jump instruction. That address is set in `NeoMethod.finalizeMethod()`.
    public NeoJumpInstruction(OpCode opcode, Label label) {
        super(opcode, new byte[4]);
        this.label = label;
    }

    public Label getLabel() {
        return label;
    }

}
