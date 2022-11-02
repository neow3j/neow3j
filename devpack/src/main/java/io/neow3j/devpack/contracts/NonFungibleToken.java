package io.neow3j.devpack.contracts;

import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.Iterator;
import io.neow3j.devpack.Map;

/**
 * This class holds the common methods of contracts that are compliant with the NEP-11 standard.
 * <p>
 * When this class is extended, the constructor of the extending class must take exactly one parameter of type
 * {@link Hash160} or a constant {@link String} and pass it to the {@code super()} call without any additional logic.
 */
public class NonFungibleToken extends Token {

    /**
     * Initializes an interface to a non-fungible token.
     * <p>
     * Use this constructor only with a string literal.
     *
     * @param contractHash the big-endian contract script hash.
     */
    public NonFungibleToken(String contractHash) {
        super(contractHash);
    }

    /**
     * Initializes an interface to a non-fungible token.
     *
     * @param contractHash the contract script hash.
     */
    public NonFungibleToken(Hash160 contractHash) {
        super(contractHash);
    }

    /**
     * Returns an iterator that contains all of the token ids owned by the given owner.
     *
     * @param owner the hash of the owner.
     * @return the iterator.
     */
    public native Iterator<ByteString> tokensOf(Hash160 owner);

    /**
     * Transfers the token with {@code tokenId} to the given address.
     *
     * @param to      the hash of the receiver.
     * @param tokenId the ID of the token to transfer.
     * @param data    optional data. This data is passed to the {@code onNEP11Payment} method, if the receiver is a
     *                deployed contract.
     * @return true if the transfer is successful. False, for example, if the token has more than one owner.
     */
    public native boolean transfer(Hash160 to, ByteString tokenId, Object data);

    /**
     * Returns an iterator that contains all token ids that exist on this contract.
     * <p>
     * This method is optional for the NEP-11 standard.
     *
     * @return a list of tokens that are minted on this contract.
     */
    public native Iterator<ByteString> tokens();

    /**
     * Gets the properties of the token with {@code tokenId}.
     * <p>
     * This method is optional for the NEP-11 standard.
     *
     * @param tokenId the token id.
     * @return the properties of the token.
     */
    public native Map<String, String> properties(ByteString tokenId);

}
