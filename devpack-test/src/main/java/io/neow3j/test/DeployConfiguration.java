package io.neow3j.test;

import io.neow3j.types.ContractParameter;

import java.util.HashMap;
import java.util.Map;

/**
 * Used for configuring the deployment of a contract under test.
 */
public class DeployConfiguration {

    private ContractParameter deployParam;

    private Map<String, String> substitutions = new HashMap<>();

    protected ContractParameter getDeployParam() {
        return deployParam;
    }

    protected Map<String, String> getSubstitutions() {
        return substitutions;
    }

    /**
     * Sets the parameter used on deployment of the contract.
     */
    public void setDeployParam(ContractParameter deployParam) {
        this.deployParam = deployParam;
    }

    /**
     * Sets the mapping from placeholder to substitution strings that will be used before the
     * contract is compiled.
     *
     * @param substitutions the substitutions.
     */
    public void setSubstitutions(Map<String, String> substitutions) {
        this.substitutions = substitutions;
    }

    /**
     * Sets a single mapping from placeholder to substitution that will be used before the contract
     * is compiled.
     *
     * @param substitution the substitution.
     */
    public void setSubstitution(String placeholder, String substitution) {
        substitutions.put(placeholder, substitution);
    }
}
