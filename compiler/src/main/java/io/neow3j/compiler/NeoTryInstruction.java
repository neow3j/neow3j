package io.neow3j.compiler;

import io.neow3j.script.OpCode;
import org.objectweb.asm.Label;

public class NeoTryInstruction extends NeoInstruction {

    // The target label of the catch block. Can be null.
    private final Label catchOffsetLabel;
    // The target label of the finally block. Can be null.
    private final Label finallyOffsetLabel;

    public NeoTryInstruction(Label catchOffsetLabel, Label finallyOffsetLabel) {
        super(OpCode.TRY_L, new byte[8]); // The operand holds two offsets of 4 bytes each.
        if (catchOffsetLabel == null && finallyOffsetLabel == null) {
            throw new CompilerException("Constructing try instruction without catch offset and without finally offset" +
                    " is illegal. Every try instruction must at least have a catch offset or a finally offset.");
        }
        this.catchOffsetLabel = catchOffsetLabel;
        this.finallyOffsetLabel = finallyOffsetLabel;
    }

    public Label getCatchOffsetLabel() {
        return catchOffsetLabel;
    }

    public Label getFinallyOffsetLabel() {
        return finallyOffsetLabel;
    }

}
