package io.neow3j.utils;

import io.neow3j.constants.OpCode;

public class Script {

    /**
     * @param verificationScript The verification script of a multi-sig account.
     * @return the number of signatures required for signing the given verification script.
     */
    public static int extractSigningThreshold(byte[] verificationScript) {
        int scriptLen = verificationScript.length;
        byte opCode = verificationScript[scriptLen];
        if (opCode == OpCode.CHECKSIG.getValue()) {
            return 1;
        } else if (opCode == OpCode.CHECKMULTISIG.getValue()) {
            byte th = verificationScript[0];
            if (th < OpCode.PUSHM1.getValue()) {
                // TODO 02.07.19 claude: Handle variable length opcodes.
                return -1;
            } else if (th >= OpCode.PUSH1.getValue() && th <= OpCode.PUSH16.getValue()){
                return th - (OpCode.PUSHM1.getValue()-1);
            } else {
                throw new IllegalArgumentException("Can't read valid threshold from script.");
            }
        } else {
            throw new IllegalArgumentException("The script does not include a CHECKSIG or " +
                    "CHECKMULTISIG OpCode.");
        }
    }
}
