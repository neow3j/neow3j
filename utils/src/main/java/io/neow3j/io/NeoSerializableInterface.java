package io.neow3j.io;

import io.neow3j.io.exceptions.DeserializationException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public interface NeoSerializableInterface {

    void deserialize(BinaryReader reader) throws DeserializationException;

    void serialize(BinaryWriter writer) throws IOException;

    /**
     * Gets the size in bytes that this serializable will have in its serialized form.
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
        } catch (IllegalAccessException | InstantiationException | IOException e) {
            throw new DeserializationException(e);
        }
    }

    static <T extends NeoSerializable> List<T> fromAsList(byte[] value, Class<T> t)
        throws DeserializationException {

        try (ByteArrayInputStream ms = new ByteArrayInputStream(value)) {
            try (BinaryReader reader = new BinaryReader(ms)) {
                return reader.readSerializableListVarBytes(t);
            }
        } catch (IllegalAccessException | InstantiationException | IOException e) {
            throw new DeserializationException(e);
        }
    }

}
