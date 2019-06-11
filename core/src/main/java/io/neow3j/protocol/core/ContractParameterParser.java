package io.neow3j.protocol.core;

import io.neow3j.crypto.KeyUtils;
import io.neow3j.model.types.ContractParameter;
import io.neow3j.model.types.ContractParameterType;
import io.neow3j.utils.Numeric;

import java.math.BigInteger;

public class ContractParameterParser {

    public static String readAddress(ContractParameter parameter) {
        if (parameter.getParamType() == ContractParameterType.BYTE_ARRAY) {
            String param = String.valueOf(parameter.getValue());
            return param.isEmpty() ? "" : KeyUtils.scriptHashToAddress(param);
        }
        return null;
    }

    public static String readString(ContractParameter parameter) {
        switch (parameter.getParamType()) {
            case BYTE_ARRAY:
                String param = String.valueOf(parameter.getValue());
                return param.isEmpty() ? "" : Numeric.hexToString(param);
            default:
                return null;
        }
    }

    public static BigInteger readNumber(ContractParameter parameter) {
        String param = String.valueOf(parameter.getValue());

        if (param.isEmpty()) {
            return BigInteger.ZERO;
        }

        switch (parameter.getParamType()) {
            case BYTE_ARRAY:
                return Numeric.hexToInteger(param);
            case INTEGER:
                return new BigInteger(param);
            default:
                return null;
        }
    }


}
