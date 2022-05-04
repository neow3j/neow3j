package io.neow3j.devpack.constants;

public class ParameterType {

    public static final byte AnyType = 0x00;
    public static final byte BoolType = 0x10;
    public static final byte IntegerType = 0x11;
    public static final byte ByteArrayType = 0x12;
    public static final byte StringType = 0x13;
    public static final byte Hash160Type = 0x14;
    public static final byte Hash256Type = 0x15;
    public static final byte PublicKeyType = 0x16;
    public static final byte SignatureType = 0x17;
    public static final byte ArrayType = 0x20;
    public static final byte MapType = 0x22;
    public static final byte InteropInterfaceType = 0x30;
    public static final byte VoidType = (byte) 0xff;

}
