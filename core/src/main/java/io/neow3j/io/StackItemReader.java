package io.neow3j.io;

import io.neow3j.io.exceptions.DeserializationException;
import io.neow3j.protocol.core.methods.response.StackItem;

public class StackItemReader {

    private StackItem reader;
    private int position = 0;

    public StackItemReader(StackItem reader) {
        this.reader = reader;
    }

    public int getPosition() {
        return position;
    }

    public String asStructReadByteStringAsString() {
        String value = reader.asStruct().get(position).asByteString().getAsString();
        position++;
        return value;
    }

    public <T extends StackItemSerializable> T readSerializable(Class<T> t)
            throws DeserializationException {

        try {
            T obj = t.newInstance();
            obj.deserialize(this);
            return obj;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new DeserializationException(e);
        }
    }

}
