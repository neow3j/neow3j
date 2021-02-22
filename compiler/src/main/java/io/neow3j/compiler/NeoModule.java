package io.neow3j.compiler;

import static java.lang.String.format;

import io.neow3j.constants.OpCode;
import io.neow3j.contract.NefFile.MethodToken;
import io.neow3j.devpack.annotations.MethodSignature;
import io.neow3j.devpack.annotations.OnDeployment;
import io.neow3j.devpack.annotations.OnVerification;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

public class NeoModule {

    // Holds this module's methods, mapping from method ID to {@link NeoMethod}; Used by the
    // compiler to quickly search for a method.
    private final Map<String, NeoMethod> methods = new HashMap<>();

    // Holds the same references to the methods as {@link NeoModule#methods} but in the order they
    // have been added to this module.
    private final List<NeoMethod> sortedMethods = new ArrayList<>();

    // Holds this module's events. The keys are the variable names used when defining the events
    // in the smart contract class.
    private final Map<String, NeoEvent> events = new HashMap<>();

    // An ordered list of static method calls that are referenced by their index in this list. Used
    // in CALLT instructions.
    private List<MethodToken> methodTokens = new ArrayList<>();

    public List<NeoEvent> getEvents() {
        return new ArrayList<>(events.values());
    }

    /**
     * Gets this module's methods in the order they were added.
     *
     * @return the methods.
     */
    public List<NeoMethod> getSortedMethods() {
        return sortedMethods;
    }

    /**
     * Gets the index of the given method token in the list of tokens.
     * <p>
     * The index is also the ID to be used in combination with the CALLT opcode.
     *
     * @param token The token to search for.
     * @return the index of the token or -1 if it doesn't exist.
     */
    public int getIndexOfMethodToken(MethodToken token) {
        int idx = 0;
        for (MethodToken t : methodTokens) {
            if (token.equals(t)) {
                return idx;
            }
            idx++;
        }
        return -1;
    }

    /**
     * Adds the given method token to this module's tokens and returns the tokens ID. If the token
     * is already present, it's current ID is returned.
     *
     * @param token The method token to add.
     * @return the ID of the added method token.
     */
    public int addMethodToken(MethodToken token) {
        int idx = getIndexOfMethodToken(token);
        if (idx != -1) {
            return idx;
        }
        methodTokens.add(token);
        return methodTokens.size() - 1;
    }

    public List<MethodToken> getMethodTokens() {
        return methodTokens;
    }

    public void addMethod(NeoMethod method) {
        if (method != null) {
            methods.put(method.getId(), method);
            sortedMethods.add(method);
        }
    }

    public void addMethods(List<NeoMethod> newMethods) {
        if (newMethods != null) {
            newMethods.forEach(this::addMethod);
        }
    }

    public void addEvent(NeoEvent event) {
        if (events.containsKey(event.getDisplayName())) {
            throw new CompilerException(format("Two events with the name '%s' are defined. Make "
                    + "sure that every event has a different name.", event.getDisplayName()));
        }
        events.put(event.getDisplayName(), event);
    }

    void finalizeModule() {
        checkForDuplicatesOfMethodSignatureAnnotations();
        int startAddress = 0;
        for (NeoMethod method : this.sortedMethods) {
            method.finalizeMethod();
            method.setStartAddress(startAddress);
            // At this point, the `nextAddress` should be set to one byte after the last
            // instruction byte of a method. So we can simply add this number to the current
            // start address and get the start address of the next method.
            startAddress += method.getLastAddress();
        }
        for (NeoMethod method : sortedMethods) {
            for (Entry<Integer, NeoInstruction> entry : method.getInstructions().entrySet()) {
                NeoInstruction insn = entry.getValue();
                // Currently we're only using OpCode.CALL_L. Using CALL instead of CALL_L might
                // lead to some savings in script size but will also require shifting
                // addresses of all following instructions.
                if (insn.getOpcode().equals(OpCode.CALL_L)) {
                    if (!(insn.getExtra() instanceof NeoMethod)) {
                        throw new CompilerException(format("Instruction with %s opcode is "
                                + "missing the reference to the called method. The jump address "
                                + "cannot be resolved.", OpCode.CALL_L.name()));
                    }
                    NeoMethod calledMethod = (NeoMethod) insn.getExtra();
                    int offset = calledMethod.getStartAddress()
                            - (method.getStartAddress() + entry.getKey());
                    insn.setOperand(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
                            .putInt(offset).array());
                }
            }
        }
    }

    private void checkForDuplicatesOfMethodSignatureAnnotations() {
        Set<MethodSignature> methodSigs = new HashSet<>();
        sortedMethods.stream().map(NeoMethod::getMethodSignatureAnnotation)
                .filter(Objects::nonNull)
                .forEach(sig -> {
                    if (methodSigs.contains(sig)) {
                        throw new CompilerException(format("There are multiple methods that are "
                                + "annotated as candidates for the '%s' method but only one is "
                                + "allowed.", sig.name()));
                    }
                    methodSigs.add(sig);
                });
    }

    int byteSize() {
        return sortedMethods.stream().map(NeoMethod::byteSize).reduce(Integer::sum).get();
    }

    /**
     * Concatenates all of this module's methods together into one script. Should only be called
     * after {@link NeoModule#finalizeModule()} becuase otherwise the {@link
     * NeoModule#sortedMethods} is not yet initialized.
     */
    byte[] toByteArray() {
        ByteBuffer b = ByteBuffer.allocate(byteSize());
        sortedMethods.forEach(m -> b.put(m.toByteArray()));
        return b.array();
    }

    public boolean hasMethod(String calledMethodId) {
        return methods.containsKey(calledMethodId);
    }

    public NeoMethod getMethod(String calledMethodId) {
        return methods.get(calledMethodId);
    }

}
