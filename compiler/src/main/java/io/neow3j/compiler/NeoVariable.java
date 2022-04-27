package io.neow3j.compiler;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.LocalVariableNode;

/**
 * A representation for variables, e.g., method-local variables and method parameters.
 */
public class NeoVariable {

    /**
     * This variable's index inside the {@link NeoMethod}.
     */
    private int neoIndex;

    /**
     * This variable's original index in the JVM bytecode.
     */
    private int jvmIndex;

    /**
     * The ASM counterpart of this variable.
     */
    private LocalVariableNode asmVariable;

    NeoVariable(int index, int jvmIndex, LocalVariableNode asmVariable) {
        this.neoIndex = index;
        this.jvmIndex = jvmIndex;
        this.asmVariable = asmVariable;
    }

    public int getNeoIndex() {
        return neoIndex;
    }

    public int getJvmIndex() {
        return jvmIndex;
    }

    public LocalVariableNode getAsmVariable() {
        return asmVariable;
    }

    /**
     * @return the variable's name.
     */
    public String getName() {
        if (asmVariable == null) {
            return null;
        }
        return asmVariable.name;
    }

    public String getDescriptor() {
        if (asmVariable == null) {
            return Type.getDescriptor(Object.class);
        }
        return asmVariable.desc;
    }

}
