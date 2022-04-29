package io.neow3j.transaction;

import io.neow3j.serialization.BinaryReader;
import io.neow3j.serialization.BinaryWriter;
import io.neow3j.serialization.NeoSerializable;
import io.neow3j.serialization.exceptions.DeserializationException;

import java.io.IOException;
import java.util.Objects;

public abstract class TransactionAttribute extends NeoSerializable {

    protected TransactionAttributeType type;

    public TransactionAttribute() {
    }

    public TransactionAttribute(TransactionAttributeType type) {
        if (type == null) {
            throw new IllegalArgumentException("Attribute type cannot be null.");
        }
        this.type = type;
    }

    public TransactionAttributeType getType() {
        return type;
    }

    @Override
    public int getSize() {
        // Byte size of attribute type plus the the size of the subclass.
        return 1 + getSizeWithoutType();
    }

    protected abstract int getSizeWithoutType();

    @Override
    public void deserialize(BinaryReader reader) throws DeserializationException {
        try {
            if (!TransactionAttributeType.valueOf(reader.readByte()).equals(this.type)) {
                throw new DeserializationException("The deserialized type does not match the type information in the " +
                        "serialized data.");
            }
            deserializeWithoutType(reader);
        } catch (IOException e) {
            throw new DeserializationException(e);
        }
    }

    protected abstract void deserializeWithoutType(BinaryReader reader)
            throws DeserializationException, IOException;

    @Override
    public void serialize(BinaryWriter writer) throws IOException {
        writer.writeByte(type.byteValue());
        serializeWithoutType(writer);
    }

    protected abstract void serializeWithoutType(BinaryWriter writer) throws IOException;

    public static TransactionAttribute deserializeAttribute(BinaryReader reader)
            throws DeserializationException {

        try {
            TransactionAttributeType type = TransactionAttributeType.valueOf(reader.readByte());
            TransactionAttribute a = type.clazz().newInstance();
            a.deserializeWithoutType(reader);
            return a;
        } catch (IOException | IllegalAccessException | InstantiationException e) {
            throw new DeserializationException(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TransactionAttribute)) {
            return false;
        }
        TransactionAttribute that = (TransactionAttribute) o;
        return getType() == that.getType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType());
    }

}
