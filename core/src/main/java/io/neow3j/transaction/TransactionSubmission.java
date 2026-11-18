package io.neow3j.transaction;

import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.Response;
import io.neow3j.protocol.core.response.NeoApplicationLog;
import io.neow3j.protocol.core.response.NeoBlock;
import io.neow3j.protocol.core.response.NeoGetApplicationLog;
import io.neow3j.protocol.core.response.NeoGetBlock;
import io.neow3j.protocol.core.response.NeoGetTransactionHeight;
import io.neow3j.protocol.core.response.NeoSendRawTransaction;
import io.neow3j.protocol.exceptions.RpcResponseErrorException;
import io.neow3j.types.Hash256;
import io.neow3j.types.NeoVMStateType;
import io.reactivex.Observable;
import io.reactivex.functions.Predicate;

import java.io.IOException;
import java.math.BigInteger;
import java.time.Duration;

import static io.neow3j.utils.Await.waitUntilTransactionIsExecuted;

/**
 * The result of submitting a transaction to a Neo node.
 */
public class TransactionSubmission {

    private static final int LEGACY_NOT_FOUND = -100;

    private static final int UNKNOWN_BLOCK = -101;

    private static final int UNKNOWN_TRANSACTION = -103;

    private static final int INVALID_PARAMS = -32602;

    private static final String UNKNOWN_TRANSACTION_OR_BLOCK_HASH = "Unknown transaction/blockhash";

    private final Transaction transaction;

    private final NeoSendRawTransaction sendResponse;

    private final BigInteger blockCountWhenSent;

    TransactionSubmission(Transaction transaction, NeoSendRawTransaction sendResponse, BigInteger blockCountWhenSent) {
        this.transaction = transaction;
        this.sendResponse = sendResponse;
        this.blockCountWhenSent = blockCountWhenSent;
    }

    /**
     * @return the submitted transaction.
     */
    public Transaction getTransaction() {
        return transaction;
    }

    /**
     * @return the raw response returned by the node's {@code sendrawtransaction} RPC.
     */
    public NeoSendRawTransaction getSendResponse() {
        return sendResponse;
    }

    /**
     * @return {@code true} if the submission response contains an error; {@code false} otherwise.
     */
    public boolean hasError() {
        return sendResponse.hasError();
    }

    /**
     * @return the node's error, or {@code null} if the submission response has no error.
     */
    public Response.Error getError() {
        return sendResponse.getError();
    }

    /**
     * @return the raw transaction result, or {@code null} if the response has no result.
     */
    public NeoSendRawTransaction.RawTransaction getSendRawTransaction() {
        return sendResponse.getSendRawTransaction();
    }

    /**
     * @return the transaction ID returned by the node, or the locally calculated ID if submission was rejected or
     * the response has no hash.
     */
    public Hash256 getTxId() {
        if (sendResponse.hasError()) {
            return transaction.getTxId();
        }
        NeoSendRawTransaction.RawTransaction result = sendResponse.getSendRawTransaction();
        return result == null || result.getHash() == null ? transaction.getTxId() : result.getHash();
    }

    /**
     * @return this submission after the node reports it as included in a block.
     * @throws io.neow3j.protocol.exceptions.RpcResponseErrorException if the node rejected the submission.
     * @throws org.awaitility.core.ConditionTimeoutException           if the transaction is not included before the
     *                                                                 default wait timeout.
     */
    public TransactionSubmission await() {
        sendResponse.getSendRawTransaction();
        waitUntilTransactionIsExecuted(getTxId(), transaction.neow3j);
        return this;
    }

    /**
     * @param timeout the maximum duration to wait.
     * @return this submission after the node reports it as included in a block.
     * @throws io.neow3j.protocol.exceptions.RpcResponseErrorException if the node rejected the submission.
     * @throws org.awaitility.core.ConditionTimeoutException           if the transaction is not included within
     *                                                                 {@code timeout}.
     * @throws IllegalArgumentException                                if {@code timeout} is null, zero, or negative.
     */
    public TransactionSubmission await(Duration timeout) {
        sendResponse.getSendRawTransaction();
        waitUntilTransactionIsExecuted(getTxId(), transaction.neow3j, timeout);
        return this;
    }

