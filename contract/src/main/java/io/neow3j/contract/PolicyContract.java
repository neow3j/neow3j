package io.neow3j.contract;

import io.neow3j.protocol.Neow3j;
import io.neow3j.transaction.TransactionBuilder;
import io.neow3j.types.Hash160;

import java.io.IOException;
import java.math.BigInteger;

import static io.neow3j.types.ContractParameter.hash160;
import static io.neow3j.types.ContractParameter.integer;

/**
 * Represents a Policy contract and provides methods to invoke it.
 */
public class PolicyContract extends SmartContract {

    private static final String NAME = "PolicyContract";
    public static final Hash160 SCRIPT_HASH = calcNativeContractHash(NAME);

    private static final String GET_FEE_PER_BYTE = "getFeePerByte";
    private static final String GET_EXEC_FEE_FACTOR = "getExecFeeFactor";
    private static final String GET_STORAGE_PRICE = "getStoragePrice";
    private static final String IS_BLOCKED = "isBlocked";
    private static final String SET_FEE_PER_BYTE = "setFeePerByte";
    private static final String SET_EXEC_FEE_FACTOR = "setExecFeeFactor";
    private static final String SET_STORAGE_PRICE = "setStoragePrice";
    private static final String BLOCK_ACCOUNT = "blockAccount";
    private static final String UNBLOCK_ACCOUNT = "unblockAccount";

    /**
     * Constructs a new {@link PolicyContract} that uses the given {@link Neow3j} instance for invocations.
     *
     * @param neow the {@link Neow3j} instance to use for invocations.
     */
    public PolicyContract(Neow3j neow) {
        super(SCRIPT_HASH, neow);
    }

    /**
     * Gets the fee paid per byte of transaction.
     *
     * @return the system fee per transaction byte.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public BigInteger getFeePerByte() throws IOException {
        return callFunctionReturningInt(GET_FEE_PER_BYTE);
    }

    /**
     * Gets the execution fee factor.
     *
     * @return the execution fee factor.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public BigInteger getExecFeeFactor() throws IOException {
        return callFunctionReturningInt(GET_EXEC_FEE_FACTOR);
    }

    /**
     * Gets the GAS price for one byte of smart contract storage.
     *
     * @return the storage price per byte.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public BigInteger getStoragePrice() throws IOException {
        return callFunctionReturningInt(GET_STORAGE_PRICE);
    }

    /**
     * Checks whether an account is blocked in the Neo network.
     *
     * @param scriptHash the script hash of the account.
     * @return true if the account is blocked. False, otherwise.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public boolean isBlocked(Hash160 scriptHash) throws IOException {
        return callFunctionReturningBool(IS_BLOCKED, hash160(scriptHash));
    }

    /**
     * Creates a transaction script to set the fee per byte and initializes a {@link TransactionBuilder} based on
     * this script.
     * <p>
     * This method can only be successfully invoked by the committee, i.e., the transaction has to be signed by the
     * committee members.
     *
     * @param fee the fee per byte.
     * @return a {@link TransactionBuilder}.
     */
    public TransactionBuilder setFeePerByte(BigInteger fee) {
        return invokeFunction(SET_FEE_PER_BYTE, integer(fee));
    }

    /**
     * Creates a transaction script to set the execution fee factor and initializes a {@link TransactionBuilder}
     * based on this script.
     *
     * @param fee the execution fee factor.
     * @return a {@link TransactionBuilder}.
     */
    public TransactionBuilder setExecFeeFactor(BigInteger fee) {
        return invokeFunction(SET_EXEC_FEE_FACTOR, integer(fee));
    }

    /**
     * Creates a transaction script to set the storage price and initializes a {@link TransactionBuilder} based on
     * this script.
     * <p>
     * This method can only be successfully invoked by the committee, i.e., the transaction has to be signed by the
     * committee members.
     *
     * @param price the storage price.
     * @return a {@link TransactionBuilder}.
     */
    public TransactionBuilder setStoragePrice(BigInteger price) {
        return invokeFunction(SET_STORAGE_PRICE, integer(price));
    }

    /**
     * Creates a transaction script to block an account in the neo-network and initializes a
     * {@link TransactionBuilder} based on this script.
     * <p>
     * This method can only be successfully invoked by the committee, i.e., the transaction has to be signed by the
     * committee members.
     *
     * @param addressToBlock the address of the account to block.
     * @return a {@link TransactionBuilder}.
     */
    public TransactionBuilder blockAccount(String addressToBlock) {
        return invokeFunction(BLOCK_ACCOUNT, hash160(Hash160.fromAddress(addressToBlock)));
    }

    /**
     * Creates a transaction script to block an account in the neo-network and initializes a
     * {@link TransactionBuilder} based on this script.
     * <p>
     * This method can only be successfully invoked by the committee, i.e., the transaction has to be signed by the
     * committee members.
     *
     * @param accountToBlock the account to block.
     * @return a {@link TransactionBuilder}.
     */
    public TransactionBuilder blockAccount(Hash160 accountToBlock) {
        return invokeFunction(BLOCK_ACCOUNT, hash160(accountToBlock));
    }

    /**
     * Creates a transaction script to unblock an account in the neo-network and initializes a
     * {@link TransactionBuilder} based on this script.
     * <p>
     * This method can only be successfully invoked by the committee, i.e., the transaction has to be signed by the
     * committee members.
     *
     * @param addressToBlock the address of the account to unblock.
     * @return a {@link TransactionBuilder}.
     */
    public TransactionBuilder unblockAccount(String addressToBlock) {
        return invokeFunction(UNBLOCK_ACCOUNT, hash160(Hash160.fromAddress(addressToBlock)));
    }

    /**
     * Creates a transaction script to unblock an account in the neo-network and initializes a
     * {@link TransactionBuilder} based on this script.
     * <p>
     * This method can only be successfully invoked by the committee, i.e., the transaction has to be signed by the
     * committee members.
     *
     * @param accountToUnblock the account to unblock.
     * @return a {@link TransactionBuilder}.
     */
    public TransactionBuilder unblockAccount(Hash160 accountToUnblock) {
        return invokeFunction(UNBLOCK_ACCOUNT, hash160(accountToUnblock));
    }

}
