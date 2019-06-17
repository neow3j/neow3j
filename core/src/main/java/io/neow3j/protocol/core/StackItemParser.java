package io.neow3j.protocol.core;

import io.neow3j.crypto.KeyUtils;
import io.neow3j.model.types.StackItem;
import io.neow3j.model.types.StackItemType;
import io.neow3j.utils.Numeric;

import java.math.BigInteger;

public class StackItemParser {

    public static String readAddress(StackItem parameter) {
        if (parameter.getType() == StackItemType.BYTE_ARRAY) {
            String param = String.valueOf(parameter.getValue());
            return param.isEmpty() ? "" : KeyUtils.scriptHashToAddress(param);
        }
        return null;
    }

    public static String readString(StackItem parameter) {
        switch (parameter.getType()) {
            case BYTE_ARRAY:
                String param = String.valueOf(parameter.getValue());
                return param.isEmpty() ? "" : Numeric.hexToString(param);
            default:
                return null;
        }
    }

    public static BigInteger readNumber(StackItem parameter) {
        String param = String.valueOf(parameter.getValue());

        if (param.isEmpty()) {
            return BigInteger.ZERO;
        }

        switch (parameter.getType()) {
            case BYTE_ARRAY:
                return Numeric.hexToInteger(param);
            case INTEGER:
                return new BigInteger(param);
            default:
                return null;
        }
    }


}
