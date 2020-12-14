package io.neow3j.contract;

import io.neow3j.constants.InteropServiceCode;
import io.neow3j.constants.OpCode;
import io.neow3j.protocol.Neow3j;

/**
 * Represents the GasToken native contract and provides methods to invoke all its functions.
 */
public class GasToken extends Nep5Token {

    public final static String NAME = "GAS";
    public final static int DECIMALS = 8;
    public final static String SYMBOL = "gas";

    private static final byte[] SCRIPT = new ScriptBuilder()
            .pushData(NAME)
            .sysCall(InteropServiceCode.SYSTEM_CONTRACT_CALLNATIVE)
            .toArray();

    public static final ScriptHash SCRIPT_HASH = ScriptHash.fromScript(
            new ScriptBuilder()
                    .opCode(OpCode.ABORT)
                    .pushData(ScriptHash.ZERO.toArray())
                    .pushData(SCRIPT)
                    .toArray());

    /**
     * Constructs a new {@code GasToken} that uses the given {@link Neow3j} instance for
     * invocations.
     *
     * @param neow The {@link Neow3j} instance to use for invocations.
     */
    public GasToken(Neow3j neow) {
        super(SCRIPT_HASH, neow);
    }

    /**
     * Returns the name of the GasToken contract. Doesn't require a call to the neo-node.
     *
     * @return the name.
     */
    @Override
    public String getName() {
        return NAME;
    }

    /**
     * Returns the symbol of the GasToken contract. Doesn't require a call to the neo-node.
     *
     * @return the symbol.
     */
    @Override
    public String getSymbol() {
        return SYMBOL;
    }

    /**
     * Returns the number of decimals of the GAS token. Doesn't require a call to the neo-node.
     *
     * @return the number of decimals.
     */
    @Override
    public int getDecimals() {
        return DECIMALS;
    }
}
