package io.neow3j.transaction.witnessrule;

import io.neow3j.serialization.BinaryReader;
import io.neow3j.serialization.BinaryWriter;
import io.neow3j.serialization.exceptions.DeserializationException;
import io.neow3j.types.Hash160;

import java.io.IOException;

/**
 * Provides the value variable and methods used for witness conditions that contain script hash values. I.e.,
 * {@link ScriptHashCondition} and {@link CalledByContractCondition}.
 */
public abstract class ScriptHashTypeCondition extends WitnessCondition {

    protected Hash160 scriptHash;

    public Hash160 getScriptHash() {
        return scriptHash;
    }

    protected void deserializeWithoutType(BinaryReader reader) throws DeserializationException {
        scriptHash = reader.readSerializable(Hash160.class);
    }

    protected void serializeWithoutType(BinaryWriter writer) throws IOException {
        writer.writeSerializableFixed(scriptHash);
    }

    @Override
    public int getSize() {
        return super.getSize() + scriptHash.getSize();
    }

}
