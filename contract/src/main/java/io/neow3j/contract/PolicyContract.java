package io.neow3j.contract;

import io.neow3j.constants.InteropServiceCode;
import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.protocol.Neow3j;

import java.io.IOException;
import java.util.List;

/**
 * Represents a Policy contract and provides methods to invoke it.
 */
public class PolicyContract extends SmartContract{

    private static final ScriptHash SCRIPT_HASH = ScriptHash.fromScript(
            new ScriptBuilder().sysCall(InteropServiceCode.NEO_NATIVE_POLICY).toArray());

    private static final String GET_MAX_TX_COUNT_PER_BLOCK = "getMaxTransactionsPerBlock";
    private static final String GET_FEE_PER_BYTE = "getFeePerByte";
    private static final String GET_BLOCKED_ACCOUNTS = "getBlockedAccounts";

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
    public Integer getMaxTxPerBlock() throws IOException {
        return callFuncReturningInt(GET_MAX_TX_COUNT_PER_BLOCK).intValue();
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
     * Gets the list of {@link ECPublicKey} that are blocked.
     *
     * @return list of blocked {@link ECPublicKey}.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public List<ECPublicKey> getBlockedAccounts() throws IOException {
        return callFunctionReturningListOfPublicKeys(GET_BLOCKED_ACCOUNTS);
    }
}
