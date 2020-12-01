package io.neow3j.compiler;

import static io.neow3j.compiler.Compiler.MAX_LOCAL_VARIABLES;
import static io.neow3j.compiler.Compiler.MAX_PARAMS_COUNT;
import static io.neow3j.compiler.Compiler.THIS_KEYWORD;
import static io.neow3j.utils.ClassUtils.getFullyQualifiedNameForInternalName;
import static java.lang.String.format;

import io.neow3j.constants.OpCode;
import io.neow3j.utils.ArrayUtils;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

/**
 * Represents a method in a NeoVM script.
 */
public class NeoMethod {

    // The ASM counterpart of this method.
    private final MethodNode asmMethod;

    // The type that contains this method.
    private final ClassNode sourceClass;

    // The method's name that is, e.g., used when generating the contract's ABI.
    private String name;

    // This method's instructions sorted by their address. The addresses in this map are only
    // relative to this method and not the whole `NeoModule` in which this method lives in.
    private SortedMap<Integer, NeoInstruction> instructions = new TreeMap<>();

    // This list contains those instructions that represent a jump, i.e. they remember a label to
    // which they need to jump. All instructions in this list are also present in the `instructions`
    // map.
    private List<NeoJumpInstruction> jumpInstructions = new ArrayList<>();

    // A mapping between labels - received from `LabelNodes` - and `NeoInstructions` used to keep
    // track of possible jump targets. This is needed when resolving jump addresses for
    // opcodes like JMPIF.
    private Map<Label, NeoInstruction> jumpTargets = new HashMap<>();

    // This list contains those instructions that represent the beginning of a try block.
    // All instructions in this list are also present in the `instructions` map.
    private List<NeoTryInstruction> tryInstructions = new ArrayList<>();

    // This method's local variables (excl. method parametrs).
    private SortedMap<Integer, NeoVariable> variablesByNeoIndex = new TreeMap<>();

    // Maps JVM bytecode indices to local variables.
    private SortedMap<Integer, NeoVariable> variablesByJVMIndex = new TreeMap<>();

    // This method's parameters.
    private SortedMap<Integer, NeoVariable> parametersByNeoIndex = new TreeMap<>();

    // Maps JVM bytecode indices to method parameters.
    private SortedMap<Integer, NeoVariable> parametersByJVMIndex = new TreeMap<>();

    // Determines if this method will show up in the contract's ABI.
    private boolean isAbiMethod = false;

    // The address after this method's last instruction byte. I.e. the next free address. This
    // address is not absolute in relation to the {@link NeoModule} this method belongs to. It is a
    // method-internal address.
    private int lastAddress = 0;

    // The address in the NeoModule at which this method starts.
    private Integer startAddress = null;

    // The current label of an instruction. Used in the compilation process to resolve jump
    // addresses. In contrast to `LineNumberNodes`, `LableNodes` are only applicable to the
    // very next instruction node.
    private Label currentLabel;

    // The current JVM instruction line number. Used in the compilation process to map line
    // numbers to `NeoInstructions`.
    private int currentLine;

    // Tells if the current line number should be added to an instruction that is added to this
    // method. If it is the first instruction corresponding to the current line, then the line
    // number is added to the instruction.
    private boolean isFreshNewLine = false;

    private List<TryCatchFinallyBlock> tryCatchFinallyBlocks = new ArrayList<>();

    /**
     * Constructs a new Neo method.
     *
     * @param asmMethod   The Java method this Neo method is converted from.
     * @param sourceClass The Java class from which this method originates.
     */
    public NeoMethod(MethodNode asmMethod, ClassNode sourceClass) {
        this.asmMethod = asmMethod;
        this.name = asmMethod.name;
        this.sourceClass = sourceClass;
        collectTryCatchBlocks(asmMethod.tryCatchBlocks);
    }

    // Sifts through the exception table of this method and constructs try-catch-finally blocks
    // that are later used to insert the corresponding instructions into the VM script.
    private void collectTryCatchBlocks(List<TryCatchBlockNode> blockNodes) {
        if (blockNodes == null || blockNodes.isEmpty()) {
            return;
        }
        checkForUnsupportedExceptionTypes(blockNodes);
        Set<TryCatchBlockNode> parsedNodes = collectBlocksWithCatchAndOptionallyFinally(blockNodes);
        collectBlocksWithNoCatchButFinally(blockNodes, parsedNodes);
    }

