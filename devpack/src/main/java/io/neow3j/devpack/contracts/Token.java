package io.neow3j.devpack.contracts;

import io.neow3j.devpack.Hash160;

public abstract class Token extends ContractInterface {

    /**
     * @return the total supply of this token.
     */
    public static native int totalSupply();

    /**
     * @return the symbol of this token.
     */
    public static native String symbol();

    /**
     * @return the number of decimals of this token.
     */
    public static native int decimals();

    /**
     * Gets the token balance of the given account.
     *
     * @param scriptHash the script hash of the account to get the balance for.
     * @return the account's balance.
     */
    public static native int balanceOf(Hash160 scriptHash);

}
