package io.neow3j.contract;

import io.neow3j.constants.InteropServiceCode;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.protocol.Neow3j;
import org.omg.CORBA.PRIVATE_MEMBER;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

/**
 * Represents a Policy contract and provides methods to invoke it.
 */
public class PolicyContract extends SmartContract{

    // TODO: 24.06.20 Michael: check if function names are correct.
    private static final String GET_MAX_TX_COUNT_PER_BLOCK = "getMaxTransactionPerBlock";
    private static final String GET_FEE_PER_BYTE = "GetFeePerByte";
    private static final String GET_BLOCKED_ACCOUNTS = "getBlockedAccounts";
    private static final String SET_MAX_TX_PER_BLOCK = "setMaxTransactionsPerBlock";
    private static final String SET_FEE_PER_BYTE = "setFeePerByte";
    private static final String BLOCK_ACCOUNT = "blockAccount";
    private static final String UNBLOCK_ACCOUNT = "unblockAccount";
    private static final ScriptHash SCRIPT_HASH = ScriptHash.fromScript(
            new ScriptBuilder().sysCall(InteropServiceCode.NEO_NATIVE_POLICY).toArray());

    private Integer MaxTxCountPerBlock;
    private Integer FeePerByte;
    private List<ScriptHash> blacklist;

    /**
     * Constructs a new <tt>NeoToken</tt> that uses the given {@link Neow3j} instance for
     * invocations.
     *
     * @param neow The {@link Neow3j} instance to use for invocations.
     */
    public PolicyContract(Neow3j neow) {
        super(SCRIPT_HASH, neow);
    }

    public Integer getMaxTxPerBlock() throws IOException {
        if (this.MaxTxCountPerBlock == null) {
            this.MaxTxCountPerBlock = callFuncReturningInt(GET_MAX_TX_COUNT_PER_BLOCK).intValue();
        }
        return this.MaxTxCountPerBlock;
    }

    public Integer getFeePerByte() throws IOException {
        if (this.FeePerByte == null) {
            this.FeePerByte = callFuncReturningInt(GET_FEE_PER_BYTE).intValue();
        }
        return this.FeePerByte;
    }

    // TODO: 24.06.20 Michael: add missing methods
}
