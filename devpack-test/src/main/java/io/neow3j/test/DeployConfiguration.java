package io.neow3j.test;

import io.neow3j.transaction.AccountSigner;
import io.neow3j.types.ContractParameter;
import io.neow3j.wallet.Account;

import java.util.HashMap;
import java.util.Map;

/**
 * Used for configuring the deployment of a contract under test.
 */
public class DeployConfiguration {

    private ContractParameter deployParam;

    private Map<String, String> substitutions = new HashMap<>();

    private AccountSigner signer;

    private Account[] signingAccounts;

    protected ContractParameter getDeployParam() {
        return deployParam;
    }

    protected Map<String, String> getSubstitutions() {
        return substitutions;
    }

    protected AccountSigner getSigner() {
        return signer;
    }

    protected Account[] getSigningAccounts() {
        return signingAccounts;
    }

    /**
     * Sets the parameter used on deployment of the contract.
     *
     * @param deployParam The parameter passed tot the contracts deploy method.
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
     * @param placeholder  The placeholder string.
     * @param substitution The substitution.
     */
    public void setSubstitution(String placeholder, String substitution) {
        substitutions.put(placeholder, substitution);
    }

    /**
     * Sets the signer that will be used on the deploy transaction.
     *
     * @param signer The signer.
     */
    public void setSigner(AccountSigner signer) {
        this.signer = signer;
    }

    /**
     * Sets the accounts that are used for signing the deploy transaction, if the signer account
     * set in {@link DeployConfiguration#setSigner(AccountSigner)} is a multi-sig account.
     *
     * @param accounts The signing accounts.
     */
    public void setSigningAccounts(Account... accounts) {
        signingAccounts = accounts;
    }
}
