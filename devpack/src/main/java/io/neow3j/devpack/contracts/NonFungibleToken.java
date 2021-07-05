package io.neow3j.devpack.contracts;

import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.Iterator;
import io.neow3j.devpack.Map;

public abstract class NonFungibleToken extends Token {

    /**
     * Returns an iterator that contains all of the token ids owned by the given owner.
     *
     * @param owner The hash of the owner.
     * @return the iterator.
     */
    public static native Iterator<ByteString> tokensOf(Hash160 owner);

    /**
     * Transfers the token with {@code tokenId} to the given address.
     *
     * @param to      The hash of the receiver.
     * @param tokenId The ID of the token to transfer.
     * @param data    Optional data. This data is passed to the {@code onNEP11Payment} method, if
     *                the receiver is a deployed contract.
     * @return True, if the transfer is successful. False, for example, if the token has more than
     * one owner.
     */
    public static native boolean transfer(Hash160 to, ByteString tokenId, Object data);

    /**
     * Returns an iterator that contains all token ids that exist on this contract.
     * <p>
     * This method is optional for the NEP-11 standard.
     *
     * @return a list of tokens that are minted on this contract.
     */
    public static native Iterator<ByteString> tokens();

    /**
     * Gets the properties of the token with {@code tokenId}.
     * <p>
     * This method is optional for the NEP-11 standard.
     *
     * @param tokenId the token id.
     * @return the properties of the token.
     */
    public static native Map<String, String> properties(ByteString tokenId);

}
