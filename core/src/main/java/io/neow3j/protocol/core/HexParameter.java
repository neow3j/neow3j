package io.neow3j.protocol.core;

import java.math.BigInteger;

/**
 * Represents a hex parameter. It takes either a String or BigInteger
 * and transforms to string hexadecimal.
 */
public interface HexParameter {

    static HexParameter valueOf(BigInteger paramAsBigInteger) {
        return new HexParameterNumber(paramAsBigInteger);
    }

    static HexParameter valueOf(String paramAsRawString) {
        return new HexParameterString(paramAsRawString);
    }

    String getHexValue();

}
