package io.neow3j.contract;

import io.neow3j.protocol.Neow3j;
import java.math.BigDecimal;

public class GasToken extends Nep5Token {

    public final static int DECIMALS = 8;
    public final static BigDecimal FACTOR = BigDecimal.TEN.pow(DECIMALS);

    public GasToken(ScriptHash scriptHash, Neow3j neow) {
        super(scriptHash, neow);
    }

    @Override
    public int getDecimals() throws Exception {
        return DECIMALS;
    }
}
