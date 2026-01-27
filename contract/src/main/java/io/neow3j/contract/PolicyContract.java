package io.neow3j.contract;

import io.neow3j.constants.NeoConstants;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.transaction.TransactionAttributeType;
import io.neow3j.transaction.TransactionBuilder;
import io.neow3j.types.Hash160;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

import static io.neow3j.types.ContractParameter.hash160;
import static io.neow3j.types.ContractParameter.integer;
import static java.util.Arrays.asList;

/**
 * Represents a Policy contract and provides methods to invoke it.
 */
public class PolicyContract extends SmartContract {

    private static final String NAME = "PolicyContract";
    public static final Hash160 SCRIPT_HASH = calcNativeContractHash(NAME);

    private static final String GET_FEE_PER_BYTE = "getFeePerByte";
    private static final String GET_EXEC_FEE_FACTOR = "getExecFeeFactor";
    private static final String GET_STORAGE_PRICE = "getStoragePrice";
    private static final String GET_MILLISECONDS_PER_BLOCK = "getMillisecondsPerBlock";
    private static final String GET_MAX_VALID_UNTIL_BLOCK_INCREMENT = "getMaxValidUntilBlockIncrement";
    private static final String GET_MAX_TRACEABLE_BLOCKS = "getMaxTraceableBlocks";
    private static final String GET_ATTRIBUTE_FEE = "getAttributeFee";
    private static final String IS_BLOCKED = "isBlocked";
    private static final String SET_FEE_PER_BYTE = "setFeePerByte";
    private static final String SET_EXEC_FEE_FACTOR = "setExecFeeFactor";
    private static final String SET_STORAGE_PRICE = "setStoragePrice";
    private static final String SET_MILLISECONDS_PER_BLOCK = "setMillisecondsPerBlock";
    private static final String SET_MAX_VALID_UNTIL_BLOCK_INCREMENT = "setMaxValidUntilBlockIncrement";
    private static final String SET_MAX_TRACEABLE_BLOCKS = "setMaxTraceableBlocks";
    private static final String SET_ATTRIBUTE_FEE = "setAttributeFee";
    private static final String BLOCK_ACCOUNT = "blockAccount";
    private static final String UNBLOCK_ACCOUNT = "unblockAccount";
    private static final String GET_BLOCKED_ACCOUNTS = "getBlockedAccounts";

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
     * Gets the execution fee factor without precision.
     * <p>
     * Note that starting from the Faun hard fork, the execution fee factor uses additional precision of 4 decimal
     * places WHICH THIS FUNCTION IGNORES, i.e., it returns the floored value without the additional precision.
     * <p>
     * If you are not yet using this function in existing code, consider using the new function
     * {@code getExecPicoFeeFactor()} which provides the full precision.
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
     * Gets the block generation time in milliseconds.
     *
     * @return the block generation time in milliseconds.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public BigInteger getMillisecondsPerBlock() throws IOException {
        return callFunctionReturningInt(GET_MILLISECONDS_PER_BLOCK);
    }

