package io.neow3j.protocol.rx;

import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.polling.BlockIndexPolling;
import io.neow3j.protocol.core.response.NeoGetBlock;
import io.neow3j.protocol.core.response.Transaction;
import io.neow3j.utils.Observables;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;

import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.ScheduledExecutorService;

/**
 * neow3j reactive API implementation.
 */
public class JsonRpc2_0Rx {

    private final Neow3j neow3j;
    private final ScheduledExecutorService scheduledExecutorService;
    private final Scheduler scheduler;

    public JsonRpc2_0Rx(Neow3j neow3j, ScheduledExecutorService scheduledExecutorService) {
        this.neow3j = neow3j;
        this.scheduledExecutorService = scheduledExecutorService;
        this.scheduler = Schedulers.from(scheduledExecutorService);
    }

    /**
     * Creates an observable that emits new block index as they are produced by the Neo blockchain. The observable
     * polls the Neo node in the given {@code pollingInterval} to check for the latest block index and emits all
     * indexes since the last time it polled.
     *
     * @param pollingInterval The polling interval in milliseconds.
     * @return the block index observable.
     */
    public Observable<BigInteger> blockIndexObservable(long pollingInterval) {
        return Observable.create(subscriber ->
                new BlockIndexPolling().run(neow3j, subscriber, scheduledExecutorService, pollingInterval)
        );
    }

    /**
     * Creates an observable that emits new blocks as they are produced by the Neo blockchain. The observable
     * polls the Neo node in the given {@code pollingInterval} to check for the latest block and emits all
     * blocks since the last time it polled.
     *
     * @param fullTransactionObjects Whether to get block information with all transaction objects or just the block
     *                               header.
     * @param pollingInterval        The polling interval in milliseconds.
     * @return the block index observable.
     */
    public Observable<NeoGetBlock> blockObservable(boolean fullTransactionObjects, long pollingInterval) {
        return blockIndexObservable(pollingInterval)
                .flatMap(blockIndex -> neow3j.getBlock(blockIndex, fullTransactionObjects).observable());
    }

    /**
     * Creates an observable that emits blocks starting at {@code startBlockNumber} up to {@code endBlock} and then
     * stops.
     *
     * @param startBlock             The block index at which to start.
     * @param endBlock               The block index at which to stop.
     * @param fullTransactionObjects If the full transactions objects should be included in the blocks.
     * @param ascending              If the blocks should be emitted in ascending or descending order.
     * @return the block index observable.
     */
    public Observable<NeoGetBlock> replayBlocksObservable(BigInteger startBlock, BigInteger endBlock,
            boolean fullTransactionObjects, boolean ascending) {
        return replayBlocksObservableSync(startBlock, endBlock, fullTransactionObjects, ascending)
                // We use a scheduler to run this Observable asynchronously
                .subscribeOn(scheduler);
    }

    private Observable<NeoGetBlock> replayBlocksObservableSync(BigInteger startBlockNumber, BigInteger endBlockNumber,
            boolean fullTransactionObjects, boolean ascending) {

        if (ascending) {
            return Observables.range(startBlockNumber, endBlockNumber)
                    .flatMap(i -> neow3j.getBlock(i, fullTransactionObjects).observable());
        } else {
            return Observables.range(startBlockNumber, endBlockNumber, false)
                    .flatMap(i -> neow3j.getBlock(i, fullTransactionObjects).observable());
        }
    }

    /**
     * Creates an observable that emits blocks starting at {@code startBlockNumber} up to the most recent block and
     * continues emitting according {@code onCaughtUpObservable} after that (e.g., stops if {@code
     * onCaughtUpObservable} is {@code Observable.empty()}).
     *
     * @param startBlockIdx          The block index at which to start catching up.
     * @param fullTransactionObjects If the full transactions objects should be included in the blocks.
     * @param onCaughtUpObservable   The observable to use after it caught up.
     * @return the block observable.
     */
    public Observable<NeoGetBlock> catchUpToLatestBlockObservable(BigInteger startBlockIdx,
            boolean fullTransactionObjects, Observable<NeoGetBlock> onCaughtUpObservable) {

        return catchUpToLatestBlockObservableSync(
                startBlockIdx, fullTransactionObjects, onCaughtUpObservable)
                // We use a scheduler to run this Observable asynchronously
                .subscribeOn(scheduler);
    }

    private Observable<NeoGetBlock> catchUpToLatestBlockObservableSync(BigInteger startBlockIdx,
            boolean fullTransactionObjects, Observable<NeoGetBlock> onCaughtUpObservable) {

        BigInteger latestBlockIdx;
        try {
            latestBlockIdx = getLatestBlockIdx();
        } catch (IOException e) {
            return Observable.error(e);
        }

        if (startBlockIdx.compareTo(latestBlockIdx) > -1) {
            return onCaughtUpObservable;
        } else {
            return Observable.concat(
                    replayBlocksObservableSync(startBlockIdx, latestBlockIdx, fullTransactionObjects, true),
                    Observable.defer(() -> catchUpToLatestBlockObservableSync(
                            latestBlockIdx.add(BigInteger.ONE),
                            fullTransactionObjects,
                            onCaughtUpObservable)));
        }
    }

    public Observable<Transaction> catchUpToLatestTransactionObservable(BigInteger startBlock) {
        return catchUpToLatestBlockObservable(startBlock, true, Observable.empty())
                .flatMapIterable(b -> b.getBlock().getTransactions());
    }

    /**
     * Creates an observable that emits blocks starting at {@code startBlockNumber} up to the most recent block and
     * continues emitting blocks that are newly created on the Neo blockchain. The new blocks are pulled every
     * {@code pollingInterval}.
     * <p>
     * Do not use {@code retry(...)} on this observable because it will not only resubscribe to the new blocks but
     * also replay the blocks since {@code startBlock}.
     *
     * @param startBlock             The block index at which to start catching up.
     * @param fullTransactionObjects If the full transactions objects should be included in the blocks.
     * @param pollingInterval        The polling interval in milliseconds.
     * @return the block observable.
     */
    public Observable<NeoGetBlock> catchUpToLatestAndSubscribeToNewBlocksObservable(BigInteger startBlock,
            boolean fullTransactionObjects, long pollingInterval) {

        return catchUpToLatestBlockObservable(startBlock, fullTransactionObjects,
                blockObservable(fullTransactionObjects, pollingInterval));
    }

    private BigInteger getLatestBlockIdx() throws IOException {
        return neow3j.getBlockCount().send().getBlockCount().subtract(BigInteger.ONE);
    }

}
