package io.neow3j.contract;

import io.neow3j.constants.InteropServiceCode;
import io.neow3j.contract.exceptions.UnexpectedReturnTypeException;
import io.neow3j.model.types.StackItemType;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.methods.response.StackItem;
import io.neow3j.transaction.Signer;
import io.neow3j.wallet.Wallet;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a Policy contract and provides methods to invoke it.
 */
public class PolicyContract extends SmartContract {

    private static final String NAME = "Policy";

    public static final ScriptHash SCRIPT_HASH = ScriptHash.fromScript(
            new ScriptBuilder().pushData(NAME).sysCall(InteropServiceCode.NEO_NATIVE_CALL)
                    .toArray());

    private static final String GET_MAX_TRANSACTIONS_PER_BLOCK = "getMaxTransactionsPerBlock";
    private static final String GET_FEE_PER_BYTE = "getFeePerByte";
    private static final String GET_BLOCKED_ACCOUNTS = "getBlockedAccounts";
    private static final String SET_FEE_PER_BYTE = "setFeePerByte";
    private static final String SET_MAX_TX_PER_BLOCK = "setMaxTransactionsPerBlock";
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
        StackItem arrayItem = callInvokeFunction(GET_BLOCKED_ACCOUNTS).getInvocationResult().getStack()
                .get(0);
        if (!arrayItem.getType().equals(StackItemType.ARRAY)) {
            throw new UnexpectedReturnTypeException(arrayItem.getType(), StackItemType.ARRAY);
        }

        return arrayItem.asArray().getValue()
                .stream().map(ac -> ScriptHash.fromAddress(ac.asByteString().getAsAddress()))
                .collect(Collectors.toList());
    }

    /**
     * Creates a transaction script to set the fee per byte and initializes a
     * {@link TransactionBuilder} based on this script.
     *
     * @param fee    The fee per byte.
     * @param wallet The wallet that contains the account authorised to invoke the policy contract.
     * @param signer The authorised account.
     * @return A {@link TransactionBuilder}.
     */
    public TransactionBuilder setFeePerByte(Integer fee, Wallet wallet, ScriptHash signer) {

        return invokeFunction(SET_FEE_PER_BYTE, ContractParameter.integer(fee))
                .wallet(wallet)
                .signers(Signer.calledByEntry(signer));
    }

    /**
     * Creates a transaction script to set the maximal amount of transactions per block and initializes
     * a {@link TransactionBuilder} based on this script.
     *
     * @param maxTxPerBlock The maximal allowed number of transactions per block.
     * @param wallet        The wallet that contains the account authorised to invoke the policy
     *                      contract.
     * @param signer        The authorised account.
     * @return A {@link TransactionBuilder}.
     */
    public TransactionBuilder setMaxTransactionsPerBlock(Integer maxTxPerBlock, Wallet wallet,
            ScriptHash signer) {

        return invokeFunction(SET_MAX_TX_PER_BLOCK, ContractParameter.integer(maxTxPerBlock))
                .wallet(wallet)
                .signers(Signer.calledByEntry(signer));
    }

    /**
     * Creates a transaction script to block an account in the neo-network and initializes
     * a {@link TransactionBuilder} based on this script.
     *
     * @param accountToBlock The account to block.
     * @param wallet         The wallet that contains the account authorised to invoke the policy
     *                       contract.
     * @param signer         The authorised account.
     * @return A {@link TransactionBuilder}.
     */
    public TransactionBuilder blockAccount(ScriptHash accountToBlock, Wallet wallet,
            ScriptHash signer) {

        return invokeFunction(BLOCK_ACCOUNT, ContractParameter.hash160(accountToBlock))
                .wallet(wallet)
                .signers(Signer.calledByEntry(signer));
    }

    /**
     * Creates a transaction script to unblock an account in the neo-network and initializes
     * a {@link TransactionBuilder} based on this script.
     *
     * @param accountToUnblock The account to unblocked.
     * @param wallet           The wallet that contains the account authorised to invoke the policy
     *                         contract.
     * @param signer           The authorised account.
     * @return A {@link TransactionBuilder}.
     */
    public TransactionBuilder unblockAccount(ScriptHash accountToUnblock, Wallet wallet,
            ScriptHash signer) {

        return invokeFunction(UNBLOCK_ACCOUNT,
                ContractParameter.hash160(accountToUnblock))
                .wallet(wallet)
                .signers(Signer.calledByEntry(signer));
    }
}
