package io.neow3j.devpack.framework;

/**
 * Defines attributes of data written to a contract's storage. E.g. one can define if data should be
 * read-only.
 */
public class StorageFlag {

    public static final byte NONE = 0x00;

    /**
     * Marks the stored data as constant, meaning that it cannot be modified or deleted once
     * written.
     */
    public static final byte CONSTANT = 0x01;

}
