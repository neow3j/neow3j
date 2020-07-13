package io.neow3j.devpack.framework;

/**
 * <tt>StorageFlags</tt> define attributes of data that is written to a contract's storage. E.g. one
 * can define if data should be read-only.
 */
public class StorageFlag {

    public static final byte NONE = 0x00;
    public static final byte CONSTANT = 0x01;

}
