package io.neow3j.protocol.rx;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.Neow3jConfig;
import io.neow3j.protocol.Neow3jService;
import io.neow3j.protocol.core.Request;
import io.neow3j.protocol.core.response.NeoBlock;
import io.neow3j.protocol.core.response.NeoBlockCount;
import io.neow3j.protocol.core.response.NeoGetBlock;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.stubbing.OngoingStubbing;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JsonRpc2_0RxTest {

    private Neow3j neow3j;

    private Neow3jService neow3jService;

    @BeforeAll
    public void setUp() {
        neow3jService = mock(Neow3jService.class);
        neow3j = Neow3j.build(neow3jService, new Neow3jConfig()
                .setPollingInterval(1000)
                .setScheduledExecutorService(Executors.newSingleThreadScheduledExecutor()));
    }

    @Test
    public void testReplayBlocksObservable() throws Exception {

        List<NeoGetBlock> neoGetBlocks = Arrays
            .asList(createBlock(0), createBlock(1), createBlock(2));

        OngoingStubbing<NeoGetBlock> stubbing =
            when(neow3jService.send(any(Request.class), eq(NeoGetBlock.class)));
        for (NeoGetBlock neoGetBlock : neoGetBlocks) {
            stubbing = stubbing.thenReturn(neoGetBlock);
        }

        Observable<NeoGetBlock> observable = neow3j.replayBlocksObservable(
                BigInteger.ZERO, BigInteger.valueOf(2), false);

        CountDownLatch transactionLatch = new CountDownLatch(neoGetBlocks.size());
        CountDownLatch completedLatch = new CountDownLatch(1);

        List<NeoGetBlock> results = new ArrayList<>(neoGetBlocks.size());

        Disposable disposable = observable.subscribe(
            result -> {
                results.add(result);
                transactionLatch.countDown();
            },
            throwable -> fail(throwable.getMessage()),
            () -> completedLatch.countDown());

        // just to be in the safe side, we add a timeout
        completedLatch.await(5, TimeUnit.SECONDS);
        assertThat(results, equalTo(neoGetBlocks));

        disposable.dispose();

        assertTrue(disposable.isDisposed());
        assertThat(transactionLatch.getCount(), is(0L));
    }

    @Test
    public void testReplayBlocksDescendingObservable() throws Exception {

        List<NeoGetBlock> neoGetBlocks = Arrays
            .asList(createBlock(2), createBlock(1), createBlock(0));

        OngoingStubbing<NeoGetBlock> stubbing =
            when(neow3jService.send(any(Request.class), eq(NeoGetBlock.class)));
        for (NeoGetBlock neoGetBlock : neoGetBlocks) {
            stubbing = stubbing.thenReturn(neoGetBlock);
        }

        Observable<NeoGetBlock> observable = neow3j.replayBlocksObservable(
            BigInteger.ZERO, BigInteger.valueOf(2), false, false);

        CountDownLatch transactionLatch = new CountDownLatch(neoGetBlocks.size());
        CountDownLatch completedLatch = new CountDownLatch(1);

        List<NeoGetBlock> results = new ArrayList<>(neoGetBlocks.size());
        Disposable disposable = observable.subscribe(
            result -> {
                results.add(result);
                transactionLatch.countDown();
            },
            throwable -> fail(throwable.getMessage()),
            () -> completedLatch.countDown());

        // just to be in the safe side, we add a timeout
        completedLatch.await(5, TimeUnit.SECONDS);
        assertThat(results, equalTo(neoGetBlocks));

        disposable.dispose();

        assertTrue(disposable.isDisposed());
        assertThat(transactionLatch.getCount(), is(0L));
    }

    @Test
    public void testCatchUpToLatestAndSubscribeToNewBlockObservable() throws Exception {

        List<NeoGetBlock> expected = Arrays.asList(
                // past blocks:
                createBlock(0),
                createBlock(1),
                createBlock(2),
                createBlock(3),
                // later blocks:
                createBlock(4),
                createBlock(5),
                createBlock(6)
        );

        OngoingStubbing<NeoGetBlock> stubbingNeoGetBlock =
                when(neow3jService.send(any(Request.class), eq(NeoGetBlock.class)));

        for (int i = 0; i < 7; i++) {
            stubbingNeoGetBlock = stubbingNeoGetBlock.thenReturn(expected.get(i));
        }

        OngoingStubbing<NeoBlockCount> stubbingNeoBlockCount =
                when(neow3jService.send(any(Request.class), eq(NeoBlockCount.class)));

        NeoBlockCount neoBlockCount = new NeoBlockCount();
        BigInteger currentBlock = BigInteger.valueOf(4);
        neoBlockCount.setResult(currentBlock);
        stubbingNeoBlockCount.thenReturn(neoBlockCount);

        Observable<NeoGetBlock> observable = neow3j
                .catchUpToLatestAndSubscribeToNewBlocksObservable(BigInteger.ZERO, false);

        CountDownLatch transactionLatch = new CountDownLatch(expected.size());
        CountDownLatch completedLatch = new CountDownLatch(1);

        List<NeoGetBlock> results = new ArrayList<>(expected.size());

        Disposable disposable = observable.subscribe(
                result -> {
                    results.add(result);
                    transactionLatch.countDown();
                    System.out.println("TransactionLatch countDown");
                    System.out.println(result.getBlock());
                },
                throwable -> fail(throwable.getMessage()),
                () -> {
                    System.out.println("Completed");
                    completedLatch.countDown();
                });

        for (int i = 4; i < 7; i++) {
            Thread.sleep(2000);
            BigInteger added = neoBlockCount.getBlockCount().add(BigInteger.ONE);
            neoBlockCount.setResult(added);
            stubbingNeoBlockCount = stubbingNeoBlockCount.thenReturn(neoBlockCount);
        }

        completedLatch.await(15250, TimeUnit.MILLISECONDS);
        assertThat(results.size(), equalTo(expected.size()));
        assertThat(results, equalTo(expected));

        disposable.dispose();

        assertTrue(disposable.isDisposed());
        assertThat(transactionLatch.getCount(), is(0L));
    }

    @Test
    public void testSubscribeToNewBlockObservable() throws Exception {

        List<NeoGetBlock> expected = Arrays.asList(
            createBlock(0),
            createBlock(1),
            createBlock(2),
            createBlock(3)
        );

        OngoingStubbing<NeoGetBlock> stubbingNeoGetBlock =
            when(neow3jService.send(any(Request.class), eq(NeoGetBlock.class)));

        for (int i = 0; i < 4; i++) {
            stubbingNeoGetBlock = stubbingNeoGetBlock.thenReturn(expected.get(i));
        }

        NeoBlockCount neoBlockCount = new NeoBlockCount();
        BigInteger currentBlock = BigInteger.valueOf(0);
        neoBlockCount.setResult(currentBlock);
        OngoingStubbing<NeoBlockCount> stubbingNeoBlockCount =
            when(neow3jService.send(any(Request.class), eq(NeoBlockCount.class)))
                    .thenReturn(neoBlockCount);

        Observable<NeoGetBlock> observable = neow3j.subscribeToNewBlocksObservable(false);

        CountDownLatch transactionLatch = new CountDownLatch(expected.size());
        CountDownLatch completedLatch = new CountDownLatch(1);

        List<NeoGetBlock> results = new ArrayList<>(expected.size());

        Disposable disposable = observable.subscribe(
            result -> {
                results.add(result);
                transactionLatch.countDown();
                System.out.println("TransactionLatch countDown");
                System.out.println(result.getBlock());
            },
            throwable -> fail(throwable.getMessage()),
            () -> {
                System.out.println("Completed");
                completedLatch.countDown();
            });

        for (int i = 0; i < 4; i++) {
            Thread.sleep(2000);
            BigInteger added = neoBlockCount.getBlockCount().add(BigInteger.ONE);
            neoBlockCount.setResult(added);
            stubbingNeoBlockCount = stubbingNeoBlockCount.thenReturn(neoBlockCount);
        }

        completedLatch.await(15250, TimeUnit.MILLISECONDS);
        assertThat(results.size(), equalTo(expected.size()));
        assertThat(results, equalTo(expected));

        disposable.dispose();

        assertTrue(disposable.isDisposed());
        assertThat(transactionLatch.getCount(), is(0L));
    }

    @Test
    @Disabled("Ignored due to a missing feature. "
        + "A feature to buffer blocks that come out of order should be implemented on neow3j lib.")
    public void testCatchUpToLatestAndSubscribeToNewBlockObservable_NotContinuousBlocks()
        throws Exception {

        List<NeoGetBlock> expected = Arrays.asList(
            createBlock(0),
            createBlock(1),
            createBlock(2),
            createBlock(3),
            createBlock(4),
            createBlock(5),
            createBlock(6)
        );

        NeoGetBlock block7 = createBlock(7);

        List<NeoGetBlock> neoGetBlocks = Arrays.asList(
            // past blocks:
            expected.get(0),
            expected.get(1),
            expected.get(2),
            expected.get(3),
            // later blocks:
            expected.get(4),
            // missing expected.get(5)
            expected.get(6),
            block7
        );

        OngoingStubbing<NeoGetBlock> stubbingNeoGetBlock =
            when(neow3jService.send(any(Request.class), eq(NeoGetBlock.class)));

        for (int i = 0; i < 7; i++) {
            stubbingNeoGetBlock = stubbingNeoGetBlock.thenReturn(neoGetBlocks.get(i));
        }

        OngoingStubbing<NeoBlockCount> stubbingNeoBlockCount =
            when(neow3jService.send(any(Request.class), eq(NeoBlockCount.class)));

        NeoBlockCount neoBlockCount = new NeoBlockCount();
        BigInteger currentBlock = BigInteger.valueOf(4);
        neoBlockCount.setResult(currentBlock);
        stubbingNeoBlockCount.thenReturn(neoBlockCount);

        Observable<NeoGetBlock> observable = neow3j
            .catchUpToLatestAndSubscribeToNewBlocksObservable(BigInteger.ZERO, false);

        CountDownLatch transactionLatch = new CountDownLatch(expected.size());
        CountDownLatch completedLatch = new CountDownLatch(1);

        List<NeoGetBlock> results = new ArrayList<>(expected.size());

        Disposable disposable = observable.subscribe(
            result -> {
                results.add(result);
                transactionLatch.countDown();
                System.out.println("TransactionLatch countDown");
                System.out.println(result.getBlock());
            },
            throwable -> fail(throwable.getMessage()),
            () -> {
                System.out.println("Completed");
                completedLatch.countDown();
            });

        for (int i = 0; i < 4; i++) {
            Thread.sleep(2000);
            BigInteger added = BigInteger.valueOf(neoGetBlocks.get(i).getBlock().getIndex());
            neoBlockCount.setResult(added);
            stubbingNeoBlockCount = stubbingNeoBlockCount.thenReturn(neoBlockCount);
        }

        transactionLatch.await(15250, TimeUnit.MILLISECONDS);
        assertThat(results.size(), equalTo(expected.size()));
        // TODO: in the following assertion we should check if the content items of the
        // results var is the same as in the expected var
        // assertThat(results, equalTo(expected));

        disposable.dispose();

        completedLatch.await(1, TimeUnit.SECONDS);
        assertTrue(disposable.isDisposed());
    }

    private NeoGetBlock createBlock(int number) {
        NeoGetBlock neoGetBlock = new NeoGetBlock();
        NeoBlock block = new NeoBlock(null, 0L, 0, null, null, 123456789, number, 0, "nonce", null,
                null, 1, null);
        neoGetBlock.setResult(block);
        return neoGetBlock;
    }

}
