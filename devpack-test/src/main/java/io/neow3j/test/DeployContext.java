package io.neow3j.test;

import io.neow3j.contract.SmartContract;
import io.neow3j.types.Hash256;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds information from the deployment of contracts that are under test.
 */
public class DeployContext {

    private Map<Class<?>, SmartContract> deployedContracts = new HashMap<>();

    private Map<Class<?>, Hash256> deployTxHashes = new HashMap<>();

    /**
     * Gets the {@code SmartContract} instance of the given contract class. This contains, e.g.,
     * the deployed contract's script hash.
     *
     * @param contractClass The class of the deployed contract.
     * @return the {@code SmartContract} instance of the deployed contract.
     */
    public SmartContract getDeployedContract(Class<?> contractClass) {
        return deployedContracts.get(contractClass);
    }

    protected void addDeployedContract(Class<?> contractClass, SmartContract contract) {
        deployedContracts.put(contractClass, contract);
    }

    /**
     * Gets the hash of the transaction in which the given contract was deployed.
     *
     * @param contractClass The class of the deployed contract.
     * @return the transaction hash.
     */
    public Hash256 getDeployTxHash(Class<?> contractClass) {
        return deployTxHashes.get(contractClass);
    }

    protected void addDeployTxHash(Class<?> contractClass, Hash256 tx) {
        deployTxHashes.put(contractClass, tx);
    }
}
