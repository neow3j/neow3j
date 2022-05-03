package io.neow3j.serialization;

import io.neow3j.serialization.exceptions.DeserializationException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public interface NeoSerializableInterface {

    void deserialize(BinaryReader reader) throws DeserializationException;

    void serialize(BinaryWriter writer) throws IOException;

    /**
     * Gets the byte size of this serializable in serialized form. This includes possible size prefixes.
     *
     * @return the byte size.
     */
    int getSize();

    default byte[] toArray() {
        try (ByteArrayOutputStream ms = new ByteArrayOutputStream()) {
            try (BinaryWriter writer = new BinaryWriter(ms)) {
                serialize(writer);
                writer.flush();
                return ms.toByteArray();
            }
        } catch (IOException ex) {
            throw new UnsupportedOperationException(ex);
        }
    }

    static <T extends NeoSerializable> T from(byte[] value, Class<T> t)
            throws DeserializationException {

        try (ByteArrayInputStream ms = new ByteArrayInputStream(value)) {
            try (BinaryReader reader = new BinaryReader(ms)) {
                return reader.readSerializable(t);
            }
        } catch (IOException e) {
            throw new DeserializationException(e);
        }
    }

    static <T extends NeoSerializable> List<T> fromAsList(byte[] value, Class<T> t)
            throws DeserializationException {

        try (ByteArrayInputStream ms = new ByteArrayInputStream(value)) {
            try (BinaryReader reader = new BinaryReader(ms)) {
                return reader.readSerializableListVarBytes(t);
            }
        } catch (IOException e) {
            throw new DeserializationException(e);
        }
    }

}
