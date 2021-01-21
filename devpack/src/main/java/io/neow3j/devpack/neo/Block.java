package io.neow3j.devpack.neo;

import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.Hash256;

/**
 * Represents a block and provides block-related information. It is returned for example when
 * calling {@link Blockchain#getBlock(int)}.
 */
public class Block {

    /**
     * The hash of this block.
     */
    public final Hash256 hash;

    /**
     * This block's version number.
     */
    public final int version;

    /**
     * The hash of the preceding block.
     */
    public final Hash256 prevHash;

    /**
     * The merkle root of this block.
     */
    public final Hash256 merkleRoot;

    /**
     * The time at which this block was generated.
     */
    public final long timestamp;

    /**
     * The block height (counted from 0).
     */
    public final long index;

    /**
     * The verification script hash of the validators of the next block. I.e., the script hash of
     * the multisig account made up of the validator's public keys.
     */
    public final Hash160 nextConsensus;

    /**
     * The number of transactions in this block.
     */
    public final int transactionsCount;

    private Block() {
        hash = new Hash256(new byte[0]);
        version = 0;
        prevHash = new Hash256(new byte[0]);
        merkleRoot = new Hash256(new byte[0]);
        timestamp = 0;
        transactionsCount = 0;
        nextConsensus = new Hash160(new byte[0]);
        index = 0;
    }
}
