package io.neow3j.devpack.contracts;

import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.Iterator;

public abstract class NonFungibleToken extends Token {

    /**
     * Returns an iterator that contains all of the token ids owned by the given owner.
     *
     * @param owner The hash of the owner.
     * @return the iterator.
     */
    // TODO: Change byte[] to ByteString as soon as that class is available.
    public static native Iterator<byte[]> tokensOf(Hash160 owner);

    /**
     * Transfers the token with {@code tokenId} to the given address.
     *
     * @param to      The hash of the receiver.
     * @param tokenId The ID of the token to transfer.
     * @return True, if the transfer is successful. False, for example, if the token has more than
     * one owner.
     */
    // TODO: Change byte[] to ByteString as soon as that class is available.
    public static native boolean transfer(Hash160 to, byte[] tokenId);

}
