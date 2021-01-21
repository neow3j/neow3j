package io.neow3j.devpack.contracts;

import io.neow3j.devpack.ContractInterface;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.annotations.ContractHash;

@ContractHash("0xdde31084c0fdbebc7f5ed5f53a38905305ccee14")
public class PolicyContract extends ContractInterface {

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
     * Gets the GAS cost per transaction byte, i.e. the fee per byte.
     *
     * @return the fee per byte.
     */
    public static native int getFeePerByte();

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
     * @return true, if the factor was successfully set. False, otherwise.
     */
    public static native boolean setExecFeeFactor(int factor);

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
     * @return true, if the price was successfully set. False, otherwise.
     */
    public static native boolean setStoragePrice(int price);

}