    /**
     * @return an observable that emits the containing block height and then completes.
     */
    public Observable<Long> track() {
        Neow3j neow3j = transaction.neow3j;
        Predicate<NeoGetBlock> containsTransaction = response -> response.getBlock().getTransactions() != null &&
                response.getBlock().getTransactions().stream().anyMatch(tx -> tx.getHash().equals(getTxId()));
        return neow3j.catchUpToLatestAndSubscribeToNewBlocksObservable(blockCountWhenSent, true)
                .takeUntil(containsTransaction).filter(containsTransaction)
                .map(response -> response.getBlock().getIndex());
    }

    /**
     * @return the containing block height, or {@code null} if the node does not report the transaction as included.
     * @throws IOException               if an error occurs while communicating with the node.
     * @throws RpcResponseErrorException if the lookup returns an unexpected RPC error.
     */
    public BigInteger getBlockHeight() throws IOException {
        NeoGetTransactionHeight response = transaction.neow3j.getTransactionHeight(getTxId()).send();
        if (isNotFoundRpcError(response.getError(), UNKNOWN_TRANSACTION, "Unknown transaction")) {
            return null;
        }
        return response.getHeight();
    }

    /**
     * @return the block timestamp in milliseconds, or {@code null} if the transaction is not included or the block
     * is unavailable.
     * @throws IOException               if an error occurs while communicating with the node.
     * @throws RpcResponseErrorException if the height or block lookup returns an unexpected RPC error.
     */
    public Long getBlockTime() throws IOException {
        BigInteger height = getBlockHeight();
        if (height == null) {
            return null;
        }
        NeoGetBlock response = transaction.neow3j.getBlock(height, false).send();
        if (isNotFoundRpcError(response.getError(), UNKNOWN_BLOCK, "Unknown block")) {
            return null;
        }
        NeoBlock block = response.getBlock();
        return block == null ? null : block.getTime();
    }

    /**
     * @return the application log, or {@code null} if it is not available.
     * @throws IOException               if an error occurs while communicating with the node.
     * @throws RpcResponseErrorException if the lookup returns an unexpected RPC error.
     */
    public NeoApplicationLog getApplicationLog() throws IOException {
        NeoGetApplicationLog response = transaction.neow3j.getApplicationLog(getTxId()).send();
        Response.Error error = response.getError();
        // Neo's ApplicationLogs plugin reports missing logs via Invalid params with a specific JSON data string.
        if (isNotFoundRpcError(error, UNKNOWN_TRANSACTION, UNKNOWN_TRANSACTION_OR_BLOCK_HASH)
                || (error != null && error.getCode() == INVALID_PARAMS
                && ("\"" + UNKNOWN_TRANSACTION_OR_BLOCK_HASH + "\"").equals(error.getData()))) {
            return null;
        }
        return response.getApplicationLog();
    }

    /**
     * @return {@code true} if the first execution halted; {@code false} if the log is unavailable, has no executions,
     * or the first execution did not halt.
     * @throws IOException               if an error occurs while communicating with the node.
     * @throws RpcResponseErrorException if the application log lookup returns an unexpected RPC error.
     */
    public boolean hasHalted() throws IOException {
        NeoApplicationLog log = getApplicationLog();
        return log != null && !log.getExecutions().isEmpty() &&
                log.getFirstExecution().getState() == NeoVMStateType.HALT;
    }

    /**
     * Checks whether an RPC error reports that the requested resource was not found. Older Neo nodes use the
     * same error code ({@code -100}) for multiple resources, so a legacy response must also match the expected
     * message.
     *
     * @param error                 the RPC error, or {@code null} for a successful response.
     * @param notFoundCode          the current Neo RPC error code for the missing resource.
     * @param legacyNotFoundMessage the expected message for the missing resource on older nodes.
     * @return true if the error matches the current code or the legacy code and message.
     */
    private static boolean isNotFoundRpcError(Response.Error error, int notFoundCode, String legacyNotFoundMessage) {
        return error != null && (error.getCode() == notFoundCode
                || (error.getCode() == LEGACY_NOT_FOUND && legacyNotFoundMessage.equals(error.getMessage())));
    }
}
