package io.neow3j.contract;

import io.neow3j.protocol.Neow3j;
import io.neow3j.types.Hash160;

/**
 * Represents the GasToken native contract and provides methods to invoke its functions.
 */
public class GasToken extends FungibleToken {

    public final static String NAME = "GasToken";
    public static final Hash160 SCRIPT_HASH = calcNativeContractHash(NAME);

    public final static int DECIMALS = 8;
    public final static String SYMBOL = "GAS";

    /**
     * Constructs a new {@code GasToken} that uses the given {@link Neow3j} instance for invocations.
     *
     * @param neow the {@link Neow3j} instance to use for invocations.
     */
    public GasToken(Neow3j neow) {
        super(SCRIPT_HASH, neow);
    }

    /**
     * Returns the name of the GasToken contract.
     * <p>
     * Doesn't require a call to the Neo node.
     *
     * @return the name.
     */
    @Override
    public String getName() {
        return NAME;
    }

    /**
     * Returns the symbol of the GasToken contract.
     * <p>
     * Doesn't require a call to the Neo node.
     *
     * @return the symbol.
     */
    @Override
    public String getSymbol() {
        return SYMBOL;
    }

    /**
     * Returns the number of decimals of the GAS token.
     * <p>
     * Doesn't require a call to the Neo node.
     *
     * @return the number of decimals.
     */
    @Override
    public int getDecimals() {
        return DECIMALS;
    }

}
