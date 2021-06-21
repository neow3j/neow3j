package io.neow3j.compiler;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldNode;

import static io.neow3j.compiler.Compiler.mapTypeToParameterType;

/**
 * Represents a static field variable, also called a contract variable.
 */
public class NeoContractVariable {

    /**
     * This variable's index in the contract (NeoVM bytecode).
     */
    private int idx;

    /**
     * This variable's index in the JVM byte code. This index can deviate from the NeoVM index
     * because events are not counted as variables in the NeoVM code but are normal variables in
     * the JVM bytecode.
     */
    private int jvmIdx;

    /**
     * The ASM counterpart of this variable.
     */
    private FieldNode asmField;

    public NeoContractVariable(int idx, int jvmIdx, FieldNode asmField) {
        this.idx = idx;
        this.jvmIdx = jvmIdx;
        this.asmField = asmField;
    }

    /**
     * Gets this variables in the format required in the debug info for the Neo Debugger.
     * @return the string for the debug info.
     */
    public String getAsDebugInfoVariable() {
        String type = mapTypeToParameterType(Type.getType(asmField.desc)).jsonValue();
        return asmField.name + "," + type + "," + idx;
    }

    public int getNeoIdx() {
        return idx;
    }

    public int getJvmIdx() {
        return jvmIdx;
    }

    public FieldNode getAsmField() {
        return asmField;
    }
}
