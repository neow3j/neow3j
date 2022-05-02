package io.neow3j.devpack;

import io.neow3j.devpack.annotations.Instruction;
import io.neow3j.script.OpCode;

/**
 * Represents a block and provides block-related information. It is returned for example when calling
 * {@link io.neow3j.devpack.contracts.LedgerContract#getBlock(Hash256)}.
 */
public class Block {

    /**
     * The hash of this block in little-endian order.
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
    public final int timestamp;

    /**
     * The nonce of this block.
     */

    public final int nonce;

    /**
     * The block height (counted from 0).
     */
    public final int index;

    /**
     * The index of the primary validator for the next block.
     */
    public final int primaryIndex;

    /**
     * The verification script hash of the validators of the next block. I.e., the script hash of the multisig
     * account made up of the validator's public keys.
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
        primaryIndex = 0;
        nextConsensus = new Hash160(new byte[0]);
        index = 0;
        nonce = 0;
    }

    /**
     * Compares this block to the given object. The comparison happens by reference only. I.e., if you retrieve the
     * same block twice, e.g., with {@link io.neow3j.devpack.contracts.LedgerContract#getBlock(int)}, then comparing
     * the two will return false.
     *
     * @param other the object to compare with.
     * @return true if this and {@code other} reference the same block. False, otherwise.
     */
    @Override
    @Instruction(opcode = OpCode.EQUAL)
    public native boolean equals(Object other);

    /**
     * Compares this and the given block by value.
     *
     * @param block another block to compare this block to.
     * @return true if all fields of the two blocks are equal. False, otherwise.
     */
    public boolean equals(Block block) {
        if (this == block) {
            return true;
        }
        return version == block.version &&
                timestamp == block.timestamp &&
                index == block.index &&
                primaryIndex == block.primaryIndex &&
                transactionsCount == block.transactionsCount &&
                hash.equals(block.hash) &&
                prevHash.equals(block.prevHash) &&
                merkleRoot.equals(block.merkleRoot) &&
                nextConsensus.equals(block.nextConsensus) &&
                nonce == block.nonce;
    }

}
