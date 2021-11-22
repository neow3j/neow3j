package io.neow3j.transaction.witnessrule;

import io.neow3j.crypto.ECKeyPair;
import io.neow3j.serialization.BinaryReader;
import io.neow3j.serialization.BinaryWriter;
import io.neow3j.serialization.exceptions.DeserializationException;

import java.io.IOException;

/**
 * This condition defines that the calling contract must be part of the specified contract group.
 * I.e., a contract can only use the witness when it is invoked by a contract in the here
 * specified contract group.
 */
public class CalledByGroupCondition extends WitnessCondition {

    private ECKeyPair.ECPublicKey group;

    public CalledByGroupCondition() {
        type = WitnessConditionType.CALLED_BY_GROUP;
    }

    /**
     * Constructs a condition with the given group EC point/public key.
     *
     * @param group The group's EC point.
     */
    public CalledByGroupCondition(ECKeyPair.ECPublicKey group) {
        this.group = group;
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
        return group.getSize();
    }
}
