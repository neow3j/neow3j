package io.neow3j.devpack.contracts;

import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.constants.NativeContract;

/**
 * Represents an interface to the native PolicyContract that manages system policies on the Neo blockchain.
 */
public class PolicyContract extends ContractInterface {

    /**
     * The maximum execution fee factor that the committee can set.
     */
    public static final int MaxExecFeeFactor = 100;

    /**
     * The maximum storage price that the committee can set.
     */
    public static final int MaxStoragePrice = 10000000;

    /**
     * Initializes an interface to the native PolicyContract.
     */
    public PolicyContract() {
        super(NativeContract.PolicyContractScriptHash);
    }

    /**
     * @return the GAS cost per transaction byte, i.e., the fee per byte.
     */
    public native int getFeePerByte();

    /**
     * Sets the maximum block size to the given value.
     *
     * @param size the desired block size.
     */
    public native void setMaxBlockSize(int size);

    /**
     * Sets the maximum number of transactions per block.
     *
     * @param size the desired number of transactions.
     */
    public native void setMaxTransactionsPerBlock(int size);

    /**
     * Sets the maximum allowed system fee of all transactions in a block.
     *
     * @param fee the desired maximum fee.
     */
    public native void setMaxBlockSystemFee(int fee);

    /**
     * Sets the fee to be paid per transaction byte.
     *
     * @param fee the desired fee per byte.
     */
    public native void setFeePerByte(int fee);

    /**
     * Blocks the account with the given script hash.
     *
     * @param scriptHash the account to block.
     * @return true if successful. False, otherwise.
     */
    public native boolean blockAccount(Hash160 scriptHash);

    /**
     * Unblocks the account with the given script hash.
     *
     * @param scriptHash the account to unblock.
     * @return true if successful. False, otherwise.
     */
    public native boolean unblockAccount(Hash160 scriptHash);

    /**
     * Checks if the given account is blocked.
     *
     * @param scriptHash the script hash of the account.
     * @return true if the account is blocked. False, otherwise.
     */
    public native boolean isBlocked(Hash160 scriptHash);

    /**
     * Gets the fee factor used to calculate the GAS cost of contract executions.
     * <p>
     * Each neo-vm instruction has a relative cost that is multiplied with this fee factor to result in the actual
     * GAS cost.
     *
     * @return the execution fee factor.
     */
    public native int getExecFeeFactor();

    /**
     * Sets the fee factor used to calculate the GAS cost of contract executions.
     * <p>
     * Each NeoVM instruction has a relative cost that is multiplied with this fee factor to result in the actual
     * GAS cost.
     *
     * @param factor the desired factor.
     */
    public native void setExecFeeFactor(int factor);

    /**
     * @return the GAS price for one byte of contract storage.
     */
    public native int getStoragePrice();

    /**
     * Sets the GAS price per byte of contract storage.
     *
     * @param price the desired price for one byte of storage.
     */
    public native void setStoragePrice(int price);

}
