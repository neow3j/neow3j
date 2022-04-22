package io.neow3j.transaction.witnessrule;

import io.neow3j.serialization.BinaryReader;
import io.neow3j.serialization.BinaryWriter;
import io.neow3j.serialization.exceptions.DeserializationException;
import io.neow3j.transaction.Signer;
import io.neow3j.types.Hash160;

import java.io.IOException;

/**
 * This condition allows including or excluding a specific contract (with the defined script hash) from using the
 * witness. This is the same as adding the contract to the scope of a {@link Signer} with
 * {@link Signer#setAllowedContracts(Hash160...)}.
 */
public class ScriptHashCondition extends WitnessCondition {

    private Hash160 contractHash;

    public ScriptHashCondition() {
        type = WitnessConditionType.SCRIPT_HASH;
    }

    /**
     * Constructs a witness condition of type {@link WitnessConditionType#SCRIPT_HASH} with the given contract script
     * hash.
     *
     * @param scriptHash the contract hash.
     */
    public ScriptHashCondition(Hash160 scriptHash) {
        this();
        this.contractHash = scriptHash;
    }

    protected void deserializeWithoutType(BinaryReader reader) throws DeserializationException {
        contractHash = reader.readSerializable(Hash160.class);
    }

    protected void serializeWithoutType(BinaryWriter writer) throws IOException {
        writer.writeSerializableFixed(contractHash);
    }

    @Override
    public int getSize() {
        return super.getSize() + contractHash.getSize();
    }

    public Hash160 getScriptHash() {
        return contractHash;
    }

    @Override
    public io.neow3j.protocol.core.witnessrule.WitnessCondition toJson() {
        return new io.neow3j.protocol.core.witnessrule.ScriptHashCondition(getScriptHash());
    }

}
