package io.neow3j.compiler;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldNode;

import static io.neow3j.compiler.Compiler.mapTypeToParameterType;

/**
 * Represents a static field variable, also called a contract variable.
 */
public class NeoContractVariable {

    /**
     * This variable's index in the contract.
     */
    private int idx;

    /**
     * The ASM counterpart of this variable.
     */
    private FieldNode asmField;

    public NeoContractVariable(int idx, FieldNode asmField) {
        this.idx = idx;
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
}
