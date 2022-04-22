package io.neow3j.transaction.witnessrule;

import io.neow3j.crypto.ECKeyPair;
import io.neow3j.serialization.BinaryReader;
import io.neow3j.serialization.BinaryWriter;
import io.neow3j.serialization.exceptions.DeserializationException;
import io.neow3j.transaction.Signer;

import java.io.IOException;

/**
 * This condition allows including or excluding a contract group (with the defined public key) from using the witness.
 * This is the same as adding the group to the scope of a {@link Signer} with
 * {@link Signer#setAllowedGroups(ECKeyPair.ECPublicKey...)}.
 */
public class GroupCondition extends WitnessCondition {

    private ECKeyPair.ECPublicKey group;

    public GroupCondition() {
        type = WitnessConditionType.GROUP;
    }

    /**
     * Constructs a witness condition of type {@link WitnessConditionType#GROUP} with the given group's public key.
     *
     * @param group the group's public key.
     */
    public GroupCondition(ECKeyPair.ECPublicKey group) {
        this();
        this.group = group;
    }

    @Override
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
        return new io.neow3j.protocol.core.witnessrule.GroupCondition(getGroup());
    }

}
