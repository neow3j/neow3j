package io.neow3j.contract;

import io.neow3j.constants.OpCode;
import io.neow3j.constants.OperandSize;
import io.neow3j.io.BinaryReader;
import io.neow3j.utils.Numeric;
import java.io.IOException;
import java.lang.annotation.Annotation;

/**
 * Reads Neo VM scripts and converts them to a more human-readable representation.
 */
public class ScriptReader {

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
                OperandSize operandSize = getOperandSize(code);
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
            return r.readShort();
        } else if (operandSize.prefixSize() == 4) {
            return r.readInt();
        } else {
            throw new UnsupportedOperationException("Only operand prefix sizes 1, 2, and 4 are "
                    + "supported, but got " + operandSize.prefixSize());
        }
    }

    private static OperandSize getOperandSize(OpCode code) {
        try {
            Annotation[] annotations = OpCode.class.getField(code.name()).getAnnotations();
            if (annotations.length == 0) {
                return null;
            }
            if (annotations[0].annotationType() != OperandSize.class) {
                throw new IllegalStateException("Unsupported annotation on OpCode.");
            }
            return (OperandSize) annotations[0];
        } catch (NoSuchFieldException ignore) {
            // Does not happen.
            return null;
        }
    }

}
