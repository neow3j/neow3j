package io.neow3j.transaction;

import io.neow3j.constants.NeoConstants;
import io.neow3j.types.Hash160;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.serialization.BinaryReader;
import io.neow3j.serialization.BinaryWriter;
import io.neow3j.serialization.IOUtils;
import io.neow3j.serialization.NeoSerializable;
import io.neow3j.serialization.exceptions.DeserializationException;
import io.neow3j.transaction.exceptions.SignerConfigurationException;
import io.neow3j.wallet.Account;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.Arrays.asList;

/**
 * A signer of a transaction. It defines a scope in which the signer's signature is valid.
 */
public class Signer extends NeoSerializable {

    /**
     * The script hash of the signer account.
     */
    private Hash160 account;

    /**
     * The scopes in which the signer's signatures can be used. Multiple scopes can be combined.
     */
    private List<WitnessScope> scopes;

    /**
     * The contract hashes of the contracts that are allowed to use the witness.
     */
    private List<Hash160> allowedContracts;

    /**
     * The group hashes of contracts that are allowed to use the witness.
     */
    private List<ECKeyPair.ECPublicKey> allowedGroups;

    public Signer() {
        this.account = new Hash160();
        this.scopes = new ArrayList<>();
        this.allowedContracts = new ArrayList<>();
        this.allowedGroups = new ArrayList<>();
    }

    private Signer(Builder builder) {
        this.account = builder.account;
        this.scopes = builder.scopes;
        this.allowedContracts = builder.allowedContracts;
        this.allowedGroups = builder.allowedGroups;
    }

    /**
     * Creates a signer for the given account with fee only witness scope
     * ({@link WitnessScope#NONE}).
     *
     * @param account The signer account.
     * @return the signer.
     */
    public static Signer feeOnly(Account account) {
        return feeOnly(account.getScriptHash());
    }

    /**
     * Creates a signer for the given account with fee only witness scope
     * ({@link WitnessScope#NONE}).
     *
     * @param account The script hash of the signer account.
     * @return the signer.
     */
    public static Signer feeOnly(Hash160 account) {
        return new Builder()
                .account(account)
                .scopes(WitnessScope.NONE)
                .build();
    }

    /**
     * Creates a signer for the given account with the most restrictive witness scope
     * ({@link WitnessScope#CALLED_BY_ENTRY}).
     *
     * @param account The signer account.
     * @return the signer.
     */
    public static Signer calledByEntry(Account account) {
        return calledByEntry(account.getScriptHash());
    }

    /**
     * Creates a signer for the given account with the most restrictive witness scope
     * ({@link WitnessScope#CALLED_BY_ENTRY}).
     *
     * @param account The script hash of the signer account.
     * @return the signer.
     */
    public static Signer calledByEntry(Hash160 account) {
        return new Builder()
                .account(account)
                .scopes(WitnessScope.CALLED_BY_ENTRY)
                .build();
    }

    /**
     * Creates a signer for the given account with global witness scope
     * ({@link WitnessScope#GLOBAL}).
     *
     * @param account The account.
     * @return the signer.
     */
    public static Signer global(Account account) {
        return global(account.getScriptHash());
    }

    /**
     * Creates a signer for the given account with global witness scope
     * ({@link WitnessScope#GLOBAL}).
     *
     * @param account The account's script hash.
     * @return the signer.
     */
    public static Signer global(Hash160 account) {
        return new Builder()
                .account(account)
                .scopes(WitnessScope.GLOBAL)
                .build();
    }

    public Hash160 getScriptHash() {
        return account;
    }

    public List<WitnessScope> getScopes() {
        return scopes;
    }

    public List<Hash160> getAllowedContracts() {
        return allowedContracts;
    }

    public List<ECKeyPair.ECPublicKey> getAllowedGroups() {
        return allowedGroups;
    }

    @Override
    public void deserialize(BinaryReader reader) throws DeserializationException {
        try {
            account = reader.readSerializable(Hash160.class);
            scopes = WitnessScope.extractCombinedScopes(reader.readByte());
            if (scopes.contains(WitnessScope.CUSTOM_CONTRACTS)) {
                allowedContracts = reader.readSerializableList(Hash160.class);
                if (allowedContracts.size() > NeoConstants.MAX_SIGNER_SUBITEMS) {
                    throw new DeserializationException("A signer's scope can only contain "
                            + NeoConstants.MAX_SIGNER_SUBITEMS + " contracts. The input data "
                            + "contained " + allowedContracts.size() + " contracts.");
                }
            }
            if (scopes.contains(WitnessScope.CUSTOM_GROUPS)) {
                allowedGroups = reader.readSerializableList(ECKeyPair.ECPublicKey.class);
                if (allowedGroups.size() > NeoConstants.MAX_SIGNER_SUBITEMS) {
                    throw new DeserializationException("A signer's scope can only contain "
                            + NeoConstants.MAX_SIGNER_SUBITEMS + " groups. The input data "
                            + "contained " + allowedGroups.size() + " groups.");
                }
            }
        } catch (IOException e) {
            throw new DeserializationException(e);
        }
    }

    @Override
    public void serialize(BinaryWriter writer) throws IOException {
        writer.writeSerializableFixed(account);
        writer.writeByte(WitnessScope.combineScopes(scopes));
        if (scopes.contains(WitnessScope.CUSTOM_CONTRACTS)) {
            writer.writeSerializableVariable(allowedContracts);
        }
        if (scopes.contains(WitnessScope.CUSTOM_GROUPS)) {
            writer.writeSerializableVariable(allowedGroups);
        }
    }

