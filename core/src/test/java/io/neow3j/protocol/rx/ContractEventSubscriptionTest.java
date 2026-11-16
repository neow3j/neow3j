package io.neow3j.protocol.rx;

import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.Neow3jService;
import io.neow3j.protocol.core.JsonRpc2_0Neow3j;
import io.neow3j.protocol.core.Response;
import io.neow3j.protocol.core.response.NeoApplicationLog;
import io.neow3j.protocol.core.response.NeoBlock;
import io.neow3j.protocol.core.response.NeoBlockCount;
import io.neow3j.protocol.core.response.NeoGetApplicationLog;
import io.neow3j.protocol.core.response.NeoGetBlock;
import io.neow3j.protocol.core.response.NeoGetVersion.NeoVersion.Protocol;
import io.neow3j.protocol.core.response.Notification;
import io.neow3j.protocol.core.response.Transaction;
import io.neow3j.protocol.exceptions.RpcResponseErrorException;
import io.neow3j.types.Hash160;
import io.neow3j.types.Hash256;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static io.neow3j.protocol.Neow3jConfig.defaultNeow3jConfig;
import static io.neow3j.types.NeoVMStateType.HALT;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ContractEventSubscriptionTest {

    private static final Hash160 CONTRACT = new Hash160("969a77db482f74ce27105f760efa139223431394");
    private static final Hash160 OTHER_CONTRACT = new Hash160();
    private static final Hash256 FIRST_TX = new Hash256();
    private static final Hash256 SECOND_TX = new Hash256(
            "7da6ae7ff9d0b7af3d32f3a2feb2aa96c2a27ef8b651f9a132cfaad6ef20724c");
    private static final long POLLING_INTERVAL = 1000;

    private final Neow3jService service = mock(Neow3jService.class);
    private final ScheduledExecutorService executor = mock(ScheduledExecutorService.class);
    private final ScheduledFuture<?> polling = mock(ScheduledFuture.class);
    private Neow3j neow3j;

    @BeforeEach
    void setUp() {
        doReturn(polling).when(executor).scheduleAtFixedRate(any(Runnable.class), eq(0L), eq(POLLING_INTERVAL),
                eq(MILLISECONDS));
        Protocol protocol = new Protocol();
        protocol.setNetwork(768L);
        neow3j = new JsonRpc2_0Neow3j(service, defaultNeow3jConfig()
                .setPollingInterval(POLLING_INTERVAL).setScheduledExecutorService(executor), protocol);
    }

    @Test
    void emitsOnlyMatchingNotificationsFromNewBlocks() throws IOException {
        Observable<Notification> events = neow3j.subscribeToEvents(CONTRACT);
        verifyNoInteractions(service, executor);

        Notification first = new Notification(CONTRACT, "First", null);
        Notification unrelated = new Notification(OTHER_CONTRACT, "First", null);
        Notification second = new Notification(CONTRACT, "Second", null);
        Notification third = new Notification(CONTRACT, "Third", null);

        when(service.send(any(), eq(NeoBlockCount.class))).thenReturn(blockCount(1), blockCount(3));
        stubBlock(1, block());
        stubBlock(2, block(transaction(FIRST_TX), transaction(SECOND_TX)));
        stubApplicationLog(FIRST_TX, applicationLog(FIRST_TX, execution(first, unrelated), execution(second)));
        stubApplicationLog(SECOND_TX, applicationLog(SECOND_TX, execution(third)));

        TestObserver<Notification> subscriber = events.test();
        Runnable poll = scheduledPoll();
        poll.run();
        subscriber.assertNoValues().assertNoErrors();

        poll.run();
        subscriber.assertValues(first, second, third).assertNoErrors().assertNotComplete();
        subscriber.dispose();
        verify(polling).cancel(false);
    }

    @Test
    void reportsApplicationLogRpcErrors() throws IOException {
        when(service.send(any(), eq(NeoBlockCount.class))).thenReturn(blockCount(1), blockCount(2));
        stubBlock(1, block(transaction(FIRST_TX)));
        Response.Error error = new Response.Error();
        error.setCode(-32601);
        error.setMessage("Method not found");
        NeoGetApplicationLog response = new NeoGetApplicationLog();
        response.setError(error);
        stubApplicationLog(FIRST_TX, response);

        TestObserver<Notification> subscriber = neow3j.subscribeToEvents(CONTRACT).test();
        Runnable poll = scheduledPoll();
        poll.run();
        poll.run();

        subscriber.assertNoValues().assertError(RpcResponseErrorException.class);
        verify(polling).cancel(false);
    }

    @Test
    void rejectsNullContractHash() {
        assertThrows(IllegalArgumentException.class, () -> neow3j.subscribeToEvents(null));
        verifyNoInteractions(service, executor);
    }

    private Runnable scheduledPoll() {
        ArgumentCaptor<Runnable> poll = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).scheduleAtFixedRate(poll.capture(), eq(0L), eq(POLLING_INTERVAL), eq(MILLISECONDS));
        return poll.getValue();
    }

    private void stubBlock(long index, NeoGetBlock response) throws IOException {
        doReturn(response).when(service).send(argThat(request -> request.getMethod().equals("getblock")
                && request.getParams().equals(asList(BigInteger.valueOf(index), 1))), eq(NeoGetBlock.class));
    }

    private void stubApplicationLog(Hash256 hash, NeoGetApplicationLog response) throws IOException {
        doReturn(response).when(service).send(argThat(request -> request.getParams().equals(singletonList(hash))),
                eq(NeoGetApplicationLog.class));
    }

    private static NeoBlockCount blockCount(long count) {
        NeoBlockCount response = new NeoBlockCount();
        response.setResult(BigInteger.valueOf(count));
        return response;
    }

    private static NeoGetBlock block(Transaction... transactions) {
        NeoBlock block = mock(NeoBlock.class);
        when(block.getTransactions()).thenReturn(asList(transactions));
        NeoGetBlock response = new NeoGetBlock();
        response.setResult(block);
        return response;
    }

    private static Transaction transaction(Hash256 hash) {
        Transaction transaction = mock(Transaction.class);
        when(transaction.getHash()).thenReturn(hash);
        return transaction;
    }

    private static NeoGetApplicationLog applicationLog(Hash256 hash, NeoApplicationLog.Execution... executions) {
        NeoGetApplicationLog response = new NeoGetApplicationLog();
        response.setResult(new NeoApplicationLog(hash, asList(executions)));
        return response;
    }

    private static NeoApplicationLog.Execution execution(Notification... notifications) {
        return new NeoApplicationLog.Execution("Application", HALT, null, "0", emptyList(), asList(notifications));
    }

}
