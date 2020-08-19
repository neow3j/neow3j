package io.neow3j.transaction;

import io.neow3j.constants.NeoConstants;
import io.neow3j.contract.ScriptHash;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.Sign;
import io.neow3j.io.BinaryReader;
import io.neow3j.io.BinaryWriter;
import io.neow3j.io.IOUtils;
import io.neow3j.io.NeoSerializable;
import io.neow3j.io.exceptions.DeserializationException;
import io.neow3j.transaction.exceptions.SignerConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


/**
 * A Signer of a transaction. It also sets a scope in which the signer's
 * witness/signature is valid.
 */
public class Signer extends NeoSerializable {

    /**
     * The script hash of the signer account.
     */
    private ScriptHash account;

    /**
     * The scopes in which the signer's signatures can be used. Multiple scopes can be combined.
     */
    private List<WitnessScope> scopes;

    /**
     * The script hashes of contracts that are allowed to use the witness.
     */
    private List<ScriptHash> allowedContracts;

    /**
     * The group hashes of contracts that are allowed to use the witness.
     */
    private List<ECKeyPair.ECPublicKey> allowedGroups;

    public Signer() {
        this.account = new ScriptHash();
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
     * Creates a Signer for the given account with fee only witness scope ({@link
     * WitnessScope#FEE_ONLY}).
     *
     * @param account The originator of the witness.
     * @return {@link Signer}
     */
    public static Signer feeOnly(ScriptHash account) {
        return new Builder()
                .account(account)
                .scopes(WitnessScope.FEE_ONLY)
                .build();
    }

    /**
     * Creates a Signer for the given account with the most restrictive witness scope ({@link
     * WitnessScope#CALLED_BY_ENTRY}).
     *
     * @param account The originator of the witness.
     * @return {@link Signer}
     */
    public static Signer calledByEntry(ScriptHash account) {
        return new Builder()
                .account(account)
                .scopes(WitnessScope.CALLED_BY_ENTRY)
                .build();
    }

    /**
     * Creates a Signer for the given account with custom contract scope ({@link
     * WitnessScope#CUSTOM_CONTRACTS}).
     *
     * @param account The originator of the witness.
     * @return {@link Signer}
     */
    public static Signer customContract(ScriptHash account) {
        return new Builder()
                .account(account)
                .scopes(WitnessScope.CUSTOM_CONTRACTS)
                .build();
    }

    /**
     * Creates a Signer for the given account with custom group scope ({@link
     * WitnessScope#CUSTOM_GROUPS}).
     *
     * @param account The originator of the witness.
     * @return {@link Signer}
     */
    public static Signer customGroups(ScriptHash account) {
        return new Builder()
                .account(account)
                .scopes(WitnessScope.CUSTOM_GROUPS)
                .build();
    }

    /**
     * Creates a Signer for the given account with global witness scope ({@link
     * WitnessScope#GLOBAL}).
     *
     * @param account The originator of the witness.
     * @return {@link Signer}
     */
    public static Signer global(ScriptHash account) {
        return new Builder()
                .account(account)
                .scopes(WitnessScope.GLOBAL)
                .build();
    }

    public ScriptHash getScriptHash() {
        return account;
    }

    public List<WitnessScope> getScopes() {
        return scopes;
    }

    public List<ScriptHash> getAllowedContracts() {
        return allowedContracts;
    }

    public List<ECKeyPair.ECPublicKey> getAllowedGroups() {
        return allowedGroups;
    }

    @Override
    public void deserialize(BinaryReader reader) throws DeserializationException {
        try {
            this.account = reader.readSerializable(ScriptHash.class);
            this.scopes = WitnessScope.extractCombinedScopes(reader.readByte());
            if (this.scopes.contains(WitnessScope.CUSTOM_CONTRACTS)) {
                this.allowedContracts = reader.readSerializableList(ScriptHash.class);
                if (this.allowedContracts.size() > NeoConstants.MAX_SIGNER_SUBITEMS) {
                    throw new DeserializationException("A signer's scope can only contain "
                            + NeoConstants.MAX_SIGNER_SUBITEMS + " contracts. The input data "
                            + "contained " + this.allowedContracts.size() + " contracts.");
                }
            }
            if (this.scopes.contains(WitnessScope.CUSTOM_GROUPS)) {
                this.allowedGroups = reader.readSerializableList(ECKeyPair.ECPublicKey.class);
                if (this.allowedGroups.size() > NeoConstants.MAX_SIGNER_SUBITEMS) {
                    throw new DeserializationException("A signer's scope can only contain "
                            + NeoConstants.MAX_SIGNER_SUBITEMS + " groups. The input data "
                            + "contained " + this.allowedGroups.size() + " groups.");
                }
            }
        } catch (IOException e) {
            throw new DeserializationException(e);
        }
    }

    @Override
    public void serialize(BinaryWriter writer) throws IOException {
        writer.writeSerializableFixed(this.account);
        writer.writeByte(WitnessScope.combineScopes(this.scopes));
        if (scopes.contains(WitnessScope.CUSTOM_CONTRACTS)) {
            writer.writeSerializableVariable(this.allowedContracts);
        }
        if (scopes.contains(WitnessScope.CUSTOM_GROUPS)) {
            writer.writeSerializableVariable(this.allowedGroups);
        }
    }

    @Override
    public int getSize() {
        // Account script hash plus scope byte.
        int size = NeoConstants.SCRIPTHASH_SIZE + 1;
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
        if (!super.equals(o)) {
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

        private ScriptHash account;
        private List<WitnessScope> scopes;
        private List<ScriptHash> allowedContracts;
        private List<ECKeyPair.ECPublicKey> allowedGroups;

        public Builder() {
            this.scopes = new ArrayList<>();
            this.allowedContracts = new ArrayList<>();
            this.allowedGroups = new ArrayList<>();
        }

        /**
         * Sets the account for which this Signer object specifies the witness scopes.
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
         * When adding contracts here the {@link WitnessScope#CUSTOM_CONTRACTS} scope is added
         * automatically.
         *
         * @param contracts one or more contract script hashes.
         * @return this builder.
         */
        public Builder allowedContracts(ScriptHash... contracts) {
            if (!this.scopes.contains(WitnessScope.CUSTOM_CONTRACTS)) {
                this.scopes.add(WitnessScope.CUSTOM_CONTRACTS);
            }
            if (this.allowedContracts.size() + contracts.length
                    > NeoConstants.MAX_SIGNER_SUBITEMS) {
                throw new SignerConfigurationException("A signer's scope can only contain "
                        + NeoConstants.MAX_SIGNER_SUBITEMS + " contracts.");
            }
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
        public Builder allowedGroups(ECKeyPair.ECPublicKey... groups) {
            if (!this.scopes.contains(WitnessScope.CUSTOM_GROUPS)) {
                this.scopes.add(WitnessScope.CUSTOM_GROUPS);
            }
            if (this.allowedGroups.size() + groups.length > NeoConstants.MAX_SIGNER_SUBITEMS) {
                throw new SignerConfigurationException("A signer's scope can only contain "
                        + NeoConstants.MAX_SIGNER_SUBITEMS + " groups.");
            }
            this.allowedGroups.addAll(Arrays.asList(groups));
            return this;
        }

        /**
         * Builds the signer.
         *
         * @return the signer.
         * @throws SignerConfigurationException if either
         *                                        <ul>
         *                                          <li>no account has been set</li>
         *                                          <li>no scope has been set</li>
         *                                          <li>the global scope is mixed with other
         *                                          scopes</li>
         *                                          <li>the custom contracts scope is set but
         *                                          no contracts are specified</li>
         *                                          <li>the custom groups scope is set but
         *                                          no groups are specified</li>
         *                                        </ul>
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
