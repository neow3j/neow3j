package io.neow3j.compiler;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.objectweb.asm.Label;
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

    // This method's instructions sorted by their address. The addresses in this map are only
    // relative to this method and not the whole `NeoModule` in which this method lives in.
    SortedMap<Integer, NeoInstruction> instructions = new TreeMap<>();
    List<NeoJumpInstruction> jumpInstructions = new ArrayList<>();

    /**
     * This method's local variables (excl. method parametrs).
     */
    SortedMap<Integer, NeoVariable> variablesByNeoIndex = new TreeMap<>();

    /**
     * Maps JVM bytecode indices to local variables.
     */
    SortedMap<Integer, NeoVariable> variablesByJVMIndex = new TreeMap<>();

    /**
     * This method's parameters.
     */
    SortedMap<Integer, NeoVariable> parametersByNeoIndex = new TreeMap<>();

    /**
     * Maps JVM bytecode indices to method parameters.
     */
    SortedMap<Integer, NeoVariable> parametersByJVMIndex = new TreeMap<>();

    /**
     * Determines if a method is a contract's entry point.
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

    // The current label of an instruction. Used in the compilation process to resolve jump
    // addresses. In contrast to `LineNumberNodes`, `LableNodes` are only applicable to the
    // very next instruction node.
    Label currentLabel;

    // The current JVM instruction line number. Used in the compilation process to map line
    // numbers to `NeoInstructions`.
    int currentLine;

    // A mapping between labels - received from `LabelNodes` - and `NeoInstructions` used to keep
    // track of possible jump targets. This is needed when resolving jump addresses for
    // opcodes like JMPIF.
    Map<Label, NeoInstruction> jumpTargets = new HashMap<>();

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
        this.parametersByNeoIndex.put(var.neoIndex, var);
        this.parametersByJVMIndex.put(var.jvmIndex, var);
    }

    /**
     * Adds a local variable to this method.
     */
    void addVariable(NeoVariable var) {
        this.variablesByNeoIndex.put(var.neoIndex, var);
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

    // Adds the given instruction to this method. The current source code line number and the
    // current address (relative to this method) is added to the instruction.
    void addInstruction(NeoInstruction neoInsn) {
        neoInsn.setLineNr(this.currentLine);
        if (this.currentLabel != null) {
            // When the compiler sees a `LabelNode` it stores it on the `currentLabelNode` field
            // and continues. The next instruction is the one that the label belongs. We expect
            // that when a new instruction is added to this method and the `currentLabelNode` is
            // set, that label belongs to that `NeoInstruction`. The label is unset as
            // soon as it has been assigned.
            // TODO: Clarify if this behavior is correct in all scenarios. JVM instructions don't
            //  always get replaced one-to-one with `NeoInstructions`.
            // TODO: Clarify if we only need jump points for instructions that additionally have
            //  a `FrameNode` before them.
            this.jumpTargets.put(this.currentLabel, neoInsn);
            this.currentLabel = null;
        }
        neoInsn.setAddress(this.nextAddress);
        this.instructions.put(this.nextAddress, neoInsn);
        if (neoInsn instanceof NeoJumpInstruction) {
            this.jumpInstructions.add((NeoJumpInstruction) neoInsn);
        }
        this.nextAddress += 1 + neoInsn.operand.length;
    }

    void removeLastInstruction() {
        // What about the currentLabel?
        NeoInstruction lastInsn = this.instructions.get(this.instructions.lastKey());
        if (this.jumpTargets.containsValue(lastInsn)) {
            throw new CompilerException("Attempting to remove an instruction that potentially is a "
                    + "jump target for jump instruction.");
        }
        this.instructions.remove(this.instructions.lastKey());
        this.jumpInstructions.remove(lastInsn);
        this.nextAddress -= (1 + lastInsn.operand.length);
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

    void finalizeMethod() {
        // Update the jump instructions with the correct target address offset.
        for (NeoJumpInstruction jumpInsn : this.jumpInstructions) {
            if (!this.jumpTargets.containsKey(jumpInsn.label)) {
                throw new CompilerException("Missing jump target for jump opcode "
                        + jumpInsn.opcode.name() + ", at source code line number " + jumpInsn.lineNr
                        + ".");
            }
            NeoInstruction destinationInsn = this.jumpTargets.get(jumpInsn.label);
            int offset = destinationInsn.address - jumpInsn.address;
            // It is assumed that the compiler makes use only of the wide (4-byte) jump opcodes. We
            // can therefore always use 4-byte operand.
            jumpInsn.setOperand(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
                    .putInt(offset).array());
        }
    }
}
