package io.neow3j.transaction.witnessrule;

import io.neow3j.serialization.BinaryReader;
import io.neow3j.serialization.BinaryWriter;
import io.neow3j.serialization.NeoSerializable;
import io.neow3j.serialization.exceptions.DeserializationException;

import java.io.IOException;

/**
 * A witness condition specifies under which conditions a contract can make use of a witness.
 */
public abstract class WitnessCondition extends NeoSerializable {

    protected static final int MAX_SUBITEMS = 16;
    public static final int MAX_NESTING_DEPTH = 2;

    protected WitnessConditionType type;

    @Override
    public void deserialize(BinaryReader reader) throws DeserializationException {
        try {
            if (!WitnessConditionType.valueOf(reader.readByte()).equals(this.type)) {
                throw new DeserializationException("The deserialized type does not match the type information in the " +
                        "serialized data.");
            }
            deserializeWithoutType(reader);
        } catch (IOException e) {
            throw new DeserializationException(e);
        }
    }

    public static WitnessCondition deserializeWitnessCondition(BinaryReader reader) throws DeserializationException {
        try {
            WitnessConditionType type = WitnessConditionType.valueOf(reader.readByte());
            WitnessCondition a = type.conditionClass().newInstance();
            a.deserializeWithoutType(reader);
            return a;
        } catch (IOException | IllegalAccessException | InstantiationException e) {
            throw new DeserializationException(e);
        }
    }

    protected abstract void deserializeWithoutType(BinaryReader reader) throws IOException, DeserializationException;

    @Override
    public void serialize(BinaryWriter writer) throws IOException {
        writer.writeByte(type.byteValue());
        serializeWithoutType(writer);
    }

    protected abstract void serializeWithoutType(BinaryWriter writer) throws IOException;

    public WitnessConditionType getType() {
        return type;
    }

    @Override
    public int getSize() {
        return 1; // type byte
    }

    public abstract io.neow3j.protocol.core.witnessrule.WitnessCondition toDTO();

}
