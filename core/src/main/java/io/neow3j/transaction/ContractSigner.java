package io.neow3j.transaction;

import io.neow3j.types.ContractParameter;
import io.neow3j.types.Hash160;

import java.util.Arrays;
import java.util.List;

/**
 * This signer represents a smart contract instead of a normal account. You can use this in transactions that require
 * the verification of a smart contract, e.g., if you want to withdraw tokens from a contract you own.
 * <p>
 * Using such a signer will make Neo call the {@code verify()} method of the corresponding contract.
 * <p>
 * Make sure to provide the necessary contract parameters if the contract's {@code verify()} method expects any.
 */
public class ContractSigner extends Signer {

    private List<ContractParameter> verifyParams;

    private ContractSigner(Hash160 contractHash, WitnessScope scope, ContractParameter... verifyParams) {
        super(contractHash, scope);
        this.verifyParams = Arrays.asList(verifyParams);
    }

    /**
     * Gets the parameters that are consumed by this contract signer's the {@code verify()} method.
     *
     * @return the verify parameters of this contract signer.
     */
    public List<ContractParameter> getVerifyParameters() {
        return verifyParams;
    }

    /**
     * Creates a signer for the given contract with a scope ({@link WitnessScope#CALLED_BY_ENTRY}) that only allows
     * the entry point contract to use this signer's witness.
     *
     * @param contractHash the script hash of the contract.
     * @param verifyParams the parameters to pass to the {@code verify()} method of the contract.
     * @return the signer.
     */
    public static ContractSigner calledByEntry(Hash160 contractHash, ContractParameter... verifyParams) {
        return new ContractSigner(contractHash, WitnessScope.CALLED_BY_ENTRY, verifyParams);
    }

    /**
     * Creates a signer for the given account with global witness scope ({@link WitnessScope#GLOBAL}).
     *
     * @param contractHash the script hash of the contract.
     * @param verifyParams the parameters to pass to the {@code verify()} method of the contract.
     * @return the signer.
     */
    public static ContractSigner global(Hash160 contractHash, ContractParameter... verifyParams) {
        return new ContractSigner(contractHash, WitnessScope.GLOBAL, verifyParams);
    }

}
