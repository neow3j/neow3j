package io.neow3j.compiler;

import static io.neow3j.compiler.Compiler.MAX_LOCAL_VARIABLES_COUNT;
import static io.neow3j.compiler.Compiler.MAX_PARAMS_COUNT;
import static io.neow3j.compiler.Compiler.THIS_KEYWORD;

import io.neow3j.constants.OpCode;
import java.util.List;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.LocalVariableNode;

public class MethodInitializer {

    public static void initializeMethod(NeoMethod neoMethod, CompilationUnit compUnit) {
        checkForUnsupportedLocalVariableTypes(neoMethod);
        if ((neoMethod.asmMethod.access & Opcodes.ACC_PUBLIC) > 0
                && (neoMethod.asmMethod.access & Opcodes.ACC_STATIC) > 0
                && neoMethod.ownerType.equals(compUnit.getContractClassNode())) {
            // Only contract methods that are public, static and on the smart contract class are
            // added to the ABI and are invokable.
            neoMethod.isAbiMethod = true;
        }

        // Look for method params and local variables and add them to the NeoMethod. Note that Java
        // mixes method params and local variables.
        if (neoMethod.asmMethod.maxLocals == 0) {
            return; // There are no local variables or parameters to process.
        }
        int nextVarIdx = collectMethodParameters(neoMethod);
        collectLocalVariables(neoMethod, nextVarIdx);

        // Add the INITSLOT opcode as first instruction of the method if the method has parameters
        // and/or local variables.
        if (neoMethod.variablesByNeoIndex.size() + neoMethod.parametersByNeoIndex.size() > 0) {
            neoMethod.addInstruction(new NeoInstruction(
                    OpCode.INITSLOT, new byte[]{(byte) neoMethod.variablesByNeoIndex.size(),
                    (byte) neoMethod.parametersByNeoIndex.size()}));
        }
    }

    private static void checkForUnsupportedLocalVariableTypes(NeoMethod neoMethod) {
        for (LocalVariableNode varNode : neoMethod.asmMethod.localVariables) {
            if (Type.getType(varNode.desc) == Type.DOUBLE_TYPE
                    || Type.getType(varNode.desc) == Type.FLOAT_TYPE) {
                throw new CompilerException(neoMethod.ownerType, neoMethod.currentLine,
                        "Method '" + neoMethod.asmMethod.name + "' has unsupported parameter or "
                                + "variable types.");
            }
        }
    }

    private static void collectLocalVariables(NeoMethod neoMethod, int nextVarIdx) {
        int paramCount = Type.getArgumentTypes(neoMethod.asmMethod.desc).length;
        List<LocalVariableNode> locVars = neoMethod.asmMethod.localVariables;
        if (locVars.size() > 0 && locVars.get(0).name.equals(THIS_KEYWORD)) {
            paramCount++;
        }
        int localVarCount = neoMethod.asmMethod.maxLocals - paramCount;
        if (localVarCount > MAX_LOCAL_VARIABLES_COUNT) {
            throw new CompilerException("The method has more than the max number of local "
                    + "variables.");
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
            neoMethod.addVariable(neoVar);
            jvmIdx++;
            neoIdx++;
        }
    }

    // Retruns the next index of the local variables after the method parameter slots.
    private static int collectMethodParameters(NeoMethod neoMethod) {
        int paramCount = 0;
        List<LocalVariableNode> locVars = neoMethod.asmMethod.localVariables;
        if (locVars.size() > 0 && locVars.get(0).name.equals(THIS_KEYWORD)) {
            paramCount++;
        }
        paramCount += Type.getArgumentTypes(neoMethod.asmMethod.desc).length;
        if (paramCount > MAX_PARAMS_COUNT) {
            throw new CompilerException("The method has more than the max number of parameters.");
        }
        int jvmIdx = 0;
        int neoIdx = 0;
        while (neoIdx < paramCount) {
            // The parameters' indices start at zero. Nonetheless, we need to look through all local
            // variables because the ordering is not necessarily according to the indices.
            for (LocalVariableNode varNode : locVars) {
                if (varNode.index == jvmIdx) {
                    neoMethod.addParameter(new NeoVariable(neoIdx, jvmIdx, varNode));
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
}
