package io.neow3j.devpack.contracts;

import io.neow3j.devpack.Hash160;

/**
 * This class holds the shared methods of contracts that are compliant with the NEP-11 or NEP-17 standard.
 */
public abstract class Token extends ContractInterface {

    public Token(Hash160 contractHash) {
        super(contractHash);
    }

    /**
     * @return the total supply of this token.
     */
    public native int totalSupply();

    /**
     * @return the symbol of this token.
     */
    public native String symbol();

    /**
     * @return the number of decimals of this token.
     */
    public native int decimals();

    /**
     * Gets the token balance of the given account.
     *
     * @param scriptHash the script hash of the account to get the balance for.
     * @return the account's balance.
     */
    public native int balanceOf(Hash160 scriptHash);

}
