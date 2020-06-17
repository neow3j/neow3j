package io.neow3j.devpack.compiler;

import java.util.SortedMap;
import java.util.TreeMap;

public class NeoMethod {

    SortedMap<Integer, NeoInstruction> instructions = new TreeMap<>();

    public void addInstruction(NeoInstruction neoInsn) {
        this.instructions.put(neoInsn.address, neoInsn);
    }

    public byte[] toByteArray() {
        byte[] bytes = new byte[getByteSize()];
        int i = 0;
        for (NeoInstruction insn : this.instructions.values()) {
            byte[] insnBytes = insn.toByteArray();
            System.arraycopy(insnBytes, 0, bytes, i, insnBytes.length);
            i += insnBytes.length;
        }
        return bytes;
    }

    public int getByteSize() {
        return this.instructions.values().stream()
                .map(NeoInstruction::getByteSize)
                .reduce(Integer::sum).get();
    }
}
