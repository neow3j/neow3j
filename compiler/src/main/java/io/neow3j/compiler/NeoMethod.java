package io.neow3j.compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Represents a method in a NeoVM script.
 */
public class NeoMethod {

    /**
     * The ASM counterpart of this method.
     */
    MethodNode asmMethod;

    /**
     * The type that contains this method.
     */
    ClassNode ownerType;

    /**
     * A string uniquely identifying this method. Includes the owner type's name the method's
     * signature and the method's name.
     */
    String id;

    /**
     * This method's NeoVM instructions. Maps from instruction address to instruction.
     */
    SortedMap<Integer, NeoInstruction> instructions = new TreeMap<>();

    /**
     * This method's local variables (excl. method parametrs).
     */
    List<NeoVariable> variables = new ArrayList<>();

    /**
     * Maps JVM bytecode indices to local variables.
     */
    Map<Integer, NeoVariable> variablesByJVMIndex = new HashMap<>();

    /**
     * This method's parameters.
     */
    List<NeoVariable> parameters = new ArrayList<>();

    /**
     * Maps JVM bytecode indices to method parameters.
     */
    Map<Integer, NeoVariable> parametersByJVMIndex = new HashMap<>();

    /**
     * Determin if a method is a contract's entry point.
     */
    boolean isEntryPoint = false;

    /**
     * Determines if this method will show up in the contract's ABI.
     */
    boolean isAbiMethod = false;

    /**
     * The address after this method's last instruction byte. I.e. the next free address. This
     * address is not absolute in relation to the {@link NeoModule} this method belongs to. It is a
     * method-internal address.
     */
    int nextAddress = 0;

    /**
     * The address in the {@link NeoModule} at which this method starts.
     */
    Integer startAddress = null;

    /**
     * The name of this method. Used like this, e.g., in the contracts ABI.
     */
    String name;

    NeoMethod(MethodNode asmMethod, ClassNode owner) {
        this.asmMethod = asmMethod;
        this.ownerType = owner;
        this.id = getMethodId(asmMethod, owner);
        this.name = asmMethod.name;
    }

    /**
     * Creates a unique id for the given method used to identify this method in the {@link
     * NeoModule}.
     */
    static String getMethodId(MethodNode asmMethod, ClassNode owner) {
        return owner.name + "." + asmMethod.name + asmMethod.desc;
    }

    /**
     * Adds a parameter to this method.
     */
    void addParameter(NeoVariable var) {
        parameters.add(var.index, var);
        this.parametersByJVMIndex.put(var.jvmIndex, var);
    }

    /**
     * Adds a local variable to this method.
     */
    void addVariable(NeoVariable var) {
        this.variables.add(var.index, var);
        this.variablesByJVMIndex.put(var.jvmIndex, var);
    }

    /**
     * Gets the variable at the given index from this method in its JVM bytecode representation
     *
     * @return the variable.
     */
    NeoVariable getVariableByJVMIndex(int index) {
        return this.variablesByJVMIndex.get(index);
    }

    /**
     * Gets the parameter at the given index from this method in its JVM bytecode representation
     *
     * @return the parameter.
     */
    NeoVariable getParameterByJVMIndex(int index) {
        return this.parametersByJVMIndex.get(index);
    }

    /**
     * Adds the given instruction to this method. Uses the instructions address for ordering among
     * the other instructions.
     */
    void addInstruction(NeoInstruction neoInsn) {
        this.instructions.put(this.nextAddress, neoInsn);
        this.nextAddress += 1 + neoInsn.operand.length;
    }

    /**
     * Serializes this method to a byte array, by serializing all its instructions ordered by
     * instruction address.
     *
     * @return the byte array.
     */
    byte[] toByteArray() {
        byte[] bytes = new byte[byteSize()];
        int i = 0;
        for (NeoInstruction insn : this.instructions.values()) {
            byte[] insnBytes = insn.toByteArray();
            System.arraycopy(insnBytes, 0, bytes, i, insnBytes.length);
            i += insnBytes.length;
        }
        return bytes;
    }

    /**
     * Gets this methods size in bytes (after serializing).
     *
     * @return the byte-size of this method.
     */
    int byteSize() {
        return this.instructions.values().stream()
                .map(NeoInstruction::byteSize)
                .reduce(Integer::sum).get();
    }

}
