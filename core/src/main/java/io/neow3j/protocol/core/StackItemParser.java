package io.neow3j.protocol.core;

import io.neow3j.model.types.StackItemType;
import io.neow3j.protocol.core.methods.response.StackItem;
import io.neow3j.utils.Keys;
import io.neow3j.utils.Numeric;

import java.math.BigInteger;

public class StackItemParser {

    public static String readAddress(StackItem parameter) {
        if (parameter.getType() == StackItemType.BYTE_ARRAY) {
            String param = String.valueOf(parameter.getValue());
            return param.isEmpty() ? "" : Keys.scriptHashToAddress(param);
        }
        return null;
    }

    public static String readString(StackItem stackItem) {
        if (stackItem.getType().equals(StackItemType.BYTE_ARRAY)) {
            String param = String.valueOf(stackItem.getValue());
            return param.isEmpty() ? "" : Numeric.hexToString(param);
        }
        return null;
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
