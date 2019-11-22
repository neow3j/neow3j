package io.neow3j.transaction;

import io.neow3j.contract.ScriptHash;
import io.neow3j.transaction.exceptions.CosignerConfigurationException;

import java.security.spec.ECPoint;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


/**
 * A Cosigner instance models witness scoping. It is part of a transaction and defines in which
 * scopes a specific witness can be used.
 */
public class Cosigner {

    /**
     * The script hash of the witness originator. I.e. the account that created the witness.
     */
    private ScriptHash account;

    /**
     * The scopes in which a transaction witness can be used. Multiple scopes can be combined.
     */
    private Set<WitnessScope> scopes;

    /**
     * The script hashes of contracts that are allowed to use the witness.
     */
    private Set<ScriptHash> allowedContracts;

    /**
     * The group hashes of contracts that are allowed to use the witness.
     */
    private Set<ECPoint> allowedGroups;

    private Cosigner(Builder builder) {
        this.account = builder.account;
        this.scopes = builder.scopes;
        this.allowedContracts = builder.allowedContracts;
        this.allowedGroups = builder.allowedGroups;
    }

    /**
     * Creates a Cosigner for the given account with the most restrictive witness scope ({@link
     * WitnessScope#CALLED_BY_ENTRY}).
     *
     * @param account The originator of the witness.
     */
    public static Cosigner calledByEntry(ScriptHash account) {
        return new Builder()
                .account(account)
                .scopes(WitnessScope.CALLED_BY_ENTRY)
                .build();
    }

    /**
     * Creates a Cosigner for the given account with global witness scope ({@link
     * WitnessScope#GLOBAL}).
     *
     * @param account The originator of the witness.
     */
    public static Cosigner global(ScriptHash account) {
        return new Builder()
                .account(account)
                .scopes(WitnessScope.GLOBAL)
                .build();
    }

    public ScriptHash getAccount() {
        return account;
    }

    public Set<WitnessScope> getScopes() {
        return scopes;
    }

    public Set<ScriptHash> getAllowedContracts() {
        return allowedContracts;
    }

    public Set<ECPoint> getAllowedGroups() {
        return allowedGroups;
    }

    public static class Builder {

        private ScriptHash account;
        private Set<WitnessScope> scopes;
        private Set<ScriptHash> allowedContracts;
        private Set<ECPoint> allowedGroups;

        public Builder() {
            this.scopes = new HashSet<>();
            this.allowedContracts = new HashSet<>();
            this.allowedGroups = new HashSet<>();
        }

        /**
         * Sets the account for which this Cosigner object sepcifies the witness scopes.
         *
         * @param account the account's script hash
         * @return this builder.
         */
        public Builder account(ScriptHash account) {
            this.account = account;
            return this;
        }

        /**
         * Sets the witness scopes in which the witness may be used.
         * <p>
         * Note that the global ({@link WitnessScope#GLOBAL}) scope cannot be mixed with any other
         * scopes.
         *
         * @param scopes one or more witness scopes.
         * @return this builder.
         */
        public Builder scopes(WitnessScope... scopes) {
            this.scopes.addAll(Arrays.asList(scopes));
            return this;
        }

        /**
         * Sets the contracts that are allowed to use the witness.
         * <p>
         * When adding contracts here the {@link WitnessScope#CUSTOM_CONSTRACTS} scope is added
         * automatically.
         *
         * @param contracts one or more contract script hashes.
         * @return this builder.
         */
        public Builder allowedContracts(ScriptHash... contracts) {
            this.scopes.add(WitnessScope.CUSTOM_CONSTRACTS);
            this.allowedContracts.addAll(Arrays.asList(contracts));
            return this;
        }

        /**
         * Sets the the groups that are allowed to use the witness.
         * <p>
         * When adding groups here the {@link WitnessScope#CUSTOM_GROUPS} scope is added
         * automatically.
         *
         * @param groups one or more group public keys as elliptic curve points.
         * @return this builder.
         */
        public Builder allowedGroups(ECPoint... groups) {
            this.scopes.add(WitnessScope.CUSTOM_GROUPS);
            this.allowedGroups.addAll(Arrays.asList(groups));
            return this;
        }

        public Cosigner build() {
            if (account == null) {
                throw new CosignerConfigurationException("No account has been set. A cosigner" +
                        " object requires an account.");
            }
            if (scopes.isEmpty()) {
                throw new CosignerConfigurationException("No scope has been defined. A cosigner" +
                        " object requires at least one scope.");
            }
            if (scopes.contains(WitnessScope.GLOBAL) && scopes.size() > 1) {
                throw new CosignerConfigurationException("The global witness scope cannot be " +
                        "combine with other scopes.");
            }
            if (scopes.contains(WitnessScope.CUSTOM_CONSTRACTS) && allowedContracts.isEmpty()) {
                throw new CosignerConfigurationException("Set of allowed contracts must not be " +
                        "empty for a cosigner with the custom contracts scope.");
            }
            if (scopes.contains(WitnessScope.CUSTOM_GROUPS) && allowedGroups.isEmpty()) {
                throw new CosignerConfigurationException("Set of allowed groups must not be " +
                        "empty for a cosigner with the custom groups scope.");
            }
            return new Cosigner(this);

        }

    }

}
