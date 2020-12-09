package io.neow3j.devpack;

import io.neow3j.devpack.annotations.ContractScriptHash;

@ContractScriptHash("0xce06595079cd69583126dbfd1d2e25cca74cffe9")
public class Policy extends ContractInterface {

    /**
     * Gets the maximum allowed number of transactions per block.
     *
     * @return the maximum allowed number of transactions per block.
     */
    public static native int getMaxTransactionsPerBlock();

    /**
     * Gets the maximum block size.
     *
     * @return the maximum block size.
     */
    public static native int getMaxBlockSize();

    /**
     * Gets the maximum allowed system fee of all transactions in a block.
     *
     * @return the maximum system fee.
     */
    public static native int getMaxBlockSystemFee();

    /**
     * Gets the cost one has to pay per transaction byte, i.e. the fee per byte.
     *
     * @return the fee per byte.
     */
    public static native int getFeePerByte();

    /**
     * Checks if the account with the given script hash is blacklisted.
     *
     * @param scriptHash the script hash of the account.
     * @return True, if the account is blocked. False, otherwise.
     */
    public static native boolean isBlocked(byte[] scriptHash);


    /**
     * Sets the maximum block size to the given value.
     *
     * @param size The desired block size.
     * @return true, if successful. False, otherwise.
     */
    public static native boolean setMaxBlockSize(int size);

    /**
     * Sets the maximum number of transactions per block.
     *
     * @param size The desired number of transactions.
     * @return true, if successful. False, otherwise.
     */
    public static native boolean setMaxTransactionsPerBlock(int size);

    /**
     * Sets the maximum allowed system fee of all transactions in a block.
     *
     * @param fee The desired maximum fee.
     * @return true, if successful. False, otherwise.
     */
    public static native boolean setMaxBlockSystemFee(int fee);

    /**
     * Sets the fee to be paid per transaction byte.
     *
     * @param fee The desired fee per byte
     * @return true, if successful. False, otherwise.
     */
    public static native boolean setFeePerByte(long fee);

    /**
     * Blocks the account with the given script hash.
     *
     * @param scriptHash The account to block.
     * @return true, if successful. False, otherwise.
     */
    public static native boolean blockAccount(byte[] scriptHash);

    /**
     * Unblocks the account with the given script hash.
     *
     * @param scriptHash The account to unblock.
     * @return true, if successful. False, otherwise.
     */
    public static native boolean unblockAccount(byte[] scriptHash);
}
