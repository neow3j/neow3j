package io.neow3j.devpack.contracts;

import io.neow3j.devpack.Hash160;

/**
 * Base class for fungible token contracts that are compliant with the NEP-17 standard. Initialize this class with a
 * contract hash ({@link Hash160}) to create an "interface" to a NEP-17 token contract on the Neo blockchain.
 * <p>
 * When this class is extended, the constructor of the extending class must take exactly one parameter of type
 * {@link Hash160} or a constant {@link String} and pass it to the {@code super()} call without any additional logic.
 */
public class FungibleToken extends Token {

    /**
     * Initializes an interface to a fungible token.
     * <p>
     * Use this constructor only with a string literal.
     *
     * @param contractHash the big-endian contract script hash.
     */
    public FungibleToken(String contractHash) {
        super(contractHash);
    }

    /**
     * Initializes an interface to a fungible token.
     *
     * @param contractHash the contract script hash.
     */
    public FungibleToken(Hash160 contractHash) {
        super(contractHash);
    }

    /**
     * Transfers the token {@code amount} from the {@code from} account to the {@code to} account. The {@code data}
     * is passed along with the call. For example, if the {@code to} account is a contract it is provided as an
     * argument to the contract's {@code onPayment} method.
     *
     * @param from   the hash of the payment originator.
     * @param to     the hash of the receiver.
     * @param amount the token amount to transfer.
     * @param data   the data to pass along with the transfer.
     * @return true if the transfer was successful. False, otherwise.
     */
    public native boolean transfer(Hash160 from, Hash160 to, int amount, Object data);

}
