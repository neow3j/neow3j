package io.neow3j.test;

import io.neow3j.contract.SmartContract;

import java.util.HashMap;
import java.util.Map;

public class DeployContext {

    private Map<Class<?>, SmartContract> deployedContracts = new HashMap<>();

    public SmartContract getDeployedContract(Class<?> contractClass) {
        return deployedContracts.get(contractClass);
    }

    protected void addDeployedContract(Class<?> contractClass, SmartContract contract) {
        deployedContracts.put(contractClass, contract);
    }
}
