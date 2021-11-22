package io.neow3j.transaction.witnessrule;

import io.neow3j.serialization.BinaryReader;
import io.neow3j.serialization.BinaryWriter;
import io.neow3j.serialization.exceptions.DeserializationException;
import io.neow3j.transaction.Signer;
import io.neow3j.types.Hash160;

import java.io.IOException;

/**
 * This condition allows including or excluding a specific contract (with the defined script hash)
 * from using the witness. This is the same as adding the contract with
 * {@link Signer#setAllowedContracts(Hash160...)}.
 */
public class ScriptHashCondition extends WitnessCondition {

    private Hash160 hash;

    public ScriptHashCondition() {
        type = WitnessConditionType.SCRIPT_HASH;
    }

    /**
     * Constructs condition with the given contract script hash.
     *
     * @param scriptHash the contract hash.
     */
    public ScriptHashCondition(Hash160 scriptHash) {
        this();
        this.hash = scriptHash;
    }

    protected void deserializeWithoutType(BinaryReader reader) throws DeserializationException {
        hash = reader.readSerializable(Hash160.class);
    }

    protected void serializeWithoutType(BinaryWriter writer) throws IOException {
        writer.writeSerializableFixed(hash);
    }

    @Override
    public int getSize() {
        return super.getSize() + hash.getSize();
    }

    public Hash160 getScriptHash() {
        return hash;
    }
}
