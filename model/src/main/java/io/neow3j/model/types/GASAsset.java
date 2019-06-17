package io.neow3j.model.types;

import java.math.BigDecimal;
import java.math.BigInteger;

public class GASAsset {

    public static final String NAME = "NEOGas";

    public static final String HASH_ID = "602c79718b16e442de58778e148d0b1084e3b2dffd5de6b7b16cee7969282de7";

    public static final AssetType TYPE = AssetType.UTILITY_TOKEN;

    public static BigInteger toBigInt(String value) {
        if (value == null) {
            return BigInteger.ZERO;
        }
        return new BigDecimal(value).multiply(BigDecimal.valueOf(100000000)).toBigInteger();
    }

}
