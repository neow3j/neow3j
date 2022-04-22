package io.neow3j.transaction.witnessrule;

import io.neow3j.crypto.ECKeyPair;
import io.neow3j.serialization.BinaryReader;
import io.neow3j.serialization.BinaryWriter;
import io.neow3j.serialization.exceptions.DeserializationException;

import java.io.IOException;

/**
 * This condition defines that the calling contract must be part of the specified contract group. I.e., a contract
 * can only use the witness when it is invoked by a contract that is part of the here specified contract group.
 */
public class CalledByGroupCondition extends WitnessCondition {

    private ECKeyPair.ECPublicKey group;

    public CalledByGroupCondition() {
        type = WitnessConditionType.CALLED_BY_GROUP;
    }

    /**
     * Constructs a witness condition of type {@link WitnessConditionType#CALLED_BY_GROUP} with the given group's
     * public key.
     *
     * @param group the group's public key.
     */
    public CalledByGroupCondition(ECKeyPair.ECPublicKey group) {
        this();
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
        return super.getSize() + group.getSize();
    }

    public ECKeyPair.ECPublicKey getGroup() {
        return group;
    }

    @Override
    public io.neow3j.protocol.core.witnessrule.WitnessCondition toJson() {
        return new io.neow3j.protocol.core.witnessrule.CalledByGroupCondition(getGroup());
    }

}
