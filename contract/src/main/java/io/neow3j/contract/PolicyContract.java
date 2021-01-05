package io.neow3j.contract;

import io.neow3j.protocol.Neow3j;

import java.io.IOException;
import java.math.BigInteger;

/**
 * Represents a Policy contract and provides methods to invoke it.
 */
public class PolicyContract extends SmartContract {

    private static final String NAME = "PolicyContract";
    public static final ScriptHash SCRIPT_HASH = getScriptHashOfNativeContract(NAME);

    private static final String GET_MAX_TRANSACTIONS_PER_BLOCK = "getMaxTransactionsPerBlock";
    private static final String GET_MAX_BLOCK_SIZE = "getMaxBlockSize";
    private static final String GET_MAX_BLOCK_SYSTEM_FEE = "getMaxBlockSystemFee";
    private static final String GET_FEE_PER_BYTE = "getFeePerByte";
    private static final String GET_EXEC_FEE_FACTOR = "getExecFeeFactor";
    private static final String GET_STORAGE_PRICE = "getStoragePrice";
    private static final String IS_BLOCKED = "isBlocked";
    private static final String SET_MAX_BLOCK_SIZE = "setMaxBlockSize";
    private static final String SET_MAX_TX_PER_BLOCK = "setMaxTransactionsPerBlock";
    private static final String SET_MAX_BLOCK_SYSTEM_FEE = "setMaxBlockSystemFee";
    private static final String SET_FEE_PER_BYTE = "setFeePerByte";
    private static final String SET_EXEC_FEE_FACTOR = "setExecFeeFactor";
    private static final String SET_STORAGE_PRICE = "setStoragePrice";
    private static final String BLOCK_ACCOUNT = "blockAccount";
    private static final String UNBLOCK_ACCOUNT = "unblockAccount";

    /**
     * Constructs a new <tt>PolicyContract</tt> that uses the given {@link Neow3j} instance for
     * invocations.
     *
     * @param neow The {@link Neow3j} instance to use for invocations.
     */
    public PolicyContract(Neow3j neow) {
        super(SCRIPT_HASH, neow);
    }

    /**
     * Returns the maximal amount of transactions allowed per block.
     *
     * @return the maximal amount of transactions allowed per block.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public Integer getMaxTransactionsPerBlock() throws IOException {
        return callFuncReturningInt(GET_MAX_TRANSACTIONS_PER_BLOCK).intValue();
    }

    /**
     * Returns the maximal size allowed for a block.
     *
     * @return the maximal size allowed for a block.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public Integer getMaxBlockSize() throws IOException {
        return callFuncReturningInt(GET_MAX_BLOCK_SIZE).intValue();
    }

    /**
     * Returns the maximal summed up system fee allowed for a block.
     *
     * @return the maximal summed up system fee allowed for a block.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public BigInteger getMaxBlockSystemFee() throws IOException {
        return callFuncReturningInt(GET_MAX_BLOCK_SYSTEM_FEE);
    }

    /**
     * Gets the system fee per byte.
     *
     * @return the system fee per byte.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public BigInteger getFeePerByte() throws IOException {
        return callFuncReturningInt(GET_FEE_PER_BYTE);
    }

    /**
     * Gets the execution fee factor.
     *
     * @return the execution fee factor.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public BigInteger getExecFeeFactor() throws IOException {
        return callFuncReturningInt(GET_EXEC_FEE_FACTOR);
    }

    /**
     * Gets the storage price.
     *
     * @return the storage price.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public BigInteger getStoragePrice() throws IOException {
        return callFuncReturningInt(GET_STORAGE_PRICE);
    }

    /**
     * Checks whether an account is blocked in the Neo network.
     *
     * @param scriptHash the script hash of the account.
     * @return true if the account is blocked. False, otherwise.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public boolean isBlocked(ScriptHash scriptHash) throws IOException {
        return callFuncReturningBool(IS_BLOCKED, ContractParameter.hash160(scriptHash));
    }

    /**
     * Creates a transaction script to set the maximal size of a block and initializes
     * a {@link TransactionBuilder} based on this script.
     *
     * @param maxBlockSize The maximal size of a block.
     * @return A {@link TransactionBuilder}.
     */
    public TransactionBuilder setMaxBlockSize(Integer maxBlockSize) {
        return invokeFunction(SET_MAX_BLOCK_SIZE, ContractParameter.integer(maxBlockSize));
    }

