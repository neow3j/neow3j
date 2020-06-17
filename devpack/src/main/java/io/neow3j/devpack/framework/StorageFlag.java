package io.neow3j.devpack.framework;

public enum StorageFlag {

    None((byte) 0x00),
    Constant((byte) 0x01);

    private byte code;

    StorageFlag(byte code) {
        this.code = code;
    }

    public byte getCode() {
        return code;
    }

}
