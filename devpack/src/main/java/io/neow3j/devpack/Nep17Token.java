package io.neow3j.devpack;

import io.neow3j.devpack.annotations.ContractScriptHash;

/**
 * Base class for NEP-17 contracts. Extend this class in combination with the {@link
 * ContractScriptHash} annotation to create an "interface" to a NEP-17 token contract on the Neo
 * blockchain. Examples are the {@link NEO} and {@link GAS} contracts.
 */
public abstract class Nep17Token extends ContractInterface {

    /**
     * Gets the symbol of the NEO token.
     *
     * @return the symbol.
     */
    public static native String symbol();

    /**
     * Gets the number of decimals of the NEO token, which is zero.
     *
     * @return the number of decimals.
     */
    public static native int decimals();

    /**
     * Gets the total supply of the NEO token.
     *
     * @return the total supply.
     */
    public static native int totalSupply();

    /**
     * Gets the NEO balance of the given account.
     *
     * @param scriptHash The script hash of the account to get the balance for.
     * @return the account's balance.
     */
    public static native int balanceOf(byte[] scriptHash);
}