    /**
     * Creates a transaction script to set the maximal amount of transactions per block and initializes
     * a {@link TransactionBuilder} based on this script.
     *
     * @param maxTxPerBlock The maximal allowed number of transactions per block.
     * @return A {@link TransactionBuilder}.
     */
    public TransactionBuilder setMaxTransactionsPerBlock(Integer maxTxPerBlock) {
        return invokeFunction(SET_MAX_TX_PER_BLOCK, ContractParameter.integer(maxTxPerBlock));
    }

    /**
     * Creates a transaction script to set the maximal system fee per block and initializes a
     * {@link TransactionBuilder} based on this script.
     *
     * @param maxBlockSystemFee The maximal system fee per block.
     * @return A {@link TransactionBuilder}.
     */
    public TransactionBuilder setMaxBlockSystemFee(BigInteger maxBlockSystemFee){
        return invokeFunction(SET_MAX_BLOCK_SYSTEM_FEE, ContractParameter.integer(maxBlockSystemFee));
    }

    /**
     * Creates a transaction script to set the fee per byte and initializes a
     * {@link TransactionBuilder} based on this script.
     *
     * @param fee The fee per byte.
     * @return A {@link TransactionBuilder}.
     */
    public TransactionBuilder setFeePerByte(Integer fee) {
        return invokeFunction(SET_FEE_PER_BYTE, ContractParameter.integer(fee));
    }

    /**
     * Creates a transaction script to set the execution fee factor and initializes a
     * {@link TransactionBuilder} based on this script.
     *
     * @param fee The execution fee factor.
     * @return A {@link TransactionBuilder}.
     */
    public TransactionBuilder setExecFeeFactor(Integer fee) {
        return invokeFunction(SET_EXEC_FEE_FACTOR, ContractParameter.integer(fee));
    }

    /**
     * Creates a transaction script to set the storage price and initializes a
     * {@link TransactionBuilder} based on this script.
     *
     * @param price The storage price.
     * @return A {@link TransactionBuilder}.
     */
    public TransactionBuilder setStoragePrice(Integer price) {
        return invokeFunction(SET_STORAGE_PRICE, ContractParameter.integer(price));
    }

    /**
     * Creates a transaction script to block an account in the neo-network and initializes
     * a {@link TransactionBuilder} based on this script.
     *
     * @param addressToBlock The address of the account to block.
     * @return A {@link TransactionBuilder}.
     */
    public TransactionBuilder blockAccount(String addressToBlock) {
        return invokeFunction(BLOCK_ACCOUNT,
                ContractParameter.hash160(ScriptHash.fromAddress(addressToBlock)));
    }

    /**
     * Creates a transaction script to block an account in the neo-network and initializes
     * a {@link TransactionBuilder} based on this script.
     *
     * @param accountToBlock The account to block.
     * @return A {@link TransactionBuilder}.
     */
    public TransactionBuilder blockAccount(ScriptHash accountToBlock) {
        return invokeFunction(BLOCK_ACCOUNT, ContractParameter.hash160(accountToBlock));
    }

    /**
     * Creates a transaction script to unblock an account in the neo-network and initializes
     * a {@link TransactionBuilder} based on this script.
     *
     * @param addressToBlock The address of the account to unblock.
     * @return A {@link TransactionBuilder}.
     */
    public TransactionBuilder unblockAccount(String addressToBlock) {
        return invokeFunction(UNBLOCK_ACCOUNT,
                ContractParameter.hash160(ScriptHash.fromAddress(addressToBlock)));
    }

    /**
     * Creates a transaction script to unblock an account in the neo-network and initializes
     * a {@link TransactionBuilder} based on this script.
     *
     * @param accountToUnblock The account to unblock.
     * @return A {@link TransactionBuilder}.
     */
    public TransactionBuilder unblockAccount(ScriptHash accountToUnblock) {
        return invokeFunction(UNBLOCK_ACCOUNT, ContractParameter.hash160(accountToUnblock));
    }
}
