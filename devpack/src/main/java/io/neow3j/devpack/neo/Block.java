package io.neow3j.devpack.neo;

/**
 * Represents a block and provides block-related information. It is returned for example when
 * calling {@link Blockchain#getBlock(int)}.
 */
public class Block {

    /**
     * The hash of this block.
     */
    public final byte[] hash;

    /**
     * This block's version number.
     */
    public final int version;

    /**
     * The hash of the preceding block.
     */
    public final byte[] prevHash;

    /**
     * The merkle root of this block.
     */
    public final byte[] merkleRoot;

    /**
     * The time at which this block was generated.
     */
    public final long timestamp;

    /**
     * The block height (counted from 0).
     */
    public final long index;

    /**
     * The consensus contract for the next block, i.e. a multi-party signed contract composed of
     * more than 2/3 of the consensus nodes.
     */
    public final byte[] nextConsensus;

    /**
     * The number of transactions in this block.
     */
    public final int transactionsCount;

    private Block() {
        hash = new byte[0];
        version = 0;
        prevHash = new byte[0];
        merkleRoot = new byte[0];
        timestamp = 0;
        transactionsCount = 0;
        nextConsensus = new byte[0];
        index = 0;
    }
}
