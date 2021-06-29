package io.neow3j.devpack.contracts;

import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.List;
import io.neow3j.devpack.annotations.ContractHash;

/**
 * Base class for divisible, non-fungible token contracts that are compliant with the
 * NEP-11 standard. Extend this class in combination with the {@link ContractHash} annotation to
 * create an "interface" to a NEP-11 (divisible) token contract on the Neo blockchain.
 */
public class DivisibleNonFungibleToken extends NonFungibleToken {

    /**
     * Gets the owners of the token with {@code tokenId}.
     *
     * @param tokenId The token id.
     * @return a list of owners of the token.
     */
    public static native List<Hash160> ownersOf(ByteString tokenId);

    /**
     * Transfers an amount of the token with {@code tokenId}.
     *
     * @param from    The sender of the token amount.
     * @param to      The receiver of the token amount.
     * @param amount  The fraction amount to transfer.
     * @param tokenId The token id.
     * @param data    The data that is passed to the {@code onNEP11Payment} method if the receiver
     *                is a deployed contract.
     * @return whether the transfer was successful.
     */
    public static native boolean transfer(Hash160 from, Hash160 to, int amount,
            ByteString tokenId, Object data);

    /**
     * Gets the balance of the token with {@code tokenId} for the given account.
     *
     * @param owner   The account to get the balance from.
     * @param tokenId The token id.
     * @return the token balance of the given account.
     */
    public static native int balanceOf(Hash160 owner, ByteString tokenId);

}
