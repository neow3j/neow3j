package io.neow3j.transaction.witnessrule;

import io.neow3j.crypto.ECKeyPair;
import io.neow3j.serialization.BinaryReader;
import io.neow3j.serialization.BinaryWriter;
import io.neow3j.serialization.exceptions.DeserializationException;

import java.io.IOException;

/**
 * Provides the variable and methods used in witness conditions that contain a public key as value. I.e.,
 * {@link GroupCondition} and {@link CalledByGroupCondition}.
 */
public abstract class GroupTypeCondition extends WitnessCondition {

    protected ECKeyPair.ECPublicKey group;

    public ECKeyPair.ECPublicKey getGroup() {
        return group;
    }

    protected void deserializeWithoutType(BinaryReader reader) throws DeserializationException {
        group = reader.readSerializable(ECKeyPair.ECPublicKey.class);
    }

    @Override
    protected void serializeWithoutType(BinaryWriter writer) throws IOException {
        writer.writeSerializableFixed(group);
    }

    @Override
    public int getSize() {
        return super.getSize() + group.getSize();
    }

}
