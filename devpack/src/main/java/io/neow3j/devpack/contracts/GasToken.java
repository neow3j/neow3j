package io.neow3j.devpack.contracts;

import io.neow3j.devpack.constants.NativeContract;

/**
 * Represents an interface to the native GasToken contract.
 */
public class GasToken extends FungibleToken {

    /**
     * Initializes an interface to the native GasToken contract.
     */
    public GasToken() {
        super(NativeContract.GasTokenScriptHash);
    }

}
