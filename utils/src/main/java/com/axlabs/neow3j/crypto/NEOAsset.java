package com.axlabs.neow3j.crypto;

import org.bouncycastle.util.BigIntegers;

import java.math.BigDecimal;
import java.math.BigInteger;

public class NEOAsset {

    public static BigInteger toBigInt(String value) {
        if (value == null) {
            return BigInteger.ZERO;
        }
        return new BigDecimal(value).multiply(BigDecimal.valueOf(100000000)).toBigInteger();
    }

}
