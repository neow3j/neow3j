package io.neow3j.model;

public class NeoConfig {

    private static byte[] magic;
    private static byte addressVersion;
    private static long milliSecondsPerBlock;

    static {
        magic = new byte[]{0x4F, 0x45, 0x4E};
        addressVersion = 0x17;
        milliSecondsPerBlock = 15 * 1000;
    }

    public static byte[] magicNumber() {
        return magic;
    }

    public static byte addressVersion() {
        return addressVersion;
    }

    public static long milliSecondsPerBlock() {
        return milliSecondsPerBlock;
    }

    public static void setMagicNumber(byte[] magicNumber) {
        magic = magicNumber;
    }

    public static void setAddressVersion(byte version) {
        addressVersion = version;
    }

    public static void setMilliSecondsPerBlock(long milliSeconds) {
        milliSecondsPerBlock = milliSeconds;
    }

}