    private void checkForUnsupportedExceptionTypes(List<TryCatchBlockNode> blockNodes) {
        Optional<String> unsupportedException = blockNodes.stream()
                .map(node -> node.type)
                .filter(type -> type != null &&
                        !type.equals(Type.getType(Exception.class).getInternalName()))
                .findFirst();

        if (unsupportedException.isPresent()) {
            throw new CompilerException(sourceClass, format("Contract tries to catch an exception "
                            + "of type %s but only %s is supported.",
                    getFullyQualifiedNameForInternalName(unsupportedException.get()),
                    Exception.class.getCanonicalName()));
        }
    }

    // Go through blocks that have a try, catch, and optionally a finally block. Blocks that
    // have a try and finally only are processed later.
    private Set<TryCatchBlockNode> collectBlocksWithCatchAndOptionallyFinally(
            List<TryCatchBlockNode> blockNodes) {

        Set<TryCatchBlockNode> parsedNodes = new HashSet<>();
        for (TryCatchBlockNode block : blockNodes) {
            if (block.type != null) {
                parsedNodes.add(block);

                // Get the catch block for this try block.
                Optional<TryCatchBlockNode> catchBlockNode = blockNodes.stream()
                        .filter(b -> b.type == null && b.start == block.handler)
                        .findFirst();
                LabelNode endCatchLabelNode = null;
                if (catchBlockNode.isPresent()) {
                    parsedNodes.add(catchBlockNode.get());
                    endCatchLabelNode = catchBlockNode.get().end;
                }

                // Check if there is a finally block for this try block.
                Optional<TryCatchBlockNode> finallyBlockNode = blockNodes.stream()
                        .filter(b -> b.type == null && b.start == block.start && b.end == block.end)
                        .findFirst();
                LabelNode finallyLabelNode = null;
                if (finallyBlockNode.isPresent()) {
                    parsedNodes.add(finallyBlockNode.get());
                    finallyLabelNode = finallyBlockNode.get().handler;
                }

                tryCatchFinallyBlocks.add(new TryCatchFinallyBlock(block.start, block.end,
                        block.handler, endCatchLabelNode, finallyLabelNode));
            }
        }
        return parsedNodes;
    }

    // Filter for blocks that only have a try and finally part. Those blocks have not been
    // processed above, don't have a exception type set, and the start is not equal the handler.
    private void collectBlocksWithNoCatchButFinally(List<TryCatchBlockNode> blockNodes,
            Set<TryCatchBlockNode> parsedNodes) {

        blockNodes.stream().filter(blockNode -> !parsedNodes.contains(blockNode)
                && blockNode.type == null && blockNode.start != blockNode.handler)
                .forEach(block -> tryCatchFinallyBlocks.add(
                        new TryCatchFinallyBlock(block.start, block.end, null, null, block.handler)));
    }

    /**
     * Gets the corresponding JVM method that this method was converted from.
     *
     * @return the method.
     */
    public MethodNode getAsmMethod() {
        return asmMethod;
    }

    /**
     * Gets the class that this method is converted from.
     *
     * @return The class.
     */
    public ClassNode getOwnerClass() {
        return sourceClass;
    }

    /**
     * Gets the fully qualified name of the class that this method was converted from.
     *
     * @return the fully qualified name of the corresponding class.
     */
    public String getOwnerClassName() {
        return getFullyQualifiedNameForInternalName(sourceClass.name);
    }

    /**
     * Gets this method's ID, a string uniquely identifying this method. It includes the owner
     * type's name, the method's signature, and the method's name.
     *
     * @return this method's ID.
     */
    public String getId() {
        return getMethodId(asmMethod, sourceClass);
    }

    /**
     * Gets the name of this method. Used like this, e.g., in the contracts ABI.
     *
     * @return this method's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the name of the JVM method that this Neo method was derived from.
     * <p>
     * This will most often be equal to the name returned by {@link NeoMethod#getName()}.
     *
     * @return the name of the corresponding source method.
     */
    public String getSourceMethodName() {
        return asmMethod.name;
    }

    /**
     * Sets this method's name to the given string.
     *
     * @param name the method name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Creates a unique id for the given method used to identify this method in the {@link
     * NeoModule}.
     */
    public static String getMethodId(MethodNode asmMethod, ClassNode owner) {
        return owner.name + "." + asmMethod.name + asmMethod.desc;
    }

