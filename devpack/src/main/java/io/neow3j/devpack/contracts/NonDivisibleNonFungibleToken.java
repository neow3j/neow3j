package io.neow3j.devpack.contracts;

import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.annotations.ContractHash;

/**
 * Base class for non-divisible, non-fungible token contracts that are compliant with the NEP-11
 * standard. Extend this class in combination with the {@link ContractHash} annotation to create an
 * "interface" to a NEP-11 (non-divisible) token contract on the Neo blockchain.
 */
public abstract class NonDivisibleNonFungibleToken extends NonFungibleToken {

    /**
     * Gets the owner of the token with {@code tokenId}.
     *
     * @param tokenId The token id.
     * @return the token owner.
     */
    public static native Hash160 ownerOf(ByteString tokenId);

}
