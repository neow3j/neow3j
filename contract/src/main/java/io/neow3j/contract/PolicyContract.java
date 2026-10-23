package io.neow3j.contract;

import io.neow3j.constants.NeoConstants;
import io.neow3j.contract.types.WhitelistFeeEntry;
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
import static io.neow3j.types.ContractParameter.string;
import static java.util.Arrays.asList;

/**
 * Represents a Policy contract and provides methods to invoke it.
 */
public class PolicyContract extends SmartContract {

    private static final String NAME = "PolicyContract";
    public static final Hash160 SCRIPT_HASH = calcNativeContractHash(NAME);

    private static final String GET_FEE_PER_BYTE = "getFeePerByte";
    private static final String GET_EXEC_FEE_FACTOR = "getExecFeeFactor";
    private static final String GET_EXEC_PICO_FEE_FACTOR = "getExecPicoFeeFactor";
    private static final String GET_STORAGE_PRICE = "getStoragePrice";
    private static final String GET_MILLISECONDS_PER_BLOCK = "getMillisecondsPerBlock";
    private static final String GET_MAX_VALID_UNTIL_BLOCK_INCREMENT = "getMaxValidUntilBlockIncrement";
    private static final String GET_MAX_TRACEABLE_BLOCKS = "getMaxTraceableBlocks";
    private static final String GET_ATTRIBUTE_FEE = "getAttributeFee";
    private static final String IS_BLOCKED = "isBlocked";
    private static final String REMOVE_WHITELIST_FEE_CONTRACT = "removeWhitelistFeeContract";
    private static final String SET_WHITELIST_FEE_CONTRACT = "setWhitelistFeeContract";
    private static final String GET_WHITELIST_FEE_CONTRACT = "getWhitelistFeeContracts";
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
    private static final String RECOVER_FUND = "recoverFund";

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
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public BigInteger getExecFeeFactor() throws IOException {
        return callFunctionReturningInt(GET_EXEC_FEE_FACTOR);
    }

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
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public BigInteger getExecPicoFeeFactor() throws IOException {
        return callFunctionReturningInt(GET_EXEC_PICO_FEE_FACTOR);
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
     * Creates a transaction script to remove a whitelist entry for a contract method with fixed execution and
     * storage fees and initializes a {@link TransactionBuilder} for that script.
     * <p>
     * The whitelist entry is identified by the contract script hash, method name, and argument count. When removed,
     * the method will again be charged according to the global execution and storage fee factors.
     * <p>
     * This operation can only be successfully executed by the committee; therefore, the resulting transaction must
     * be signed by the committee members.
     *
     * @param contract the script hash of the smart contract.
     * @param method   the name of the contract method.
     * @param argCount the number of arguments the method accepts.
     * @return a {@link TransactionBuilder} initialized with the removal script.
     */
    public TransactionBuilder removeWhitelistFeeContract(Hash160 contract, String method, int argCount) {
        return invokeFunction(REMOVE_WHITELIST_FEE_CONTRACT, hash160(contract), string(method), integer(argCount));
    }

    /**
     * Creates a transaction script to add or update a whitelist entry for a contract method with fixed execution and
     * storage fees and initializes a {@link TransactionBuilder} for that script.
     * <p>
     * The whitelist entry is identified by the contract script hash, method name, and argument count. When
     * whitelisted, the method is charged a fixed system fee, overriding both the execution fee (opcode and interop
     * prices multiplied by the execution fee factor) and the storage fee (storage prices multiplied by the storage
     * fee factor).
     * <p>
     * This operation can only be successfully executed by the committee; therefore, the resulting transaction must
     * be signed by the committee members.
     *
     * @param contract the script hash of the smart contract.
     * @param method   the name of the contract method.
     * @param argCount the number of arguments the method accepts.
     * @param fixedFee the fixed system fee applied to the method.
     * @return a {@link TransactionBuilder} initialized with the whitelist update script.
     */
    public TransactionBuilder setWhitelistFeeContract(Hash160 contract, String method, int argCount,
            BigInteger fixedFee) {
        return invokeFunction(SET_WHITELIST_FEE_CONTRACT, hash160(contract), string(method), integer(argCount),
                integer(fixedFee));
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
     * Creates a transaction script to set the fee factor used to calculate the GAS cost of contract executions, and
     * initializes a {@link TransactionBuilder} based on this script.
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
     * @return a list of blocked accounts.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public List<Hash160> getBlockedAccountsUnwrapped() throws IOException {
        List<StackItem> list = callFunctionAndUnwrapIterator(GET_BLOCKED_ACCOUNTS, asList(), DEFAULT_ITERATOR_COUNT);
        return list.stream().map(h -> Hash160.fromAddress(h.getAddress())).collect(Collectors.toList());
    }

    /**
     * Creates a transaction script to recover funds from a blocked account.
     * <p>
     * The account must be blocked and have been blocked for at least one year before funds can be recovered.
     * <p>
     * This method can only be successfully invoked by the committee, i.e., the transaction has to be signed by the
     * committee members.
     *
     * @param account the account to recover funds from.
     * @param token   the token to recover.
     * @return a {@link TransactionBuilder}.
     */
    public TransactionBuilder recoverFund(Hash160 account, Hash160 token) {
        return invokeFunction(RECOVER_FUND, hash160(account), hash160(token));
    }

    /**
     * Returns an iterator over whitelist entries for contract methods with fixed system fees.
     * <p>
     * Each returned {@link WhitelistFeeEntry} represents a contract method for which both the execution fee and the
     * storage fee are overridden by a fixed system fee.
     * <p>
     * This method allows retrieving the complete whitelist across multiple RPC calls, but it requires the connected
     * node to have sessions enabled.
     * <p>
     * If the node has sessions disabled, use {@link #getWhitelistFeeContractsUnwrapped()}, noting that it only
     * returns the first {@value #DEFAULT_ITERATOR_COUNT} entries.
     *
     * @return an iterator over {@link WhitelistFeeEntry} instances.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public Iterator<WhitelistFeeEntry> getWhitelistFeeContracts() throws IOException {
        return callFunctionReturningIterator(WhitelistFeeEntry::fromStackItem, GET_WHITELIST_FEE_CONTRACT);
    }

    /**
     * Returns whitelist entries for contract methods with fixed system fees as a list.
     * <p>
     * Each returned {@link WhitelistFeeEntry} represents a contract method for which both the execution fee and the
     * storage fee are overridden by a fixed system fee.
     * <p>
     * This is a convenience method that unwraps only the first {@value #DEFAULT_ITERATOR_COUNT} entries. If the
     * whitelist contains more entries, use {@link #getWhitelistFeeContracts()} to retrieve the complete set
     * (requires sessions enabled).
     *
     * @return a list of {@link WhitelistFeeEntry} instances (limited to the first {@value #DEFAULT_ITERATOR_COUNT}
     * entries).
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public List<WhitelistFeeEntry> getWhitelistFeeContractsUnwrapped() throws IOException {
        return callFunctionAndUnwrapIterator(GET_WHITELIST_FEE_CONTRACT, asList(), DEFAULT_ITERATOR_COUNT)
                .stream().map(WhitelistFeeEntry::fromStackItem).collect(Collectors.toList());
    }

}
