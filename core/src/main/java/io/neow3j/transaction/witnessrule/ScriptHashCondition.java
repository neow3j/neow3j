package io.neow3j.transaction.witnessrule;

import io.neow3j.serialization.BinaryReader;
import io.neow3j.serialization.BinaryWriter;
import io.neow3j.serialization.exceptions.DeserializationException;
import io.neow3j.types.Hash160;

import java.io.IOException;

public class ScriptHashCondition extends WitnessCondition {

    private Hash160 hash;

    public ScriptHashCondition() {
        type = WitnessConditionType.SCRIPT_HASH;
    }

    protected void deserializeWithoutType(BinaryReader reader) throws DeserializationException {
        hash = reader.readSerializable(Hash160.class);
    }

    protected void serializeWithoutType(BinaryWriter writer) throws IOException {
        writer.writeSerializableFixed(hash);
    }

    @Override
    public int getSize() {
        return hash.getSize();
    }
}
