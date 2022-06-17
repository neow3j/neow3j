package io.neow3j.devpack.contracts;

import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.annotations.NativeContract;

import static io.neow3j.devpack.Helper.reverse;
import static io.neow3j.devpack.StringLiteralHelper.hexToBytes;
import static io.neow3j.devpack.constants.NativeContract.GasTokenScriptHash;

/**
 * Represents an interface to the native GAS token contract.
 */
@NativeContract
public class GasToken extends FungibleToken {

    public GasToken() {
        super(new Hash160(reverse(hexToBytes(GasTokenScriptHash).toByteArray())));
    }

}
