package io.neow3j.compiler;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
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
     * The ASM counterpart of this variable.
     */
    private FieldNode asmField;

    /**
     * The owning class of this variable.
     */
    private ClassNode owner;

    public NeoContractVariable(FieldNode asmField, ClassNode owner, int idx) {
        this.asmField = asmField;
        this.owner = owner;
        this.idx = idx;
    }

    /**
     * Gets this' variables in the format required in the debug info for the Neo Debugger.
     *
     * @return the string for the debug info.
     */
    public String getAsDebugInfoVariable() {
        String type = mapTypeToParameterType(Type.getType(asmField.desc)).jsonValue();
        return asmField.name + "," + type + "," + idx;
    }

    public int getNeoIdx() {
        return idx;
    }

    public ClassNode getOwner() {
        return owner;
    }

    public static String getVariableId(ClassNode owner, FieldNode variable) {
        return owner.name + variable.name + variable.desc;
    }

}
