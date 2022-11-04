package io.neow3j.devpack.contracts;

import io.neow3j.devpack.Hash160;

/**
 * Base class for contract interfaces that give convenient access to methods of a deployed contract. Initialize this
 * class with a contract hash ({@link Hash160}) to create an "interface" to a smart contract on the
 * Neo blockchain.
 * <p>
 * When this class is extended, the constructor of the extending class must take exactly one parameter of type
 * {@link Hash160} or a constant {@link String} and pass it to the {@code super()} call without any additional logic.
 */
public class ContractInterface {

    /**
     * Initializes an interface to a smart contract.
     * <p>
     * Use this constructor only with a string literal.
     *
     * @param contractHash the big-endian contract script hash.
     */
    public ContractInterface(String contractHash) {
    }

    /**
     * Initializes an interface to a smart contract.
     *
     * @param contractHash the contract script hash.
     */
    public ContractInterface(Hash160 contractHash) {
    }

    /**
     * @return the contract's script hash in little-endian order.
     */
    public native Hash160 getHash();

}
