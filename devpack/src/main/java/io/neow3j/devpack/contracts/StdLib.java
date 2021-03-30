package io.neow3j.devpack.contracts;

import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.ContractInterface;
import io.neow3j.devpack.annotations.ContractHash;

@ContractHash("0xacce6fd80d44e1796aa0c2c625e9e4e0ce39efc0")
public class StdLib extends ContractInterface {

    /**
     * Serialize the given object to a byte string.
     * <p>
     * As an example, the following class will be serialized to the byte string below. We assume
     * that the class was instantiated with the values {@code true} and {@code 32069}.
     *
     * <pre>
     * static class MyClass {
     *     boolean b;
     *     int i;
     *
     *     public MyClass(boolean b, int i) {
     *         this.b = b;
     *         this.i = i;
     *     }
     * }
     * </pre>
     *
     * Serialized bytes:
     * <pre>
     * [0] StackItemType.ARRAY
     * [1] 0x02 // Number of field variables on the object
     * [2] StackItemType.BOOLEAN // type of first variable, could also be INTEGER
     * [3] 0x01 // byte size of the variable's value
     * [4] 0x01 // the variable's value
     * [5] StackItemType.INTEGER // type of second variable
     * [6] 0x02 // byte size of the variable's value
     * [7] 0x45 // part 1 of the variable's value (little-endian)
     * [8] 0x7D // part 2 of the variable's value (little-endian)
     * </pre>
     * @param source the object to serialize.
     * @return the serialized bytes.
     */
    public static native ByteString serialize(Object source);

    /**
     * Deserializes the given bytes. It is up to the developer to know what type to
     * expect from the deserialization.
     * <p>
     * See {@link StdLib#serialize(Object)} for an example mapping between object and serialized
     * byte array.
     *
     * @param source the bytes to deserialize.
     * @return the deserialized object.
     */
    public static native Object deserialize(ByteString source);


    /**
     * Serializes the given object to a JSON string.
     * <p>
     * Given the following class, the expected JSON string after serialization will look like
     * this: {@code ["hello world!", 42]}. I.e., the object's field variables are serialized
     * into a JSON array in the same order as they appear in the class definition.
     * <pre>
     *     class MyClass {
     *         String s;
     *         int i;
     *
     *         public MyClass(String s, int i) {
     *             this.s = s;
     *             this.i = i;
     *         }
     *     }
     * </pre>
     * @param obj The object to JSON-serialize.
     * @return the object as a JSON string.
     */
    public native static String jsonSerialize(Object obj);

    /**
     * Deserializes the given JSON-formatted string into an object.
     * <p>
     * Given the following class, the JSON string to deserialize into it would need to look like
     * this: {@code ["hello world!", 42]}. I.e., a JSON array is expected in which the class
     * field variables are given in the same order as they appear in the class definition.
     * <pre>
     * class MyClass {
     *     String s;
     *     int i;
     *
     *     public MyClass(String s, int i) {
     *         this.s = s;
     *         this.i = i;
     *     }
     * }
     * </pre>
     * @param json The string to deserialize.
     * @return The deserialized object.
     */
    public native static Object jsonDeserialize(String json);

    /**
     * Encodes the given bytes to a Base64 string.
     *
     * @param input The bytes to encode.
     * @return the encoded string.
     */
    public static native String base64Encode(ByteString input);

    /**
     * Decodes the given Base64-encoded string.
     *
     * @param input The Base64-encoded string.
     * @return the decoded byte string.
     */
    public static native ByteString base64Decode(String input);

    /**
     * Encodes the given byte string to a Base58 string.
     *
     * @param input The bytes to encode.
     * @return the encoded string.
     */
    public static native String base58Encode(ByteString input);

    /**
     * Decodes the given Base58-encoded string.
     *
     * @param input The Base58-encoded string.
     * @return the decoded bytes.
     */
    public static native ByteString base58Decode(String input);

    /**
     * Converts the given number to its string representation.
     * <p>
     * The hexadecimal representation uses the 2's complement to represent negative numbers. Always
     * the smallest possible multiple of 4-bits is used to represent the number. E.g., for -1 the
     * hex representation is 0xf (1111). For -8 it's 0x8 (1000). If we move out of the range [-8,
     * 7], four more bits are added, such that -9 is 0xf7 (1111 0111).
     *
     * @param i    The number
     * @param base The base to use for the string representation. Can be decimal (10) or hexadecimal
     *             (16).
     * @return the number as a string.
     */
    public static native String itoa(int i, int base);

    /**
     * Converts the given number string into an integer.
     *
     * @param s    The number string.
     * @param base The base to use for interpreting the string. Can be decimal (10) or hexadecimal
     *             (16).
     * @return the number.
     */
    public static native int atoi(String s, int base);

}
