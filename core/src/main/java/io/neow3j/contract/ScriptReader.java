package io.neow3j.contract;

import io.neow3j.constants.InteropServiceCode;
import io.neow3j.constants.OpCode;
import io.neow3j.constants.OperandSize;
import io.neow3j.io.BinaryReader;
import io.neow3j.utils.Numeric;
import java.io.IOException;
import java.util.HashMap;

/**
 * Reads Neo VM scripts and converts them to a more human-readable representation.
 */
public class ScriptReader {

    /**
     * Gets the InteropService that creates the provided hash.
     *
     * @param hash The hash of the InteropServiceCode.
     * @return The InteropServiceCode matching the hash.
     */
    public static InteropServiceCode getInteropServiceCode(String hash) {
        HashMap<String, InteropServiceCode> interopServiceCodeMap = new HashMap<>();
        for (InteropServiceCode code : InteropServiceCode.values()) {
            interopServiceCodeMap.put(code.getHash(), code);
        }
        if (interopServiceCodeMap.containsKey(hash)) {
            return interopServiceCodeMap.get(hash);
        } else {
            throw new IllegalArgumentException("Code is not a valid InteropServiceCode Hash.");
        }
    }

    /**
     * Converts a Neo VM script into a string representation using OpCode names.
     *
     * @param script The script to convert in hexadecimal format.
     * @return the OpCode representation of the script.
     */
    public static String convertToOpCodeString(String script) {
        return convertToOpCodeString(Numeric.hexStringToByteArray(script));
    }

    /**
     * Converts a Neo VM script into a string representation using OpCode names.
     *
     * @param script The script to convert.
     * @return the OpCode representation of the script.
     */
    public static String convertToOpCodeString(byte[] script) {
        BinaryReader r = new BinaryReader(script);
        StringBuilder builder = new StringBuilder();
        try {
            while (r.getPosition() < script.length) {
                OpCode code = OpCode.get(r.readByte());
                builder.append(code.name());
                OperandSize operandSize = OpCode.getOperandSize(code);
                if (operandSize == null) {
                    builder.append("\n");
                    continue;
                }
                if (operandSize.size() > 0) {
                    builder.append(" ");
                    builder.append(Numeric.toHexStringNoPrefix(r.readBytes(operandSize.size())));
                } else if (operandSize.prefixSize() > 0) {
                    int size = getPrefixSize(r, operandSize);
                    builder.append(" ");
                    builder.append(size);
                    builder.append(" ");
                    builder.append(Numeric.toHexStringNoPrefix(r.readBytes(size)));
                }
                builder.append("\n");
            }
        } catch (IOException ignore) {
            // Does not happen.
        }
        return builder.toString();
    }

    private static int getPrefixSize(BinaryReader r, OperandSize operandSize) throws IOException {
        if (operandSize.prefixSize() == 1) {
            return r.readUnsignedByte();
        } else if (operandSize.prefixSize() == 2) {
            return r.readInt16();
        } else if (operandSize.prefixSize() == 4) {
            return r.readInt32();
        } else {
            throw new UnsupportedOperationException("Only operand prefix sizes 1, 2, and 4 are "
                    + "supported, but got " + operandSize.prefixSize());
        }
    }

}
