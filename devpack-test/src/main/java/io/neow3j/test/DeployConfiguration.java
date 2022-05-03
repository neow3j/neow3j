package io.neow3j.test;

import io.neow3j.transaction.AccountSigner;
import io.neow3j.types.ContractParameter;
import io.neow3j.wallet.Account;

import java.util.HashMap;
import java.util.Map;

/**
 * Used for configuring the deployment of a contract under test. Instantiate and return this class in a method
 * annotated with {@link DeployConfig} to configure the deployment of a specific contract under test.
 */
public class DeployConfiguration {

    private ContractParameter deployParam;

    private Map<String, String> substitutions = new HashMap<>();

    private AccountSigner signer;

    private Account[] signingAccounts;

    public DeployConfiguration() {
    }

    protected ContractParameter getDeployParam() {
        return deployParam;
    }

    /**
     * Sets the parameter used on deployment of the contract.
     *
     * @param deployParam the parameter passed tot the contracts deploy method.
     */
    public void setDeployParam(ContractParameter deployParam) {
        this.deployParam = deployParam;
    }

    protected Map<String, String> getSubstitutions() {
        return substitutions;
    }

    /**
     * Sets the mapping from placeholder to substitution strings that will be used before the contract is compiled.
     *
     * @param substitutions the substitutions.
     */
    public void setSubstitutions(Map<String, String> substitutions) {
        this.substitutions = substitutions;
    }

    protected AccountSigner getSigner() {
        return signer;
    }

    /**
     * Sets the signer that will be used on the deploy transaction.
     * <p>
     * If no signer is set, a default account will be used by the test framework.
     *
     * @param signer the signer.
     */
    public void setSigner(AccountSigner signer) {
        this.signer = signer;
    }

    protected Account[] getSigningAccounts() {
        return signingAccounts;
    }

    /**
     * Sets the accounts that are used for signing the deploy transaction, if the signer account set in
     * {@link DeployConfiguration#setSigner(AccountSigner)} is a multi-sig account. I.e., don't use this when the
     * signer is a single-sig account.
     *
     * @param accounts the signing accounts.
     */
    public void setSigningAccounts(Account... accounts) {
        signingAccounts = accounts;
    }

    /**
     * Sets a single mapping from placeholder to substitution that will be used before the contract is compiled.
     *
     * @param placeholder  the placeholder string.
     * @param substitution the substitution.
     */
    public void setSubstitution(String placeholder, String substitution) {
        substitutions.put(placeholder, substitution);
    }

}
