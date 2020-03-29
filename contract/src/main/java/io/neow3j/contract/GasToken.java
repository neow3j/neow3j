package io.neow3j.contract;

import io.neow3j.constants.InteropServiceCode;
import io.neow3j.protocol.Neow3j;

public class GasToken extends Nep5Token {

    public final static int DECIMALS = 8;
    public final static String NAME = "GAS";
    public final static String SYMBOL = "gas";
    public static final ScriptHash SCRIPT_HASH = ScriptHash.fromScript(
            new ScriptBuilder().sysCall(InteropServiceCode.NEO_NATIVE_TOKENS_GAS).toArray());

    // TODO: Add method for the remaining function call. Not yet implemented in neo-core.
    //  See: https://github.com/neo-ngd/NEO3-Development-Guide/tree/master/en/SmartContract#gastoken
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
}
