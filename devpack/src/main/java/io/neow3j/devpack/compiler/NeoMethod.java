package io.neow3j.devpack.compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class NeoMethod {

    SortedMap<Integer, NeoInstruction> instructions = new TreeMap<>();
    List<NeoVariable> variables = new ArrayList<>();
    Map<Integer, NeoVariable> variablesByJVMIndex = new HashMap<>();
    List<NeoVariable> parameters = new ArrayList<>();
    Map<Integer, NeoVariable> parametersByJVMIndex = new HashMap<>();

    public void addParameter(NeoVariable var) {
        parameters.add(var.index, var);
        this.parametersByJVMIndex.put(var.jvmIndex, var);
    }

    public void addVariable(NeoVariable var) {
        this.variables.add(var.index, var);
        this.variablesByJVMIndex.put(var.jvmIndex, var);
    }

    public NeoVariable getVariableByJVMIndex(int index) {
        return this.variablesByJVMIndex.get(index);
    }

    public NeoVariable getParameterByJVMIndex(int index) {
        return this.parametersByJVMIndex.get(index);
    }

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
