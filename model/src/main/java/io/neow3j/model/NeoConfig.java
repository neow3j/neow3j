package io.neow3j.model;

public class NeoConfig {

    private static byte[] magic;
    private static byte addressVersion;
    private static long milliSecondsPerBlock;

    static {
        magic = new byte[]{0x4e, 0x45, 0x4F, 0x00};
        addressVersion = 0x17;
        milliSecondsPerBlock = 15 * 1000;
    }

    /**
     * Gets the configured magic number encoded in 4 bytes and little-endian format.
     *
     * @return the magic number
     */
    public static byte[] magicNumber() {
        return magic;
    }

    public static byte addressVersion() {
        return addressVersion;
    }

    public static long milliSecondsPerBlock() {
        return milliSecondsPerBlock;
    }

    /**
     * Sets the magic number on the configuration.
     * <p>
     * The argument must be 4 bytes in little-endian format. E.g., for the magic number 769 the
     * array [0x01, 0x03, 0x00, 0x00] must be used.
     *
     * @param magicNumber The magic number to set.
     */
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
