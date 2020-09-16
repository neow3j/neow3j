package io.neow3j.devpack.neo;

import io.neow3j.constants.InteropServiceCode;
import io.neow3j.devpack.annotations.Syscall;

// TODO: Clarify on which objects this is usable.
/**
 * Offers methods to serialize to and deserialize from JSON.
 */
public class Json {

    /**
     * Serializes the given object to a JSON string.
     *
     * @param obj The object to JSON-serialize.
     * @return the object as a JSON string.
     */
    @Syscall(InteropServiceCode.SYSTEM_JSON_SERIALIZE)
    public native static String serialize(Object obj);

    /**
     * Deserializes the given JSON-formatted string into an object.
     *
     * @param json The string to deserialize.
     * @return The deserialized object.
     */
    @Syscall(InteropServiceCode.SYSTEM_JSON_DESERIALIZE)
    public native static Object deserialize(String json);

}
