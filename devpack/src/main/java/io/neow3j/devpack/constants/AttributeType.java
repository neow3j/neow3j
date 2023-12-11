package io.neow3j.devpack.constants;

/**
 * Represents the types of attributes that can be added to a transaction.
 */
public class AttributeType {

    /**
     * Attribute that allows committee members to prioritize a transaction.
     */
    public static final byte HighPriority = 0x01;

    /**
     * Attribute that is used by oracle nodes to append oracle responses to a transaction.
     */
    public static final byte OracleResponse = 0x11;

    /**
     * Attribute that is used to specify the earliest time a transaction can be included in a block.
     */
    public static final byte NotValidBefore = 0x20;

    /**
     * Attribute that is used to specify if it has a conflict with another transaction that might have already been
     * verified in the mempool.
     */
    public static final byte Conflicts = 0x21;

}
