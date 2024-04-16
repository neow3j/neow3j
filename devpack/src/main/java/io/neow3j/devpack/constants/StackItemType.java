package io.neow3j.devpack.constants;

public class StackItemType {
    public static final byte ANY = 0x00;
    public static final byte POINTER = 0x10;
    public static final byte BOOLEAN = 0x20;
    public static final byte INTEGER = 0x21;
    public static final byte BYTE_STRING = 0x28;
    public static final byte BUFFER = 0x30;
    public static final byte ARRAY = 0x40;
    public static final byte STRUCT = 0x41;
    public static final byte MAP = 0x48;
    public static final byte INTEROP_INTERFACE = 0x60;
}
