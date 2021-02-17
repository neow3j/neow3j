package io.neow3j.model;

public class NeoConfig {

    private static byte addressVersion;
    private static long milliSecondsPerBlock;

    static {
        addressVersion = 0x35;
        milliSecondsPerBlock = 15 * 1000;
    }

    public static byte addressVersion() {
        return addressVersion;
    }

    public static long milliSecondsPerBlock() {
        return milliSecondsPerBlock;
    }

    public static void setAddressVersion(byte version) {
        addressVersion = version;
    }

    public static void setMilliSecondsPerBlock(long milliSeconds) {
        milliSecondsPerBlock = milliSeconds;
    }

}
