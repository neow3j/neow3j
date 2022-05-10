package io.neow3j.transaction;

import io.neow3j.constants.NeoConstants;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.serialization.BinaryReader;
import io.neow3j.serialization.BinaryWriter;
import io.neow3j.serialization.IOUtils;
import io.neow3j.serialization.NeoSerializable;
import io.neow3j.serialization.exceptions.DeserializationException;
import io.neow3j.transaction.exceptions.SignerConfigurationException;
import io.neow3j.transaction.witnessrule.CompositeCondition;
import io.neow3j.transaction.witnessrule.WitnessCondition;
import io.neow3j.transaction.witnessrule.WitnessRule;
import io.neow3j.types.Hash160;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static io.neow3j.constants.NeoConstants.MAX_SIGNER_SUBITEMS;
import static io.neow3j.transaction.witnessrule.WitnessCondition.MAX_NESTING_DEPTH;
import static java.lang.String.format;
import static java.util.Arrays.asList;

/**
 * A signer of a transaction. It defines a scope in which the signer's signature is valid.
 */
public class Signer extends NeoSerializable {

    /**
     * The script hash of the signer account.
     */
    private Hash160 signerHash;

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

    /**
     * The rules that the witness must meet.
     */
    private List<WitnessRule> rules;

    public Signer() {
    }

    protected Signer(Hash160 signerHash, WitnessScope scope) {
        this.signerHash = signerHash;
        scopes = new ArrayList<>();
        scopes.add(scope);
        allowedContracts = new ArrayList<>();
        allowedGroups = new ArrayList<>();
        rules = new ArrayList<>();
    }

    /**
     * Adds the given contracts to this signer's scope. These contracts are allowed to use the signers witness.
     *
     * @param allowedContracts the hashes of the allowed contracts.
     * @return this.
     */
    public Signer setAllowedContracts(Hash160... allowedContracts) {
        if (allowedContracts.length == 0) {
            return this;
        }
        if (scopes.contains(WitnessScope.GLOBAL)) {
            throw new SignerConfigurationException("Trying to set allowed contracts on a Signer with global scope.");
        }
        if (this.allowedContracts.size() + allowedContracts.length > MAX_SIGNER_SUBITEMS) {
            throw new SignerConfigurationException(
                    format("Trying to set more than %s allowed contracts on a signer.", MAX_SIGNER_SUBITEMS));
        }
        scopes.remove(WitnessScope.NONE); // remove the none witness scope if it is present.
        if (!scopes.contains(WitnessScope.CUSTOM_CONTRACTS)) {
            scopes.add(WitnessScope.CUSTOM_CONTRACTS);
        }
        this.allowedContracts.addAll(asList(allowedContracts));
        return this;
    }

    /**
     * Adds the given contract groups to this signer's scope. The contracts in these groups are allowed to use the
     * signers witness.
     *
     * @param allowedGroups the public keys of the allowed groups.
     * @return this.
     */
    public Signer setAllowedGroups(ECKeyPair.ECPublicKey... allowedGroups) {
        if (allowedGroups.length == 0) {
            return this;
        }
        if (scopes.contains(WitnessScope.GLOBAL)) {
            throw new SignerConfigurationException(
                    "Trying to set allowed contract groups on a Signer with global scope.");
        }
        if (this.allowedGroups.size() + allowedGroups.length > MAX_SIGNER_SUBITEMS) {
            throw new SignerConfigurationException(
                    format("Trying to set more than %s allowed contract groups on a signer.", MAX_SIGNER_SUBITEMS));
        }
        scopes.remove(WitnessScope.NONE); // remove the none witness scope if it is present.
        if (!scopes.contains(WitnessScope.CUSTOM_GROUPS)) {
            scopes.add(WitnessScope.CUSTOM_GROUPS);
        }
        this.allowedGroups.addAll(asList(allowedGroups));
        return this;
    }