    /**
     * Gets the upper increment size of blockchain height (in blocks) exceeding that a transaction should fail
     * validation.
     *
     * @return the upper increment size of blockchain height (in blocks).
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public BigInteger getMaxValidUntilBlockIncrement() throws IOException {
        return callFunctionReturningInt(GET_MAX_VALID_UNTIL_BLOCK_INCREMENT);
    }

    /**
     * Gets the length of the chain accessible to smart contracts.
     *
     * @return the length of the chain accessible to smart contracts.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public BigInteger getMaxTraceableBlocks() throws IOException {
        return callFunctionReturningInt(GET_MAX_TRACEABLE_BLOCKS);
    }

    /**
     * Gets the GAS fee for using a transaction attribute.
     *
     * @param attributeType the type of the transaction attribute.
     * @return the GAS fee for using a transaction attribute.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public BigInteger getAttributeFee(TransactionAttributeType attributeType) throws IOException {
        return callFunctionReturningInt(GET_ATTRIBUTE_FEE, integer(attributeType.byteValue()));
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
     * <p>
     * Starting from the Faun hard fork, this method requires the factor to use additional precision of 4 decimal
     * places. For example, to set the factor to what previously would have been 12, you now need to set it to 120'000.
     *
     * @param factor the execution fee factor.
     * @return a {@link TransactionBuilder}.
     */
    public TransactionBuilder setExecFeeFactor(BigInteger factor) {
        return invokeFunction(SET_EXEC_FEE_FACTOR, integer(factor));
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
     * Creates a transaction script to set the block generation time in milliseconds and initializes
     * a {@link TransactionBuilder} based on this script.
     * <p>
     * This method can only be successfully invoked by the committee, i.e., the transaction has to be signed by the
     * committee members.
     *
     * @param milliseconds the block generation time in milliseconds.
     * @return a {@link TransactionBuilder}.
     */
    public TransactionBuilder setMillisecondsPerBlock(BigInteger milliseconds) {
        return invokeFunction(SET_MILLISECONDS_PER_BLOCK, integer(milliseconds));
    }

    /**
     * Creates a transaction script to set the upper increment size of blockchain height (in blocks)
     * exceeding that a transaction should fail validation and initializes a {@link TransactionBuilder} based on
     * this script.
     * <p>
     * This method can only be successfully invoked by the committee, i.e., the transaction has to be signed by the
     * committee members.
     *
     * @param increment the upper increment size of blockchain height (in blocks).
     * @return a {@link TransactionBuilder}.
     */
    public TransactionBuilder setMaxValidUntilBlockIncrement(BigInteger increment) {
        return invokeFunction(SET_MAX_VALID_UNTIL_BLOCK_INCREMENT, integer(increment));
    }

    /**
     * Creates a transaction script to set the length of the chain accessible to smart contracts and initializes a
     * {@link TransactionBuilder} based on this script.
     * <p>
     * This method can only be successfully invoked by the committee, i.e., the transaction has to be signed by the
     * committee members.
     *
     * @param maxTraceableBlocks the length of the chain accessible to smart contracts.
     * @return a {@link TransactionBuilder}.
     */
    public TransactionBuilder setMaxTraceableBlocks(BigInteger maxTraceableBlocks) {
        return invokeFunction(SET_MAX_TRACEABLE_BLOCKS, integer(maxTraceableBlocks));
    }

    /**
     * Creates a transaction script to set the attribute fee and initializes a {@link TransactionBuilder} based on
     * this script.
     * <p>
     * This method can only be successfully invoked by the committee, i.e., the transaction has to be signed by the
     * committee members.
     *
     * @param attributeType the type of the transaction attribute.
     * @param fee           the attribute fee.
     * @return a {@link TransactionBuilder}.
     */
    public TransactionBuilder setAttributeFee(TransactionAttributeType attributeType, BigInteger fee) {
        return invokeFunction(SET_ATTRIBUTE_FEE, integer(attributeType.byteValue()), integer(fee));
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

    /**
     * Get blocked accounts as iterator.
     *
     * @return an iterator of blocked accounts.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public Iterator<Hash160> getBlockedAccounts() throws IOException {
        return callFunctionReturningIterator(h -> Hash160.fromAddress(h.getAddress()), GET_BLOCKED_ACCOUNTS);
    }

    /**
     * Get blocked accounts as a list.
     * <p>
     * Use this method if sessions are disabled on the Neo node.
     * <p>
     * This method returns at most {@link NeoConstants#MAX_ITERATOR_ITEMS_DEFAULT} values. If there are more values,
     * connect to a Neo node that supports sessions and use {@link #getBlockedAccounts()}.
     *
     * @return
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public List<Hash160> getBlockedAccountsUnwrapped() throws IOException {
        List<StackItem> list = callFunctionAndUnwrapIterator(GET_BLOCKED_ACCOUNTS, asList(), DEFAULT_ITERATOR_COUNT);
        return list.stream().map(h -> Hash160.fromAddress(h.getAddress())).collect(Collectors.toList());
    }

}
