package io.neow3j.compiler;

import org.objectweb.asm.tree.LocalVariableNode;

/**
 * A representation for variables, e.g., method-local variables and method parameters.
 */
public class NeoVariable {

    /**
     * This variable's index inside of the {@link NeoMethod}.
     */
    int index;

    /**
     * This variable's original index in the JVM bytecode.
     */
    int jvmIndex;

    /**
     * The ASM counterpart of this variable.
     */
    LocalVariableNode asmVariable;

    NeoVariable(int index, int jvmIndex, LocalVariableNode asmVariable) {
        this.index = index;
        this.jvmIndex = jvmIndex;
        this.asmVariable = asmVariable;
    }
}
