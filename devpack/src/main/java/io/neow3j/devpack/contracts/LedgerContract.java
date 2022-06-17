package io.neow3j.devpack.contracts;

import io.neow3j.devpack.Block;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.Hash256;
import io.neow3j.devpack.Transaction;
import io.neow3j.devpack.annotations.NativeContract;

import static io.neow3j.devpack.Helper.reverse;
import static io.neow3j.devpack.StringLiteralHelper.hexToBytes;
import static io.neow3j.devpack.constants.NativeContract.LedgerContractScriptHash;

/**
 * Represents an interface to the native LedgerContract that stores all blocks and transactions.
 */
@NativeContract
public class LedgerContract extends ContractInterface {

    public LedgerContract() {
        super(new Hash160(reverse(hexToBytes(LedgerContractScriptHash).toByteArray())));
    }

    /**
     * @return the latest block hash.
     */
    public native Hash256 currentHash();

    /**
     * @return the current block height.
     */
    public native int currentIndex();

    /**
     * Gets the block at the given index/height.
     *
     * @param index the block index.
     * @return the block at the given index or {@code null} if it doesn't exist.
     */
    public native Block getBlock(int index);

    /**
     * Gets the block with the given hash.
     *
     * @param hash the block hash.
     * @return the block with the given hash or {@code null} if it doesn't exist.
     */
    public native Block getBlock(Hash256 hash);

    /**
     * Gets the transaction with the given hash.
     *
     * @param hash the transaction hash.
     * @return the transaction with the given hash or {@code null} if it doesn't exist.
     */
    public native Transaction getTransaction(Hash256 hash);

    /**
     * Gets the transaction at {@code index} in the block with {@code blockHash}.
     *
     * @param blockHash the block hash.
     * @param index     the transaction index in the block.
     * @return the transaction or {@code null} if it doesn't exist.
     */
    public native Transaction getTransactionFromBlock(Hash256 blockHash, int index);

    /**
     * Gets the transaction at {@code index} in the block at index {@code blockIndex}.
     *
     * @param blockIndex the block index/height.
     * @param index      the transaction index in the block.
     * @return the transaction or {@code null} if it doesn't exist.
     */
    public native Transaction getTransactionFromBlock(int blockIndex, int index);

    /**
     * Gets the VM state of the transaction with the given hash.
     * <p>
     * Compare the state byte with the values in {@link io.neow3j.devpack.constants.VMState}.
     *
     * @param hash the transaction hash.
     * @return the transaction's VM state.
     */
    public native byte getTransactionVMState(Hash256 hash);

    /**
     * Gets the index of the block that contains the transaction with the given hash.
     *
     * @param hash the transaction hash.
     * @return the block index.
     */
    public native int getTransactionHeight(Hash256 hash);

}
