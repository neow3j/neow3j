package io.neow3j.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public interface NeoSerializableInterface {

    void deserialize(BinaryReader reader) throws IOException;

    void serialize(BinaryWriter writer) throws IOException;

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

    static <T extends NeoSerializable> T from(byte[] value, Class<T> t) throws InstantiationException, IllegalAccessException {
        try (ByteArrayInputStream ms = new ByteArrayInputStream(value)) {
            try (BinaryReader reader = new BinaryReader(ms)) {
                return reader.readSerializable(t);
            }
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    static <T extends NeoSerializable> List<T> fromAsList(byte[] value, Class<T> t) throws InstantiationException, IllegalAccessException {
        try (ByteArrayInputStream ms = new ByteArrayInputStream(value)) {
            try (BinaryReader reader = new BinaryReader(ms)) {
                return reader.readSerializableListVarBytes(t);
            }
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

}