    /**
     * Gets the current line number that is currently being converted by the compiler.
     *
     * @return the current line number.
     */
    public int getCurrentLine() {
        return currentLine;
    }

    /**
     * Set the current line of this meethod to the given number.
     *
     * @param currentLine The current line to set.
     */
    public void setCurrentLine(int currentLine) {
        this.currentLine = currentLine;
        isFreshNewLine = true;
    }

    public void setCurrentLabel(Label currentLabel) {
        this.currentLabel = currentLabel;
    }


    /**
     * Gets this methods start address.
     * <p>
     * This address is set by the NeoModule when it is finalized. It is the absolute position of the
     * method inside of the module/script. The addresses of this method's instructions are only
     * relative and have to be used with the start address in order to get absolute addresses.
     *
     * @return the start address, or null if the corresponding NeoModule was not yet finalized.
     */
    public int getStartAddress() {
        return startAddress;
    }

    /**
     * Sets the given start address on this method.
     *
     * @param startAddress The address where this method begins inside of its NeoModule.
     */
    public void setStartAddress(int startAddress) {
        this.startAddress = startAddress;
    }


    /**
     * Checks if this method should show up in the ABI, i.e., is public and directly invocable from
     * outside of the smart contract.
     *
     * @return true if this method is an ABI method. False, otherwise.
     */
    public boolean isAbiMethod() {
        return isAbiMethod;
    }

    public void setIsAbiMethod(boolean abiMethod) {
        isAbiMethod = abiMethod;
    }

    /**
     * Gets the sorted instructions of this method. The map is sorted by instruction addresses,
     * i.e., the keys are the addresses.
     *
     * @return the instructions.
     */
    public SortedMap<Integer, NeoInstruction> getInstructions() {
        return instructions;
    }

    /**
     * Gets this method's variables sorted by their index. The index is the one these variables have
     * in the Neo script. It might defer from the one they have in the Java bytecode.
     *
     * @return the variables.
     */
    public SortedMap<Integer, NeoVariable> getVariablesByNeoIndex() {
        return variablesByNeoIndex;
    }

    /**
     * Gets this method's parameters sorted by their index. The index is the one these parameters
     * have in the Neo script. It might defer from the one they have in the Java bytecode.
     *
     * @return the prameters.
     */
    public SortedMap<Integer, NeoVariable> getParametersByNeoIndex() {
        return parametersByNeoIndex;
    }

    /**
     * Gets the address that follows this method's last instruction, i.e., the next free address.
     * This address is only absolute in context of this method but not in the whole module.
     *
     * @return the next instruction address.
     */
    public int getLastAddress() {
        return lastAddress;
    }

    /**
     * Adds a parameter to this method.
     *
     * @param var The parameter to add.
     */
    public void addParameter(NeoVariable var) {
        this.parametersByNeoIndex.put(var.getNeoIndex(), var);
        this.parametersByJVMIndex.put(var.getJvmIndex(), var);
    }

    /**
     * Adds a local variable to this method.
     */
    public void addVariable(NeoVariable var) {
        this.variablesByNeoIndex.put(var.getNeoIndex(), var);
        this.variablesByJVMIndex.put(var.getJvmIndex(), var);
    }

    /**
     * Gets the variable at the given index from this method in its JVM bytecode representation
     *
     * @return the variable.
     */
    public NeoVariable getVariableByJVMIndex(int index) {
        return this.variablesByJVMIndex.get(index);
    }

    /**
     * Gets the parameter at the given index from this method in its JVM bytecode representation
     *
     * @return the parameter.
     */
    public NeoVariable getParameterByJVMIndex(int index) {
        return this.parametersByJVMIndex.get(index);
    }

    /**
     * Converts the JVM instructions of this method to neo-vm instructions.
     *
     * @param compUnit The compilation unit.
     * @throws IOException If an error occurs when reading class files.
     */
    public void convert(CompilationUnit compUnit) throws IOException {
        AbstractInsnNode insn = asmMethod.instructions.get(0);
        while (insn != null) {
            insn = Compiler.handleInsn(insn, this, compUnit);
            insn = insn.getNext();
        }
        insertTryCatchBlocks();
    }

