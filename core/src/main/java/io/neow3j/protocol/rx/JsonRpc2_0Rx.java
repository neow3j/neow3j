package io.neow3j.protocol.rx;

import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.methods.response.NeoGetBlock;
import io.neow3j.protocol.core.methods.response.Transaction;
import io.neow3j.protocol.core.polling.BlockPolling;
import io.neow3j.utils.Observables;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposables;
import io.reactivex.schedulers.Schedulers;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

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

    public Observable<BigInteger> neoBlockObservable(long pollingInterval) {
        return Observable.create(subscriber -> {
            BlockPolling blockPolling = new BlockPolling(neow3j, subscriber::onNext);
            blockPolling.run(scheduledExecutorService, pollingInterval);
            subscriber.setDisposable(Disposables.fromAction(blockPolling::cancel));
        });
    }

    public Observable<NeoGetBlock> replayBlocksObservable(
            BigInteger startBlock, BigInteger endBlock,
            boolean fullTransactionObjects) {
        return replayBlocksObservable(startBlock, endBlock, fullTransactionObjects, true);
    }

    public Observable<NeoGetBlock> replayBlocksObservable(
            BigInteger startBlock, BigInteger endBlock,
            boolean fullTransactionObjects, boolean ascending) {
        // We use a scheduler to ensure this Observable runs asynchronously for users to be
        // consistent with the other Observables
        return replayBlocksObservableSync(startBlock, endBlock, fullTransactionObjects, ascending)
                .subscribeOn(scheduler);
    }

    private Observable<NeoGetBlock> replayBlocksObservableSync(
            BigInteger startBlock, BigInteger endBlock,
            boolean fullTransactionObjects) {
        return replayBlocksObservableSync(startBlock, endBlock, fullTransactionObjects, true);
    }

    private Observable<NeoGetBlock> replayBlocksObservableSync(
            BigInteger startBlockNumber, BigInteger endBlockNumber,
            boolean fullTransactionObjects, boolean ascending) {

        if (ascending) {
            return Observables.range(startBlockNumber, endBlockNumber)
                    .flatMap(i -> neow3j.getBlock(i, fullTransactionObjects).observable());
        } else {
            return Observables.range(startBlockNumber, endBlockNumber, false)
                    .flatMap(i -> neow3j.getBlock(i, fullTransactionObjects).observable());
        }
    }

    public Observable<NeoGetBlock> catchUpToLatestBlockObservable(
            BigInteger startBlock, boolean fullTransactionObjects,
            Observable<NeoGetBlock> onCompleteObservable) {
        // We use a scheduler to ensure this Observable runs asynchronously for users to be
        // consistent with the other Observables
        return catchUpToLatestBlockObservableSync(
                startBlock, fullTransactionObjects, onCompleteObservable)
                .subscribeOn(scheduler);
    }

    public Observable<NeoGetBlock> catchUpToLatestBlockObservable(
            BigInteger startBlock, boolean fullTransactionObjects) {
        return catchUpToLatestBlockObservable(
                startBlock, fullTransactionObjects, Observable.empty());
    }

    private Observable<NeoGetBlock> catchUpToLatestBlockObservableSync(
            BigInteger startBlockNumber, boolean fullTransactionObjects,
            Observable<NeoGetBlock> onCompleteObservable) {

        BigInteger latestBlockNumber;
        try {
            latestBlockNumber = getLatestBlockNumber();
        } catch (IOException e) {
            return Observable.error(e);
        }

        if (startBlockNumber.compareTo(latestBlockNumber) > -1) {
            return onCompleteObservable;
        } else {
            return Observable.concat(
                    replayBlocksObservableSync(
                            startBlockNumber,
                            latestBlockNumber,
                            fullTransactionObjects),
                    Observable.defer(() -> catchUpToLatestBlockObservableSync(
                            latestBlockNumber.add(BigInteger.ONE),
                            fullTransactionObjects,
                            onCompleteObservable)));
        }
    }

    public Observable<Transaction> catchUpToLatestTransactionObservable(BigInteger startBlock) {
        return catchUpToLatestBlockObservable(
                startBlock, true, Observable.empty())
                .flatMapIterable(JsonRpc2_0Rx::toTransactions);
    }

    public Observable<NeoGetBlock> catchUpToLatestAndSubscribeToNewBlocksObservable(
            BigInteger startBlock, boolean fullTransactionObjects, long pollingInterval) {

        return catchUpToLatestBlockObservable(
                startBlock, fullTransactionObjects,
                blockObservable(fullTransactionObjects, pollingInterval));
    }

    public Observable<NeoGetBlock> blockObservable(boolean fullTransactionObjects,
            long pollingInterval) {

        return neoBlockObservable(pollingInterval)
                .flatMap(blockIndex ->
                        neow3j.getBlock(blockIndex, fullTransactionObjects).observable());
    }

    public Observable<NeoGetBlock> subscribeToNewBlocksObservable(boolean fullTransactionObjects,
            long pollingInterval) throws IOException {

        return catchUpToLatestAndSubscribeToNewBlocksObservable(
                getLatestBlockNumber(), fullTransactionObjects, pollingInterval);

    }

    private static List<Transaction> toTransactions(NeoGetBlock neoGetBlock) {
        return new ArrayList<>(neoGetBlock.getBlock().getTransactions());
    }

    private BigInteger getLatestBlockNumber() throws IOException {
        return neow3j.getBlockCount().send().getBlockIndex().subtract(BigInteger.ONE);
    }

}
