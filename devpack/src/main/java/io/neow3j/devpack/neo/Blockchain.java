package io.neow3j.devpack.neo;

import static io.neow3j.constants.InteropServiceCode.SYSTEM_BLOCKCHAIN_GETBLOCK;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_BLOCKCHAIN_GETCONTRACT;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_BLOCKCHAIN_GETHEIGHT;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_BLOCKCHAIN_GETTRANSACTION;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_BLOCKCHAIN_GETTRANSACTIONFROMBLOCK;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_BLOCKCHAIN_GETTRANSACTIONHEIGHT;

import io.neow3j.devpack.annotations.Syscall;

/**
 * Provides a set of methods for accessing blockchain data.
 */
public class Blockchain {

    @Syscall(SYSTEM_BLOCKCHAIN_GETHEIGHT)
    public static native long getHeight();

    /**
     * Gets the block at the given block height.
     *
     * @param height The block height.
     * @return the <tt>Block</tt>.
     */
    @Syscall(SYSTEM_BLOCKCHAIN_GETBLOCK)
    public static native Block getBlock(long height);

    /**
     * Gets the block with the given block hash.
     *
     * @param hash The 32-byte block hash.
     * @return the <tt>Block</tt>.
     */
    @Syscall(SYSTEM_BLOCKCHAIN_GETBLOCK)
    public static native Block getBlock(byte[] hash);

    /**
     * Gets the transaction with the given transaction hash.
     *
     * @param hash The 32-byte transaction hash.
     * @return the <tt>Transaction</tt>.
     */
    @Syscall(SYSTEM_BLOCKCHAIN_GETTRANSACTION)
    public static native Transaction getTransaction(byte[] hash);

    /**
     * Gets the transaction at the given index in the block with the given block hash.
     *
     * @param blockHash The 32-byte hash of the block to get the transaction from.
     * @param txIndex   The index of the transaction in the block.
     * @return the <tt>Transaction</tt>.
     */
    @Syscall(SYSTEM_BLOCKCHAIN_GETTRANSACTIONFROMBLOCK)
    public static native Transaction getTransactionFromBlock(byte[] blockHash, int txIndex);

    /**
     * Gets the transaction at the given index in the block with the given height.
     *
     * @param blockHeight The height of the block to get the transaction from.
     * @param txIndex     The index of the transaction in the block.
     * @return the <tt>Transaction</tt>.
     */
    @Syscall(SYSTEM_BLOCKCHAIN_GETTRANSACTIONFROMBLOCK)
    public static native Transaction getTransactionFromBlock(long blockHeight, int txIndex);

    /**
     * Gets the transaction height of the transaction with the given hash. The transaction
     * height is the number of the block in which a transaction is contained.
     *
     * @param transactionHash The hash of the transaction.
     * @return the transaction's height.
     */
    @Syscall(SYSTEM_BLOCKCHAIN_GETTRANSACTIONHEIGHT)
    public static native int getTransactionHeight(byte[] transactionHash);

    /**
     * Gets the contract with the given script hash.
     *
     * @param scriptHash The contract's script hash.
     * @return the contract.
     */
    @Syscall(SYSTEM_BLOCKCHAIN_GETCONTRACT)
    public static native Contract getContract(byte[] scriptHash);

}