    private void insertTryCatchBlocks() {
        for (TryCatchFinallyBlock block : tryCatchFinallyBlocks) {
            insertTryInstruction(block);
        }
    }

    private void insertTryInstruction(TryCatchFinallyBlock block) {
        NeoInstruction insn = jumpTargets.get(block.tryLabelNode.getLabel());
        if (insn == null) {
            throw new CompilerException(sourceClass, "Could not find the beginning instruction of "
                    + "a try block.");
        }
        int addr = insn.getAddress();
        Label catchLabel = null;
        if (block.catchLabelNode != null) {
            catchLabel = block.catchLabelNode.getLabel();
        }
        Label finallyLabel = null;
        if (block.finallyLabelNode != null) {
            finallyLabel = block.finallyLabelNode.getLabel();
        }
        NeoTryInstruction tryInsn = new NeoTryInstruction(catchLabel, finallyLabel);
        insertInstruction(addr, tryInsn);
        tryInstructions.add(tryInsn);
    }

    // Adds the given instruction at the given address into the sorted instructions map of this
    // method. The address is set on the instruction as well. Shifts all instructions
    // with an address equal or bigger than the given address by the size of the new instructions.
    private void insertInstruction(int atAddr, NeoInstruction newInsn) {
        SortedMap<Integer, NeoInstruction> head = instructions.headMap(atAddr);
        SortedMap<Integer, NeoInstruction> tail = instructions.tailMap(atAddr);
        SortedMap<Integer, NeoInstruction> newMap = new TreeMap<>();
        newMap.putAll(head);
        newInsn.setAddress(atAddr);
        newMap.put(newInsn.getAddress(), newInsn);
        int shift = newInsn.byteSize();
        tail.forEach((i, insn) -> {
            insn.setAddress(i + shift);
            newMap.put(i + shift, insn);
        });
        instructions = newMap;
        increaseLastAddress(newInsn);
    }

    /**
     * Adds the given instruction to this method. The corresponding source code line number and the
     * instruction's address (relative to this method) is added to the instruction object.
     *
     * @param neoInsn The instruction to add.
     */
    public void addInstruction(NeoInstruction neoInsn) {
        if (isFreshNewLine) {
            neoInsn.setLineNr(currentLine);
            isFreshNewLine = false;
        }
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
        addInstructionInternal(neoInsn);
    }

    private void addInstructionInternal(NeoInstruction neoInsn) {
        neoInsn.setAddress(lastAddress);
        this.instructions.put(lastAddress, neoInsn);
        if (neoInsn instanceof NeoJumpInstruction) {
            this.jumpInstructions.add((NeoJumpInstruction) neoInsn);
        }
        increaseLastAddress(neoInsn);
    }

    private void increaseLastAddress(NeoInstruction neoInsn) {
        this.lastAddress += 1 + neoInsn.getOperand().length;
    }

    /**
     * Removes the last instruction from this method.
     *
     * @throws CompilerException if the instruction is a jump target.
     */
    public void removeLastInstruction() {
        NeoInstruction lastInsn = this.instructions.get(this.instructions.lastKey());
        if (this.jumpTargets.containsValue(lastInsn)) {
            throw new CompilerException(this, "Attempting to remove an instruction that is a jump "
                    + "target for another instruction.");
        }
        removeLastInstructionInternal();
    }

    private void removeLastInstructionInternal() {
        NeoInstruction insn = instructions.remove(instructions.lastKey());
        jumpInstructions.remove(insn);
        lastAddress -= (1 + insn.getOperand().length);
    }

    /**
     * Replaces the last instruction on this method with the given one. If the last instruction is a
     * jump target, i.e., has a label set, the label will be transferred to the new instruction.
     *
     * @param newInsn The replacement instruction.
     */
    public void replaceLastInstruction(NeoInstruction newInsn) {
        NeoInstruction lastInsn = this.instructions.get(this.instructions.lastKey());
        if (jumpTargets.containsValue(lastInsn)) {
            Optional<Entry<Label, NeoInstruction>> jumpTarget = jumpTargets.entrySet().stream()
                    .filter(e -> e.getValue() == lastInsn).findFirst();
            Label label = jumpTarget.get().getKey();
            jumpTargets.remove(label);
            jumpTargets.put(label, newInsn);
        }
        if (lastInsn.getLineNr() != null) {
            newInsn.setLineNr(lastInsn.getLineNr());
        }
        removeLastInstructionInternal();
        addInstructionInternal(newInsn);
    }

