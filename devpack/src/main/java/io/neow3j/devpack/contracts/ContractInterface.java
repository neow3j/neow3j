package io.neow3j.devpack.contracts;

import io.neow3j.devpack.Hash160;

/**
 * Base class for contract interfaces that give convenient access to methods of a deployed contract. Initialize this
 * class with a contract hash ({@link Hash160}) to create an "interface" to a smart contract on the
 * Neo blockchain.
 * <p>
 * When this class is extended, the constructor of the extending class must take exactly one parameter of type
 * {@link Hash160} and pass it to the {@code super()} call without any additional logic.
 */
public abstract class ContractInterface {

    public ContractInterface(Hash160 contractHash) {
    }

    /**
     * @return the contract's script hash in little-endian order.
     */
    public native Hash160 getHash();

}
