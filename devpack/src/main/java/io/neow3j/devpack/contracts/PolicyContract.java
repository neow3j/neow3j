package io.neow3j.devpack.contracts;

import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.annotations.ContractHash;

@ContractHash("0xcc5e4edd9f5f8dba8bb65734541df7a1c081c67b")
public class PolicyContract extends ContractInterface {

    /**
     * The maximum execution fee factor that the committee can set.
     */
    public static final int MAX_EXEC_FEE_FACTOR = 1000;

    /**
     * The maximum storage price that the committee can set.
     */
    public static final int MAX_STORAGE_PRICE = 1000;

    /**
     * Gets the GAS cost per transaction byte, i.e. the fee per byte.
     *
     * @return the fee per byte.
     */
    public static native int getFeePerByte();

    /**
     * Sets the maximum block size to the given value.
     *
     * @param size The desired block size.
     */
    public static native void setMaxBlockSize(int size);

    /**
     * Sets the maximum number of transactions per block.
     *
     * @param size The desired number of transactions.
     */
    public static native void setMaxTransactionsPerBlock(int size);

    /**
     * Sets the maximum allowed system fee of all transactions in a block.
     *
     * @param fee The desired maximum fee.
     */
    public static native void setMaxBlockSystemFee(int fee);

    /**
     * Sets the fee to be paid per transaction byte.
     *
     * @param fee The desired fee per byte
     */
    public static native void setFeePerByte(int fee);

    /**
     * Blocks the account with the given script hash.
     *
     * @param scriptHash The account to block.
     * @return true, if successful. False, otherwise.
     */
    public static native boolean blockAccount(Hash160 scriptHash);

    /**
     * Unblocks the account with the given script hash.
     *
     * @param scriptHash The account to unblock.
     * @return true, if successful. False, otherwise.
     */
    public static native boolean unblockAccount(Hash160 scriptHash);

    /**
     * Checks if the given account is blocked.
     *
     * @param scriptHash the script hash of the account.
     * @return true if the account is blocked. False, otherwise.
     */
    public static native boolean isBlocked(Hash160 scriptHash);

    /**
     * Gets the fee factor used to calculate the GAS cost of contract executions.
     * <p>
     * Each neo-vm instruction has a relative cost that is multiplied with this fee factor to result
     * in the actual GAS cost.
     *
     * @return the execution fee factor.
     */
    public static native int getExecFeeFactor();

    /**
     * Sets the fee factor used to calculate the GAS cost of contract executions.
     * <p>
     * Each neo-vm instruction has a relative cost that is multiplied with this fee factor to result
     * in the actual GAS cost.
     *
     * @param factor The desired factor.
     */
    public static native void setExecFeeFactor(int factor);

    /**
     * Gets the GAS price per byte of contract storage.
     *
     * @return the price for one byte of storage.
     */
    public static native int getStoragePrice();

    /**
     * Sets the GAS price per byte of contract storage.
     *
     * @param price The desired price for one byte of storage.
     */
    public static native void setStoragePrice(int price);

}
