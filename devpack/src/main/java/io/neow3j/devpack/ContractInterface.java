package io.neow3j.devpack;

import io.neow3j.devpack.annotations.ContractHash;
import io.neow3j.devpack.contracts.NeoToken;
import io.neow3j.devpack.contracts.PolicyContract;

/**
 * Base class for contract interfaces that give convenient access to a deployed contract's methods.
 * Extend this class in combination with the {@link ContractHash} annotation to create an
 * "interface" to smart contract on the Neo blockchain. Examples are the {@link PolicyContract} and
 * {@link NeoToken} contracts.
 */
public abstract class ContractInterface {

    /**
     * Gets the contract's script hash in little-endian order. This requires the extending class to
     * use the {@link ContractHash} annotation.
     *
     * @return the contract's script hash.
     */
    public static native Hash160 getHash();

}
