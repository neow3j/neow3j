package io.neow3j.devpack.contracts;

import io.neow3j.devpack.Hash160;

public abstract class Token extends ContractInterface {

    /**
     * Gets the total supply of this token.
     *
     * @return the total supply.
     */
    public static native int totalSupply();

    /**
     * Gets the symbol of this token.
     *
     * @return the symbol.
     */
    public static native String symbol();

    /**
     * Gets the number of decimals of this token.
     *
     * @return the number of decimals.
     */
    public static native int decimals();

    /**
     * Gets the token balance of the given account.
     *
     * @param scriptHash The script hash of the account to get the balance for.
     * @return the account's balance.
     */
    public static native int balanceOf(Hash160 scriptHash);

}
