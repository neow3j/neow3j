package io.neow3j.compiler;

import static io.neow3j.compiler.Compiler.MAX_LOCAL_VARIABLES_COUNT;

import io.neow3j.constants.OpCode;

public class LocalVariableHelper {

    public static void addLoadLocalVariable(int varIndex, NeoMethod neoMethod) {
        addLoadOrStoreLocalVariable(varIndex, neoMethod, OpCode.LDARG, OpCode.LDLOC);
    }

    public static void addStoreLocalVariable(int varIndex, NeoMethod neoMethod) {
        addLoadOrStoreLocalVariable(varIndex, neoMethod, OpCode.STARG, OpCode.STLOC);
    }

    private static void addLoadOrStoreLocalVariable(int varIndex, NeoMethod neoMethod,
            OpCode argOpcode, OpCode varOpcode) {

        if (varIndex >= MAX_LOCAL_VARIABLES_COUNT) {
            throw new CompilerException("Local variable index to high. Was " + varIndex + " but "
                    + "maximally " + MAX_LOCAL_VARIABLES_COUNT + " local variables are supported.");
        }
        // The local variable can either be a method parameter or a normal variable defined in
        // the method body. The NeoMethod has been initialized with all the local variables.
        // Therefore, we can check here if it is a parameter or a normal variable and treat it
        // accordingly.
        NeoVariable param = neoMethod.getParameterByJVMIndex(varIndex);
        if (param != null) {
            neoMethod.addInstruction(buildStoreOrLoadVariableInsn(param.getNeoIndex(), argOpcode));
        } else {
            NeoVariable var = neoMethod.getVariableByJVMIndex(varIndex);
            neoMethod.addInstruction(buildStoreOrLoadVariableInsn(var.getNeoIndex(), varOpcode));
        }
    }

    public static NeoInstruction buildStoreOrLoadVariableInsn(int index, OpCode opcode) {
        NeoInstruction neoInsn;
        if (index <= 6) {
            OpCode storeCode = OpCode.get(opcode.getCode() - 7 + index);
            neoInsn = new NeoInstruction(storeCode);
        } else {
            byte[] operand = new byte[]{(byte) index};
            neoInsn = new NeoInstruction(opcode, operand);
        }
        return neoInsn;
    }
}
