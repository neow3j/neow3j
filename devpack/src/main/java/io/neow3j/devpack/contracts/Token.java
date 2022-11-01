package io.neow3j.devpack.contracts;

import io.neow3j.devpack.Hash160;

/**
 * This class holds the shared methods of contracts that are compliant with the NEP-11 or NEP-17 standard.
 * <p>
 * When this class is extended, the constructor of the extending class must take exactly one parameter of type
 * {@link Hash160} or a constant {@link String} and pass it to the {@code super()} call without any additional logic.
 */
public class Token extends ContractInterface {

    /**
     * Initializes an interface to a token contract.
     * <p>
     * Use this constructor only with a string literal.
     *
     * @param contractHash the big-endian contract script hash.
     */
    public Token(String contractHash) {
        super(contractHash);
    }

    /**
     * Initializes an interface to a token contract.
     *
     * @param contractHash the contract script hash.
     */
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
