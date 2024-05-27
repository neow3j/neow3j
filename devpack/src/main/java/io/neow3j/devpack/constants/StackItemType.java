package io.neow3j.devpack.constants;

/**
 * The types that an item on the NeoVM stack can have.
 */
// These values can also be found in the neow3j core module (io.neow3j.types.StackItemType). Make sure to update it
// there too when things change.
public class StackItemType {
    /**
     * Represents any type.
     */
    public static final byte ANY = 0x00;

    /**
     * Represents a pointer.
     */
    public static final byte POINTER = 0x10;

    /**
     * Represents a boolean.
     */
    public static final byte BOOLEAN = 0x20;

    /**
     * Represents an integer.
     */
    public static final byte INTEGER = 0x21;

    /**
     * Represents a byte string (immutable).
     */
    public static final byte BYTE_STRING = 0x28;

    /**
     * Represents a buffer (mutable).
     */
    public static final byte BUFFER = 0x30;

    /**
     * Represents an array or complex object.
     */
    public static final byte ARRAY = 0x40;

    /**
     * Represents a struct.
     */
    public static final byte STRUCT = 0x41;

    /**
     * Represents an ordered collection of key-value pairs.
     */
    public static final byte MAP = 0x48;

    /**
     * Represents an interface used to interoperate with the outside of the VM.
     */
    public static final byte INTEROP_INTERFACE = 0x60;
}
