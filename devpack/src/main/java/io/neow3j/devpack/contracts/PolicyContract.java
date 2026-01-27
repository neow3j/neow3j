package io.neow3j.devpack.contracts;

import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.Iterator;
import io.neow3j.devpack.constants.NativeContract;
import io.neow3j.devpack.annotations.CallFlags;

import static io.neow3j.devpack.constants.CallFlags.All;
import static io.neow3j.devpack.constants.CallFlags.ReadOnly;

/**
 * Represents an interface to the native PolicyContract that manages system policies on the Neo blockchain.
 */
public class PolicyContract extends ContractInterface {

    /**
     * The maximum execution fee factor that the committee can set.
     * <p>
     * Note that this value is without the additional precision of 4 decimal places which is the underlying value
     * used starting with the Faun hard fork.
     */
    public static final int MaxExecFeeFactor = 100;

    /**
     * The maximum attribute fee that the committee can set.
     */
    public static final int maxAttributeFee = 10_0000_0000;

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
    @CallFlags(ReadOnly)
    public native int getFeePerByte();

    /**
     * Sets the fee to be paid per transaction byte.
     *
     * @param fee the desired fee per byte.
     */
    @CallFlags(All)
    public native void setFeePerByte(int fee);

    /**
     * Sets the maximum block size to the given value.
     *
     * @param size the desired block size.
     */
    @CallFlags(All)
    public native void setMaxBlockSize(int size);

    /**
     * Sets the maximum number of transactions per block.
     *
     * @param size the desired number of transactions.
     */
    @CallFlags(All)
    public native void setMaxTransactionsPerBlock(int size);

    /**
     * Sets the maximum allowed system fee of all transactions in a block.
     *
     * @param fee the desired maximum fee.
     */
    @CallFlags(All)
    public native void setMaxBlockSystemFee(int fee);

    /**
     * Blocks the account with the given script hash.
     *
     * @param scriptHash the account to block.
     * @return true if successful. False, otherwise.
     */
    @CallFlags(All)
    public native boolean blockAccount(Hash160 scriptHash);

    /**
     * Unblocks the account with the given script hash.
     *
     * @param scriptHash the account to unblock.
     * @return true if successful. False, otherwise.
     */
    @CallFlags(All)
    public native boolean unblockAccount(Hash160 scriptHash);

    /**
     * @return an iterator of all blocked accounts.
     */
    @CallFlags(ReadOnly)
    public native Iterator<Hash160> getBlockedAccounts();

    /**
     * Checks if the given account is blocked.
     *
     * @param scriptHash the script hash of the account.
     * @return true if the account is blocked. False, otherwise.
     */
    @CallFlags(ReadOnly)
    public native boolean isBlocked(Hash160 scriptHash);

    /**
     * Recovers funds from the given account by transferring its entire balance of the specified token to the native
     * treasury contract.
     * <p>
     * The account must be blocked and have been blocked for at least one year before funds can be recovered.
     *
     * @param account the account to recover funds from.
     * @param token the token to recover.
     * @return true if successful. False, otherwise.
     */
    @CallFlags(All)
    public native boolean recoverFund(Hash160 account, Hash160 token);

    /**
     * Gets the execution fee factor without additional precision.
     * <p>
     * Note that starting with the Faun hard fork, the execution fee factor supports an additional precision of 4
     * decimal places. This function ignores that additional precision and returns the floored value.
     * <p>
     * As a result, the value returned by this function may be {@code 0} when the actual execution fee factor is
     * between {@code 0} and {@code 1} (e.g., {@code 0.5} is returned as {@code 0}).
     * <p>
     * For new code, consider using {@link #getExecPicoFeeFactor()}, which provides the full precision.
     *
     * @return the execution fee factor without additional precision.
     */
    @CallFlags(ReadOnly)
    public native int getExecFeeFactor();

    /**
     * Gets the execution fee factor with additional precision.
     * <p>
     * The execution fee factor is used to calculate the execution GAS cost by multiplying it with the base cost of
     * each NeoVM instruction executed in a transaction.
     * <p>
     * Starting with the Faun hard fork, the execution fee factor is internally represented using an value with 4
     * decimal places of additional precision (the "pico" fee factor). This function returns that full-precision value.
     * <p>
     * For example, a return value of {@code 995627} represents a fee factor of {@code 99.5627} when interpreted with
     * 4 decimal places of precision. {@link #getExecFeeFactor()} would return the floored value {@code 99}.
     *
     * @return the execution fee factor with additional precision.
     */
    @CallFlags(ReadOnly)
    public native int getExecPicoFeeFactor();

    /**
     * Sets the fee factor used to calculate the GAS cost of contract executions.
     * <p>
     * Each NeoVM instruction has a relative cost, which is multiplied by this fee factor to determine the actual GAS
     * cost.
     * <p>
     * Note that starting with the Faun hard fork, the execution fee factor supports an additional precision of 4
     * decimal places. This function expects the full precision to be provided. In other words, the value passed to
     * this function is the same value that will later be returned by {@link #getExecPicoFeeFactor()}, while
     * {@link #getExecFeeFactor()} returns the floored value without the additional precision.
     * <p>
     * For example, to set an execution fee factor of {@code 1.5627}, call this function with {@code 15627}. In this
     * case, {@link #getExecFeeFactor()} will return {@code 1}, while {@link #getExecPicoFeeFactor()} will return
     * {@code 15627}.
     *
     * @param factor the execution fee factor.
     */
    @CallFlags(All)
    public native void setExecFeeFactor(int factor);

    /**
     * @return the GAS price for one byte of contract storage.
     */
    @CallFlags(ReadOnly)
    public native int getStoragePrice();

    /**
     * Sets the GAS price per byte of contract storage.
     *
     * @param price the desired price for one byte of storage.
     */
    @CallFlags(All)
    public native void setStoragePrice(int price);

    /**
     * @return the block generation time in milliseconds
     */
    @CallFlags(ReadOnly)
    public native int getMillisecondsPerBlock();

    /**
     * Sets the block generation time in milliseconds.
     *
     * @param milliseconds The block generation time in milliseconds.
     */
    @CallFlags(All)
    public native void setMillisecondsPerBlock(int milliseconds);

    /**
     * @return the upper increment size of blockchain height (in blocks) exceeding that a transaction should fail
     * validation.
     */
    @CallFlags(ReadOnly)
    public native int getMaxValidUntilBlockIncrement();

    /**
     * Sets the upper increment size of blockchain height (in blocks) exceeding that a transaction should fail
     * validation.
     *
     * @param increment The upper increment size of blockchain height (in blocks).
     */
    @CallFlags(All)
    public native void setMaxValidUntilBlockIncrement(int increment);

    /**
     * @return the length of the chain accessible to smart contracts.
     */
    @CallFlags(ReadOnly)
    public native int getMaxTraceableBlocks();

    /**
     * Sets the length of the chain accessible to smart contracts.
     *
     * @param blocks The length of the chain accessible to smart contracts.
     */
    @CallFlags(All)
    public native void setMaxTraceableBlocks(int blocks);

    /**
     * Gets the GAS fee for the given attribute type.
     *
     * @param attributeType the attribute type.
     * @return the GAS fee for the given attribute type.
     */
    @CallFlags(ReadOnly)
    public native int getAttributeFee(byte attributeType);

    /**
     * Sets the GAS fee for the given attribute type.
     *
     * @param attributeType the attribute type.
     * @param fee           the GAS fee.
     */
    @CallFlags(All)
    public native void setAttributeFee(byte attributeType, int fee);

}
