package io.neow3j.contract;

import io.neow3j.constants.OpCode;
import io.neow3j.utils.Numeric;

import java.math.BigInteger;

public class ScriptReader {

    public static BigInteger readInteger(byte[] input) {
        if (input[0] == OpCode.PUSHM1.getValue()) {
            return BigInteger.ONE.negate();
        } else if (input[0] == OpCode.PUSH0.getValue()) {
            return BigInteger.ZERO;
        } else if (input[0] >= OpCode.PUSH1.getValue() && input[0] <= OpCode.PUSH16.getValue()) {
            int base = (OpCode.PUSH1.getValue() - 1);
            return BigInteger.valueOf(input[0] - base);
        } else {
            return Numeric.toBigInt(readData(input));
        }
    }

    public static byte[] readData(byte[] input) {
        int dataLen;
        int dataBeginsIdx;
        if (input[0] <= OpCode.PUSHBYTES75.getValue()) {
            // Length of data is encoded in the first byte and is in range [0, 75]
            dataLen = input[0];
            dataBeginsIdx = 1;
        } else if (input[0] == OpCode.PUSHDATA1.getValue()) {
            // Length of data is encoded in the second byte and is in range [0, 255]
            dataLen = Numeric.toBigInt(new byte[]{input[1]}).intValue();
            dataBeginsIdx = 2;
        } else if (input[0] == OpCode.PUSHDATA2.getValue()) {
            // Length of data is encoded in the second and third byte and is in range [0, 65535]
            dataLen = Numeric.toBigInt(input, 1, 2).intValue();
            dataBeginsIdx = 3;
        } else{
            // Length of data is encoded in the second, third, fourth and fifth byte and is in
            // bigger than 65535.
            dataLen = Numeric.toBigInt(input, 1, 4).intValue();
            dataBeginsIdx = 5;
        }

        byte[] data = new byte[dataLen];
        System.arraycopy(input, dataBeginsIdx, data, 0, dataLen);
        return data;
    }
}
