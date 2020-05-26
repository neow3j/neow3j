package io.neow3j.contract;

import io.neow3j.constants.InteropServiceCode;
import io.neow3j.protocol.Neow3j;

public class GasToken extends Nep5Token {

    public final static int DECIMALS = 8;
    public final static String NAME = "GAS";
    public final static String SYMBOL = "gas";
    public static final ScriptHash SCRIPT_HASH = ScriptHash.fromScript(
            new ScriptBuilder().sysCall(InteropServiceCode.NEO_NATIVE_TOKENS_GAS).toArray());

    // TODO: According to the dev guide (see
    //  https://github.com/neo-ngd/NEO3-Development-Guide/tree/master/en/SmartContract#gastoken)
    //  there is a function "getSysFeeAmount" on the GasToken contract, but at the time of
    //  writing neo-node did not support it.
    public static final String GET_SYSFEE_AMOUNT = "getSysFeeAmount";

    public GasToken(Neow3j neow) {
        super(SCRIPT_HASH, neow);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getSymbol() {
        return SYMBOL;
    }

    @Override
    public int getDecimals() {
        return DECIMALS;
    }

    public int getSystemFeeAmount() {
        throw new UnsupportedOperationException();
    }
}
