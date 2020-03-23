package io.neow3j.model.types;

import java.math.BigDecimal;
import java.math.BigInteger;

// TODO: Transform into subclass of Nep5Token.
public class GASAsset {

    public static final String NAME = "NEOGas";

    // TODO: This hash is not up to date. Use the following to retrieve the correct hash:
    //  ScriptHash.fromScript(new ScriptBuilder().sysCall(InteropServiceCode.NEO_NATIVE_TOKENS_GAS).toArray())
    public static final String HASH_ID = "a1760976db5fcdfab2a9930e8f6ce875b2d18225";

    public static BigInteger toBigInt(String value) {
        if (value == null) {
            return BigInteger.ZERO;
        }
        return new BigDecimal(value).multiply(BigDecimal.valueOf(100000000)).toBigInteger();
    }

}