    @Override
    public int getSize() {
        // Account script hash plus scope byte.
        int size = NeoConstants.HASH160_SIZE + 1;
        if (this.scopes.contains(WitnessScope.CUSTOM_CONTRACTS)) {
            size += IOUtils.getVarSize(this.allowedContracts);
        }
        if (this.scopes.contains(WitnessScope.CUSTOM_GROUPS)) {
            size += IOUtils.getVarSize(this.allowedGroups);
        }
        return size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Signer that = (Signer) o;
        return Objects.equals(this.account, that.account) &&
                Objects.equals(this.scopes, that.scopes) &&
                Objects.equals(this.allowedContracts, that.allowedContracts) &&
                Objects.equals(this.allowedGroups, that.allowedGroups);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), account, scopes, allowedContracts, allowedGroups);
    }

    public static class Builder {

        private Hash160 account;
        private List<WitnessScope> scopes;
        private List<Hash160> allowedContracts;
        private List<ECKeyPair.ECPublicKey> allowedGroups;

        public Builder() {
            this.scopes = new ArrayList<>();
            this.allowedContracts = new ArrayList<>();
            this.allowedGroups = new ArrayList<>();
        }

        /**
         * Sets the account for which this Signer object specifies the witness scopes.
         *
         * @param account The account.
         * @return this builder.
         */
        public Builder account(Account account) {
            this.account = account.getScriptHash();
            return this;
        }

        /**
         * Sets the account for which this Signer object specifies the witness scopes.
         *
         * @param account The account's script hash.
         * @return this builder.
         */
        public Builder account(Hash160 account) {
            this.account = account;
            return this;
        }

        /**
         * Sets the witness scopes in which the witness may be used.
         * <p>
         * Note that the global ({@link WitnessScope#GLOBAL}) scope cannot be mixed with any other
         * scopes.
         *
         * @param scopes One or more witness scopes.
         * @return this builder.
         */
        public Builder scopes(WitnessScope... scopes) {
            this.scopes.addAll(asList(scopes));
            return this;
        }

        /**
         * Sets the contracts that are allowed to use the witness.
         * <p>
         * When adding contracts here the {@link WitnessScope#CUSTOM_CONTRACTS} scope is added
         * automatically.
         *
         * @param contracts One or more contract script hashes.
         * @return this builder.
         */
        public Builder allowedContracts(Hash160... contracts) {
            if (!this.scopes.contains(WitnessScope.CUSTOM_CONTRACTS)) {
                this.scopes.add(WitnessScope.CUSTOM_CONTRACTS);
            }
            if (this.allowedContracts.size() + contracts.length
                    > NeoConstants.MAX_SIGNER_SUBITEMS) {
                throw new SignerConfigurationException("A signer's scope can only contain "
                        + NeoConstants.MAX_SIGNER_SUBITEMS + " contracts.");
            }
            this.allowedContracts.addAll(asList(contracts));
            return this;
        }

        /**
         * Sets the the groups that are allowed to use the witness.
         * <p>
         * When adding groups here the {@link WitnessScope#CUSTOM_GROUPS} scope is added
         * automatically.
         *
         * @param groups One or more group public keys as elliptic curve points.
         * @return this builder.
         */
        public Builder allowedGroups(ECKeyPair.ECPublicKey... groups) {
            if (!this.scopes.contains(WitnessScope.CUSTOM_GROUPS)) {
                this.scopes.add(WitnessScope.CUSTOM_GROUPS);
            }
            if (this.allowedGroups.size() + groups.length > NeoConstants.MAX_SIGNER_SUBITEMS) {
                throw new SignerConfigurationException("A signer's scope can only contain "
                        + NeoConstants.MAX_SIGNER_SUBITEMS + " groups.");
            }
            this.allowedGroups.addAll(asList(groups));
            return this;
        }

        /**
         * Builds the signer.
         *
         * @return The signer.
         * @throws SignerConfigurationException if either
         *                                      <ul>
         *                                        <li>no account has been set</li>
         *                                        <li>no scope has been set</li>
         *                                        <li>the global scope is mixed with other
         *                                        scopes</li>
         *                                        <li>the custom contracts scope is set but
         *                                        no contracts are specified</li>
         *                                        <li>the custom groups scope is set but
         *                                        no groups are specified</li>
         *                                      </ul>
         */
        public Signer build() {
            if (account == null) {
                throw new SignerConfigurationException("No account has been set. A signer" +
                        " object requires an account.");
            }
            if (scopes.isEmpty()) {
                throw new SignerConfigurationException("No scope has been defined. A signer" +
                        " object requires at least one scope.");
            }
            if (scopes.contains(WitnessScope.NONE) && scopes.size() > 1) {
                throw new SignerConfigurationException("The fee-only witness scope cannot be " +
                        "combined with other scopes.");
            }
            if (scopes.contains(WitnessScope.GLOBAL) && scopes.size() > 1) {
                throw new SignerConfigurationException("The global witness scope cannot be " +
                        "combined with other scopes.");
            }
            if (scopes.contains(WitnessScope.CUSTOM_CONTRACTS) && allowedContracts.isEmpty()) {
                throw new SignerConfigurationException("Set of allowed contracts must not be " +
                        "empty for a signer with the custom contracts scope.");
            }
            if (scopes.contains(WitnessScope.CUSTOM_GROUPS) && allowedGroups.isEmpty()) {
                throw new SignerConfigurationException("Set of allowed groups must not be " +
                        "empty for a signer with the custom groups scope.");
            }
            return new Signer(this);
        }

    }

}
