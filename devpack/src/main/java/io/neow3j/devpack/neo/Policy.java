package io.neow3j.devpack.neo;

import io.neow3j.devpack.annotations.Contract;

@Contract(scriptHash = "0xdde31084c0fdbebc7f5ed5f53a38905305ccee14")
public class Policy {

    /**
     * Gets the name of the Policy contract.
     *
     * @return the name.
     */
    public static native String name();

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
     * Gets the script hashes of the accounts that are currently blacklisted/blocked.
     *
     * @return the blocked accounts' script hashes as hexadecimal strings and in little-endian
     * order.
     */
    public static native String[] getBlockedAccounts();

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
