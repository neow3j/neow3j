package io.neow3j.neofs.sdk;

import io.neow3j.utils.Numeric;

/**
 * Frequently used Basic ACL values. Bitmasks are taken from the NeoFS Specification.
 */
public enum BasicACL {

    // Todo: Investigate way to set custom ACL values. This might require additional helper methods.

    /**
     * Represents fully-private container without eACL.
     */
    PRIVATE_BASIC_NAME("private", "0x1C8C8CCC"),

    /**
     * Represents fully-private container that allows eACL.
     */
    EACL_PRIVATE_BASIC_NAME("eacl-private", "0x0C8C8CCC"),

    /**
     * Represents public read-only container without eACL.
     */
    READ_ONLY_BASIC_NAME("public-read", "0x1FBF8CFF"),

    /**
     * Represents public read-only container that allows eACL.
     */
    EACL_READ_ONLY_BASIC_NAME("eacl-public-read", "0x0FBF8CFF"),

    /**
     * Represents fully-public container without eACL.
     */
    PUBLIC_BASIC_NAME("public-read-write", "0x1FBFBFFF"),

    /**
     * Represents fully-public container that allows eACL.
     */
    EACL_PUBLIC_BASIC_NAME("eacl-public-read-write", "0x0FBFBFFF"),

    /**
     * Represents a fully-public container without eACL except that the DELETE operation is only allowed on the owner.
     */
    PUBLIC_APPEND_NAME("public-append", "0x1FBF9FFF"),

    /**
     * Represents fully-public container that allows eACL except DELETE operation is only allowed on the owner.
     */
    EACL_PUBLIC_APPEND_NAME("eacl-public-append", "0x0FBF9FFF");

    private final String desc;
    private final String hex;

    BasicACL(String desc, String hex) {
        this.desc = desc;
        this.hex = hex;
    }

    public String desc() {
        return desc;
    }

    public int value() {
        return Integer.parseInt(Numeric.cleanHexPrefix(hex), 16);
    }

}
