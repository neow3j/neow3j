package io.neow3j.devpack.contracts;

import io.neow3j.devpack.Block;
import io.neow3j.devpack.Hash256;
import io.neow3j.devpack.Transaction;
import io.neow3j.devpack.annotations.ContractHash;

@ContractHash("0xda65b600f7124ce6c79950c1772a36403104f2be")
public class LedgerContract extends ContractInterface {

    /**
     * Gets the hash of the latest block.
     *
     * @return the latest block hash.
     */
    public static native Hash256 currentHash();

    /**
     * Gets the current block height.
     *
     * @return the current block height.
     */
    public static native int currentIndex();

    /**
     * Gets the block at the given index/height.
     *
     * @param index The block index.
     * @return the block at the given index or {@code null} if it doesn't exist.
     */
    public static native Block getBlock(int index);

    /**
     * Gets the block with the given hash.
     *
     * @param hash The block hash.
     * @return the block with the given hash or {@code null} if it doesn't exist.
     */
    public static native Block getBlock(Hash256 hash);

    /**
     * Gets the transaction with the given hash.
     *
     * @param hash The transaction hash.
     * @return the transaction with the given hash or {@code null} if it doesn't exist.
     */
    public static native Transaction getTransaction(Hash256 hash);

    /**
     * Gets the transaction at {@code index} in the block with {@code blockHash}
     *
     * @param blockHash The block hash.
     * @param index     The transaction index in the block.
     * @return the transaction or {@code null} if it doesn't exist.
     */
    public static native Transaction getTransactionFromBlock(Hash256 blockHash, int index);

    /**
     * Gets the transaction at {@code index} in the block at index {@code blockIndex}
     *
     * @param blockIndex The block index/height.
     * @param index      The transaction index in the block.
     * @return the transaction or {@code null} if it doesn't exist.
     */
    public static native Transaction getTransactionFromBlock(int blockIndex, int index);

    /**
     * Gets the index of the block that contains the transaction with the given hash.
     *
     * @param hash The transaction hash.
     * @return the block index.
     */
    public static native int getTransactionHeight(Hash256 hash);

}
