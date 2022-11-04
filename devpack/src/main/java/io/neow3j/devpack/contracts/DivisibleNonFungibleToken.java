package io.neow3j.devpack.contracts;

import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.Iterator;

/**
 * Base class for divisible, non-fungible token contracts that are compliant with the NEP-11 standard. Initialize this
 * class with a contract hash ({@link Hash160}) to create an "interface" to a NEP-11 (divisible) token contract on
 * the Neo blockchain.
 * <p>
 * When this class is extended, the constructor of the extending class must take exactly one parameter of type
 * {@link Hash160} or a constant {@link String} and pass it to the {@code super()} call without any additional logic.
 */
public class DivisibleNonFungibleToken extends NonFungibleToken {

    /**
     * Initializes an interface to a divisible non-fungible token.
     * <p>
     * Use this constructor only with a string literal.
     *
     * @param contractHash the big-endian contract script hash.
     */
    public DivisibleNonFungibleToken(String contractHash) {
        super(contractHash);
    }

    /**
     * Initializes an interface to a divisible non-fungible token.
     *
     * @param contractHash the contract script hash.
     */
    public DivisibleNonFungibleToken(Hash160 contractHash) {
        super(contractHash);
    }

    /**
     * Transfers an amount of the token with {@code tokenId}.
     *
     * @param from    the sender of the token amount.
     * @param to      the receiver of the token amount.
     * @param amount  the fraction amount to transfer.
     * @param tokenId the token id.
     * @param data    the data that is passed to the {@code onNEP11Payment} method if the receiver is a deployed
     *                contract.
     * @return whether the transfer was successful.
     */
    public native boolean transfer(Hash160 from, Hash160 to, int amount, ByteString tokenId, Object data);

    /**
     * Returns an iterator that contains all owners of the token with {@code tokenId}.
     *
     * @param tokenId the token id.
     * @return a list of owners of the token.
     */
    public native Iterator<Hash160> ownerOf(ByteString tokenId);

    /**
     * Gets the balance of the token with {@code tokenId} for the given account.
     *
     * @param owner   the account to get the balance from.
     * @param tokenId the token id.
     * @return the token balance of the given account.
     */
    public native int balanceOf(Hash160 owner, ByteString tokenId);

}
