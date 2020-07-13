package io.neow3j.devpack.compiler;

import org.objectweb.asm.tree.LocalVariableNode;

public class NeoVariable {

    int index;
    int jvmIndex;
    LocalVariableNode asmVariable;

    public NeoVariable(int index, int jvmIndex, LocalVariableNode asmVariable) {
        this.index = index;
        this.jvmIndex = jvmIndex;
        this.asmVariable = asmVariable;
    }
}
