package io.neow3j.contract;

import io.neow3j.constants.InteropServiceCode;
import io.neow3j.contract.exceptions.UnexpectedReturnTypeException;
import io.neow3j.model.types.StackItemType;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.methods.response.StackItem;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a Policy contract and provides methods to invoke it.
 */
public class PolicyContract extends SmartContract {

    private static final String NAME = "Policy";

    public static final ScriptHash SCRIPT_HASH = ScriptHash.fromScript(
            new ScriptBuilder()
                    .pushData(NAME)
                    .sysCall(InteropServiceCode.NEO_NATIVE_CALL)
                    .toArray());

    private static final String GET_MAX_TRANSACTIONS_PER_BLOCK = "getMaxTransactionsPerBlock";
    private static final String GET_MAX_BLOCK_SIZE = "getMaxBlockSize";
    private static final String GET_MAX_BLOCK_SYSTEM_FEE = "getMaxBlockSystemFee";
    private static final String GET_FEE_PER_BYTE = "getFeePerByte";
    private static final String GET_BLOCKED_ACCOUNTS = "getBlockedAccounts";
    private static final String SET_MAX_BLOCK_SIZE = "setMaxBlockSize";
    private static final String SET_MAX_TX_PER_BLOCK = "setMaxTransactionsPerBlock";
    private static final String SET_MAX_BLOCK_SYSTEM_FEE = "setMaxBlockSystemFee";
    private static final String SET_FEE_PER_BYTE = "setFeePerByte";
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
    public Integer getFeePerByte() throws IOException {
        return callFuncReturningInt(GET_FEE_PER_BYTE).intValue();
    }

    /**
     * Gets the list of Accounts that are blocked.
     *
     * @return list of blocked Accounts.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public List<ScriptHash> getBlockedAccounts() throws IOException {
        StackItem arrayItem = callInvokeFunction(GET_BLOCKED_ACCOUNTS)
                .getInvocationResult().getStack().get(0);

        if (!arrayItem.getType().equals(StackItemType.ARRAY)) {
            throw new UnexpectedReturnTypeException(arrayItem.getType(), StackItemType.ARRAY);
        }

        return arrayItem.asArray().getValue()
                .stream().map(ac -> ScriptHash.fromAddress(ac.asByteString().getAsAddress()))
                .collect(Collectors.toList());
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

    public TransactionBuilder setMaxBlockSystemFee(BigInteger maxBlockSystemFee){
        return invokeFunction(SET_MAX_BLOCK_SYSTEM_FEE, ContractParameter.integer(maxBlockSystemFee));
    }

    /**
     * Creates a transaction script to set the fee per byte and initializes a
     * {@link TransactionBuilder} based on this script.
     *
     * @param fee    The fee per byte.
     * @return A {@link TransactionBuilder}.
     */
    public TransactionBuilder setFeePerByte(Integer fee) {
        return invokeFunction(SET_FEE_PER_BYTE, ContractParameter.integer(fee));
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
     * @param accountToUnblock The account to unblocked.
     * @return A {@link TransactionBuilder}.
     */
    public TransactionBuilder unblockAccount(ScriptHash accountToUnblock) {
        return invokeFunction(UNBLOCK_ACCOUNT, ContractParameter.hash160(accountToUnblock));
    }
}
