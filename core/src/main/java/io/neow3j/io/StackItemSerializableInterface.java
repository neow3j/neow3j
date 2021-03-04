package io.neow3j.io;

import io.neow3j.io.exceptions.DeserializationException;
import io.neow3j.protocol.core.methods.response.StackItem;

public interface StackItemSerializableInterface {

    void deserialize(StackItemReader reader) throws DeserializationException;

    static <T extends StackItemSerializable> T from(StackItem item, Class<T> t)
            throws DeserializationException {

        StackItemReader stackItemReader = new StackItemReader(item);
        return stackItemReader.readSerializable(t);
    }

}
