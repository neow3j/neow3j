package io.neow3j.transaction.witnessrule;

import io.neow3j.crypto.ECKeyPair;
import io.neow3j.serialization.BinaryReader;
import io.neow3j.serialization.BinaryWriter;
import io.neow3j.serialization.exceptions.DeserializationException;
import io.neow3j.transaction.Signer;

import java.io.IOException;


/**
 * This condition allows including or excluding a contract group (with the defined EC point)
 * from using the witness. This is the same as adding the group with
 * {@link Signer#setAllowedGroups(ECKeyPair.ECPublicKey...)}.
 */
public class GroupCondition extends WitnessCondition {

    private ECKeyPair.ECPublicKey group;

    public GroupCondition() {
        type = WitnessConditionType.GROUP;
    }

    /**
     * Constructs condition with the given group EC point/public key.
     *
     * @param group the group's EC point.
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
        return group.getSize();
    }

    public ECKeyPair.ECPublicKey getGroup() {
        return group;
    }
}