    /**
     * Gets the last instruction in this method.
     *
     * @return the last instruction.
     */
    public NeoInstruction getLastInstruction() {
        return this.instructions.get(this.instructions.lastKey());
    }

    /**
     * Serializes this method to a byte array, by serializing all its instructions ordered by
     * instruction address.
     *
     * @return the byte array.
     */
    public byte[] toByteArray() {
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
    protected int byteSize() {
        return this.instructions.values().stream()
                .map(NeoInstruction::byteSize)
                .reduce(Integer::sum).get();
    }

    protected void finalizeMethod() {
        setJumpInstructionOffsets();
        setTryInstructionOffsets();
    }

    // Updates the jump instructions with the correct target address offset.
    private void setJumpInstructionOffsets() {
        for (NeoJumpInstruction jumpInsn : this.jumpInstructions) {
            if (jumpInsn.getLabel() == null) {
                // If no label is set, we assume that the correct offset is already set.
                continue;
            }
            if (!this.jumpTargets.containsKey(jumpInsn.getLabel())) {
                throw new CompilerException(format("Missing jump target for opcode %s, at source "
                                + "code line number %d.", jumpInsn.getOpcode().name(),
                        jumpInsn.getLineNr()));
            }
            NeoInstruction destinationInsn = this.jumpTargets.get(jumpInsn.getLabel());
            int offset = destinationInsn.getAddress() - jumpInsn.getAddress();
            // It is assumed that the compiler makes use only of the wide (4-byte) jump opcodes. We
            // can therefore always use 4-byte operand.
            jumpInsn.setOperand(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
                    .putInt(offset).array());
        }
    }

    // Updates the try instructions (TRY_L) with the correct address offsets for the catch and
    // finally blocks.
    private void setTryInstructionOffsets() {
        for (NeoTryInstruction tryInsn : this.tryInstructions) {
            byte[] catchOffset = new byte[4];
            if (tryInsn.getCatchOffsetLabel() != null) {
                if (!this.jumpTargets.containsKey(tryInsn.getCatchOffsetLabel())) {
                    throw new CompilerException("Missing target instruction for catch block of a "
                            + "try block");
                }
                NeoInstruction destInsn = this.jumpTargets.get(tryInsn.getCatchOffsetLabel());
                int offset = destInsn.getAddress() - tryInsn.getAddress();
                catchOffset = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(offset)
                        .array();
            }

            byte[] finallyOffset = new byte[4];
            if (tryInsn.getFinallyOffsetLabel() != null) {
                if (!this.jumpTargets.containsKey(tryInsn.getFinallyOffsetLabel())) {
                    throw new CompilerException("Missing target instruction for finally block of a "
                            + "try block");
                }
                NeoInstruction destInsn = this.jumpTargets.get(tryInsn.getFinallyOffsetLabel());
                int offset = destInsn.getAddress() - tryInsn.getAddress();
                finallyOffset = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(offset)
                        .array();
            }

            tryInsn.setOperand(ArrayUtils.concatenate(catchOffset, finallyOffset));
        }
    }

    public void initializeMethod(CompilationUnit compUnit) {
        checkForUnsupportedLocalVariableTypes();
        if ((asmMethod.access & Opcodes.ACC_PUBLIC) > 0
                && (asmMethod.access & Opcodes.ACC_STATIC) > 0
                && compUnit.getContractClasses().contains(sourceClass)) {
            // Only contract methods that are public, static and on the smart contract class are
            // added to the ABI and are invokable.
            setIsAbiMethod(true);
        }

        // Look for method params and local variables and add them to the NeoMethod. Note that Java
        // mixes method params and local variables.
        if (asmMethod.maxLocals == 0) {
            return; // There are no local variables or parameters to process.
        }
        int nextVarIdx = collectMethodParameters();
        collectLocalVariables(nextVarIdx);

        // Add the INITSLOT opcode as first instruction of the method if the method has parameters
        // and/or local variables.
        if (variablesByNeoIndex.size() + parametersByNeoIndex.size() > 0) {
            addInstruction(new NeoInstruction(OpCode.INITSLOT, new byte[]{
                    (byte) variablesByNeoIndex.size(),
                    (byte) parametersByNeoIndex.size()}));
        }
    }

    private void checkForUnsupportedLocalVariableTypes() {
        for (LocalVariableNode varNode : asmMethod.localVariables) {
            if (Type.getType(varNode.desc) == Type.DOUBLE_TYPE
                    || Type.getType(varNode.desc) == Type.FLOAT_TYPE) {
                throw new CompilerException(this, format("Method '%s' has unsupported parameter or "
                        + "variable types.", asmMethod.name));
            }
        }
    }

    private void collectLocalVariables(int nextVarIdx) {
        int paramCount = Type.getArgumentTypes(asmMethod.desc).length;
        List<LocalVariableNode> locVars = asmMethod.localVariables;
        if (locVars.size() > 0 && locVars.get(0).name.equals(THIS_KEYWORD)) {
            paramCount++;
        }
        int localVarCount = asmMethod.maxLocals - paramCount;
        if (localVarCount > MAX_LOCAL_VARIABLES) {
            throw new CompilerException(format("The method '%s' has %d local variables but only a "
                            + "max of %d is supported.", getSourceMethodName(), localVarCount,
                    MAX_LOCAL_VARIABLES));
        }
        int neoIdx = 0;
        int jvmIdx = nextVarIdx;
        while (neoIdx < localVarCount) {
            // The variables' indices start where the parameters left off. Nonetheless, we need to
            // look through all local variables because the ordering is not necessarily according to
            // the indices.
            NeoVariable neoVar = null;
            for (LocalVariableNode varNode : locVars) {
                if (varNode.index == jvmIdx) {
                    neoVar = new NeoVariable(neoIdx, jvmIdx, varNode);
                    if (Type.getType(varNode.desc) == Type.LONG_TYPE) {
                        // Long vars/params use two index slots, i.e. we increment one more time.
                        jvmIdx++;
                    }
                    break;
                }
            }
            if (neoVar == null) {
                // Not all local variables show up in ASM's `localVariables` list, e.g. when a
                // String-based switch-case occurs.
                neoVar = new NeoVariable(neoIdx, jvmIdx, null);
            }
            addVariable(neoVar);
            jvmIdx++;
            neoIdx++;
        }
    }

    // Retruns the next index of the local variables after the method parameter slots.
    private int collectMethodParameters() {
        int paramCount = 0;
        List<LocalVariableNode> locVars = asmMethod.localVariables;
        if (locVars.size() > 0 && locVars.get(0).name.equals(THIS_KEYWORD)) {
            paramCount++;
        }
        paramCount += Type.getArgumentTypes(asmMethod.desc).length;
        if (paramCount > MAX_PARAMS_COUNT) {
            throw new CompilerException(format("The method '%s' has %d parameters but only a max "
                            + "of %d is supported.", getSourceMethodName(), paramCount,
                    MAX_PARAMS_COUNT));
        }
        int jvmIdx = 0;
        int neoIdx = 0;
        while (neoIdx < paramCount) {
            // The parameters' indices start at zero. Nonetheless, we need to look through all local
            // variables because the ordering is not necessarily according to the indices.
            for (LocalVariableNode varNode : locVars) {
                if (varNode.index == jvmIdx) {
                    addParameter(new NeoVariable(neoIdx, jvmIdx, varNode));
                    jvmIdx++;
                    neoIdx++;
                    if (Type.getType(varNode.desc) == Type.LONG_TYPE) {
                        // Long vars/params use two index slots, i.e. we increment one more time.
                        jvmIdx++;
                    }
                    break;
                }
            }
        }
        return jvmIdx;
    }

    private static class TryCatchFinallyBlock {

        private final LabelNode tryLabelNode;
        private final LabelNode endTryLabelNode;
        private final LabelNode catchLabelNode;
        private final LabelNode endCatchLabelNode;
        private final LabelNode finallyLabelNode;

        private TryCatchFinallyBlock(LabelNode tryLabelNode, LabelNode endTryLabelNode,
                LabelNode catchLabelNode, LabelNode endCatchLabelNode,
                LabelNode finallyLabelNode) {
            this.tryLabelNode = tryLabelNode;
            this.endTryLabelNode = endTryLabelNode;
            this.catchLabelNode = catchLabelNode;
            this.endCatchLabelNode = endCatchLabelNode;
            this.finallyLabelNode = finallyLabelNode;
        }
    }

}
