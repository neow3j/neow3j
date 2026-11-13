package io.neow3j.transaction;

import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.Request;
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
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.time.Duration;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
class TransactionSubmissionTest {

    private static final Hash256 TX_ID = new Hash256("0x830816f0c801bcabf919dfa1a90d7b9a4f867482cb4d18d0631a5aa6daefab6a");

    @Test
    void exposesTransactionAndSubmissionResponse() {
        Neow3j neow3j = mock(Neow3j.class);
        NeoSendRawTransaction response = sendResponse(TX_ID);
        Transaction tx = transaction(neow3j);
        TransactionSubmission submission = new TransactionSubmission(tx, response, BigInteger.TEN);

        assertSame(tx, submission.getTransaction());
        assertSame(response, submission.getSendResponse());
        assertFalse(submission.hasError());
        assertNull(submission.getError());
        assertEquals(TX_ID, submission.getSendRawTransaction().getHash());
        assertEquals(TX_ID, submission.getTxId());
    }

    @Test
    void returnsLocalTransactionIdWhenResponseHasNoHash() {
        Transaction tx = transaction(mock(Neow3j.class));
        TransactionSubmission submission = new TransactionSubmission(tx, new NeoSendRawTransaction(), BigInteger.ZERO);

        assertEquals(tx.getTxId(), submission.getTxId());
    }

    @Test
    void rejectsNonPositiveAwaitDuration() {
        TransactionSubmission submission = submission(mock(Neow3j.class), sendResponse(TX_ID));

        assertThrows(IllegalArgumentException.class, () -> submission.await(Duration.ZERO));
        assertThrows(IllegalArgumentException.class, () -> submission.await(Duration.ofSeconds(-1)));
        assertThrows(IllegalArgumentException.class, () -> submission.await(null));
    }

    @Test
    void getsBlockHeightAndTime() throws IOException {
        Neow3j neow3j = mock(Neow3j.class);
        TransactionSubmission submission = submission(neow3j, sendResponse(TX_ID));
        NeoGetTransactionHeight heightResponse = new NeoGetTransactionHeight();
        heightResponse.setResult(BigInteger.valueOf(123));
        Request<?, NeoGetTransactionHeight> heightRequest = mock(Request.class);
        when(heightRequest.send()).thenReturn(heightResponse);
        doReturn(heightRequest).when(neow3j).getTransactionHeight(TX_ID);

        NeoBlock block = new NeoBlock(null, 0L, 0, null, null, 987654321L, "7F8EEE652D4BC959", 123, 0,
                "nonce", null, Collections.emptyList(), 1, null);
        NeoGetBlock blockResponse = new NeoGetBlock();
        blockResponse.setResult(block);
        Request<?, NeoGetBlock> blockRequest = mock(Request.class);
        when(blockRequest.send()).thenReturn(blockResponse);
        doReturn(blockRequest).when(neow3j).getBlock(BigInteger.valueOf(123), false);

        assertEquals(BigInteger.valueOf(123), submission.getBlockHeight());
        assertEquals(987654321L, submission.getBlockTime());
    }

    @Test
    void returnsNullHeightWhenTransactionIsNotIncluded() throws IOException {
        Neow3j neow3j = mock(Neow3j.class);
        TransactionSubmission submission = submission(neow3j, sendResponse(TX_ID));
        NeoGetTransactionHeight response = new NeoGetTransactionHeight();
        response.setError(new Response.Error(-100, "Unknown transaction"));
        Request<?, NeoGetTransactionHeight> request = mock(Request.class);
        when(request.send()).thenReturn(response);
        doReturn(request).when(neow3j).getTransactionHeight(TX_ID);

        assertNull(submission.getBlockHeight());
        assertNull(submission.getBlockTime());
    }

    @Test
    void fetchesApplicationLogAndReportsHaltState() throws IOException {
        Neow3j neow3j = mock(Neow3j.class);
        TransactionSubmission submission = submission(neow3j, sendResponse(TX_ID));
        NeoApplicationLog.Execution execution = new NeoApplicationLog.Execution("Application", NeoVMStateType.HALT,
                null, "0", Collections.emptyList(), Collections.emptyList());
        NeoApplicationLog log = new NeoApplicationLog(TX_ID, Collections.singletonList(execution));
        NeoGetApplicationLog response = new NeoGetApplicationLog();
        response.setResult(log);
        Request<?, NeoGetApplicationLog> request = mock(Request.class);
        when(request.send()).thenReturn(response);
        doReturn(request).when(neow3j).getApplicationLog(TX_ID);

        assertSame(log, submission.getApplicationLog());
        assertTrue(submission.hasHalted());

        execution = new NeoApplicationLog.Execution("Application", NeoVMStateType.FAULT, "fault", "0",
                Collections.emptyList(), Collections.emptyList());
        response.setResult(new NeoApplicationLog(TX_ID, Collections.singletonList(execution)));
        assertFalse(submission.hasHalted());
    }

    @Test
    void awaitWaitsForInclusionAndPropagatesSubmissionError() throws IOException {
        Neow3j neow3j = mock(Neow3j.class);
        TransactionSubmission submission = submission(neow3j, sendResponse(TX_ID));
        NeoGetTransactionHeight height = new NeoGetTransactionHeight();
        height.setResult(BigInteger.ONE);
        Request<?, NeoGetTransactionHeight> request = mock(Request.class);
        when(request.send()).thenReturn(height);
        doReturn(request).when(neow3j).getTransactionHeight(TX_ID);

        assertSame(submission, submission.await());

        NeoSendRawTransaction rejected = new NeoSendRawTransaction();
        rejected.setError(new Response.Error(-1, "Rejected"));
        TransactionSubmission rejectedSubmission = submission(neow3j, rejected);
        assertTrue(rejectedSubmission.hasError());
        assertNotNull(rejectedSubmission.getError());
        assertThrows(RpcResponseErrorException.class, rejectedSubmission::await);
    }

    @Test
    void awaitAcceptsCustomDuration() throws IOException {
        Neow3j neow3j = mock(Neow3j.class);
        TransactionSubmission submission = submission(neow3j, sendResponse(TX_ID));
        NeoGetTransactionHeight height = new NeoGetTransactionHeight();
        height.setResult(BigInteger.ONE);
        Request<?, NeoGetTransactionHeight> request = mock(Request.class);
        when(request.send()).thenReturn(height);
        doReturn(request).when(neow3j).getTransactionHeight(TX_ID);

        assertSame(submission, submission.await(Duration.ofSeconds(2)));
    }

    private static TransactionSubmission submission(Neow3j neow3j, NeoSendRawTransaction response) {
        return new TransactionSubmission(transaction(neow3j), response, BigInteger.ZERO);
    }

    private static Transaction transaction(Neow3j neow3j) {
        return new Transaction(neow3j, (byte) 0, 0, 0, Collections.emptyList(), 0, 0, Collections.emptyList(),
                new byte[0], Collections.emptyList());
    }

    private static NeoSendRawTransaction sendResponse(Hash256 txId) {
        NeoSendRawTransaction response = new NeoSendRawTransaction();
        response.setResult(new NeoSendRawTransaction.RawTransaction(txId));
        return response;
    }
}