    /**
     * Adds the given witness rules to this signer.
     *
     * @param rules the rules.
     * @return this.
     */
    public Signer setRules(WitnessRule... rules) {
        if (scopes.contains(WitnessScope.GLOBAL)) {
            throw new SignerConfigurationException("Trying to set witness rules on a Signer with global scope.");
        }
        if (this.rules.size() + rules.length > MAX_SIGNER_SUBITEMS) {
            throw new SignerConfigurationException(
                    format("Trying to set more than %s allowed witness rules on a signer.", MAX_SIGNER_SUBITEMS));
        }
        Arrays.stream(rules).forEach(r -> checkDepth(r.getCondition(), MAX_NESTING_DEPTH));
        scopes.remove(WitnessScope.NONE); // remove the none witness scope if it is present.
        if (!scopes.contains(WitnessScope.WITNESS_RULES)) {
            scopes.add(WitnessScope.WITNESS_RULES);
        }
        this.rules.addAll(asList(rules));
        return this;
    }

    private void checkDepth(WitnessCondition condition, int depth) {
        if (depth < 0) {
            throw new SignerConfigurationException(
                    format("A maximum nesting depth of %s is allowed for witness conditions.", MAX_NESTING_DEPTH));
        }
        if (condition instanceof CompositeCondition) {
            ((CompositeCondition) condition).getExpressions().forEach(c -> checkDepth(c, depth - 1));
        }
    }

    public Hash160 getScriptHash() {
        return signerHash;
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

    public List<WitnessRule> getRules() {
        return rules;
    }

    @Override
    public void deserialize(BinaryReader reader) throws DeserializationException {
        try {
            signerHash = reader.readSerializable(Hash160.class);
            scopes = WitnessScope.extractCombinedScopes(reader.readByte());
            if (scopes.contains(WitnessScope.CUSTOM_CONTRACTS)) {
                allowedContracts = reader.readSerializableList(Hash160.class);
                if (allowedContracts.size() > MAX_SIGNER_SUBITEMS) {
                    throw new DeserializationException(format("A signer's scope can only contain %s allowed contracts" +
                            ". The input data contained %s contracts.", MAX_SIGNER_SUBITEMS, allowedContracts.size()));
                }
            }
            if (scopes.contains(WitnessScope.CUSTOM_GROUPS)) {
                allowedGroups = reader.readSerializableList(ECKeyPair.ECPublicKey.class);
                if (allowedGroups.size() > MAX_SIGNER_SUBITEMS) {
                    throw new DeserializationException(format("A signer's scope can only contain %s allowed contract " +
                            "groups. The input data contained %s groups.", MAX_SIGNER_SUBITEMS, allowedGroups.size()));
                }
            }
            if (scopes.contains(WitnessScope.WITNESS_RULES)) {
                rules = reader.readSerializableList(WitnessRule.class);
                if (rules.size() > MAX_SIGNER_SUBITEMS) {
                    throw new DeserializationException(format("A signer's scope can only contain %s rules. The input " +
                            "data contained %s rules.", MAX_SIGNER_SUBITEMS, rules.size()));
                }
            }
        } catch (IOException e) {
            throw new DeserializationException(e);
        }
    }

    @Override
    public void serialize(BinaryWriter writer) throws IOException {
        writer.writeSerializableFixed(signerHash);
        writer.writeByte(WitnessScope.combineScopes(scopes));
        if (scopes.contains(WitnessScope.CUSTOM_CONTRACTS)) {
            writer.writeSerializableVariable(allowedContracts);
        }
        if (scopes.contains(WitnessScope.CUSTOM_GROUPS)) {
            writer.writeSerializableVariable(allowedGroups);
        }
        if (scopes.contains(WitnessScope.WITNESS_RULES)) {
            writer.writeSerializableVariable(rules);
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
        if (this.scopes.contains(WitnessScope.WITNESS_RULES)) {
            size += IOUtils.getVarSize(rules);
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
        return Objects.equals(this.signerHash, that.signerHash) &&
                Objects.equals(this.scopes, that.scopes) &&
                Objects.equals(this.allowedContracts, that.allowedContracts) &&
                Objects.equals(this.allowedGroups, that.allowedGroups) &&
                Objects.equals(this.rules, that.rules);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), signerHash, scopes, allowedContracts, allowedGroups, rules);
    }

}
