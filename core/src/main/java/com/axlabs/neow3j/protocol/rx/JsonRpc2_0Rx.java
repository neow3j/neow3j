package com.axlabs.neow3j.protocol.rx;

import com.axlabs.neow3j.protocol.Neow3j;
import com.axlabs.neow3j.protocol.core.BlockParameter;
import com.axlabs.neow3j.protocol.core.BlockParameterIndex;
import com.axlabs.neow3j.protocol.core.BlockParameterName;
import com.axlabs.neow3j.protocol.core.polling.BlockPolling;
import com.axlabs.neow3j.protocol.core.methods.response.NeoBlockCount;
import com.axlabs.neow3j.protocol.core.methods.response.NeoGetBlock;
import com.axlabs.neow3j.protocol.core.methods.response.Transaction;
import com.axlabs.neow3j.utils.Observables;
import rx.Observable;
import rx.Scheduler;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;

import java.io.IOException;
import java.math.BigInteger;
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
            subscriber.add(Subscriptions.create(blockPolling::cancel));
        });
    }

    public Observable<NeoGetBlock> replayBlocksObservable(
            BlockParameter startBlock, BlockParameter endBlock,
            boolean fullTransactionObjects) {
        return replayBlocksObservable(startBlock, endBlock, fullTransactionObjects, true);
    }

    public Observable<NeoGetBlock> replayBlocksObservable(
            BlockParameter startBlock, BlockParameter endBlock,
            boolean fullTransactionObjects, boolean ascending) {
        // We use a scheduler to ensure this Observable runs asynchronously for users to be
        // consistent with the other Observables
        return replayBlocksObservableSync(startBlock, endBlock, fullTransactionObjects, ascending)
                .subscribeOn(scheduler);
    }

    private Observable<NeoGetBlock> replayBlocksObservableSync(
            BlockParameter startBlock, BlockParameter endBlock,
            boolean fullTransactionObjects) {
        return replayBlocksObservableSync(startBlock, endBlock, fullTransactionObjects, true);
    }

    private Observable<NeoGetBlock> replayBlocksObservableSync(
            BlockParameter startBlock, BlockParameter endBlock,
            boolean fullTransactionObjects, boolean ascending) {

        BigInteger startBlockNumber = null;
        BigInteger endBlockNumber = null;
        try {
            startBlockNumber = getBlockNumber(startBlock);
            endBlockNumber = getBlockNumber(endBlock);
        } catch (IOException e) {
            Observable.error(e);
        }

        if (ascending) {
            return Observables.range(startBlockNumber, endBlockNumber)
                    .flatMap(i -> neow3j.getBlock(new BlockParameterIndex(i), fullTransactionObjects).observable());
        } else {
            return Observables.range(startBlockNumber, endBlockNumber, false)
                    .flatMap(i -> neow3j.getBlock(new BlockParameterIndex(i), fullTransactionObjects).observable());
        }
    }

    public Observable<NeoGetBlock> catchUpToLatestBlockObservable(
            BlockParameter startBlock, boolean fullTransactionObjects,
            Observable<NeoGetBlock> onCompleteObservable) {
        // We use a scheduler to ensure this Observable runs asynchronously for users to be
        // consistent with the other Observables
        return catchUpToLatestBlockObservableSync(
                startBlock, fullTransactionObjects, onCompleteObservable)
                .subscribeOn(scheduler);
    }

    public Observable<NeoGetBlock> catchUpToLatestBlockObservable(
            BlockParameter startBlock, boolean fullTransactionObjects) {
        return catchUpToLatestBlockObservable(
                startBlock, fullTransactionObjects, Observable.empty());
    }

    private Observable<NeoGetBlock> catchUpToLatestBlockObservableSync(
            BlockParameter startBlock, boolean fullTransactionObjects,
            Observable<NeoGetBlock> onCompleteObservable) {

        BigInteger startBlockNumber;
        BigInteger latestBlockNumber;
        try {
            startBlockNumber = getBlockNumber(startBlock);
            latestBlockNumber = getLatestBlockNumber();
        } catch (IOException e) {
            return Observable.error(e);
        }

        if (startBlockNumber.compareTo(latestBlockNumber) > -1) {
            return onCompleteObservable;
        } else {
            return Observable.concat(
                    replayBlocksObservableSync(
                            new BlockParameterIndex(startBlockNumber),
                            new BlockParameterIndex(latestBlockNumber),
                            fullTransactionObjects),
                    Observable.defer(() -> catchUpToLatestBlockObservableSync(
                            new BlockParameterIndex(latestBlockNumber.add(BigInteger.ONE)),
                            fullTransactionObjects,
                            onCompleteObservable)));
        }
    }

    public Observable<Transaction> catchUpToLatestTransactionObservable(
            BlockParameter startBlock) {
        return catchUpToLatestBlockObservable(
                startBlock, true, Observable.empty())
                .flatMapIterable(JsonRpc2_0Rx::toTransactions);
    }

    public Observable<NeoGetBlock> catchUpToLatestAndSubscribeToNewBlocksObservable(
            BlockParameter startBlock, boolean fullTransactionObjects,
            long pollingInterval) {

        return catchUpToLatestBlockObservable(
                startBlock, fullTransactionObjects,
                blockObservable(fullTransactionObjects, pollingInterval));
    }

    public Observable<NeoGetBlock> blockObservable(boolean fullTransactionObjects, long pollingInterval) {
        return neoBlockObservable(pollingInterval)
                .flatMap(blockIndex ->
                        neow3j.getBlock(new BlockParameterIndex(blockIndex), fullTransactionObjects).observable());
    }

    private static List<Transaction> toTransactions(NeoGetBlock neoGetBlock) {
        return neoGetBlock.getBlock().getTransactions().stream().collect(Collectors.toList());
    }

    private BigInteger getLatestBlockNumber() throws IOException {
        return getBlockNumber(BlockParameterName.LATEST).subtract(BigInteger.ONE);
    }

    private BigInteger getBlockNumber(
            BlockParameter defaultBlockParameter) throws IOException {
        if (defaultBlockParameter instanceof BlockParameterIndex) {
            return ((BlockParameterIndex) defaultBlockParameter).getBlockIndex();
        } else {
            if (defaultBlockParameter instanceof BlockParameterName) {
                if (defaultBlockParameter.getValue() == BlockParameterName.EARLIEST.getValue()) {
                    return BigInteger.ZERO;
                }
            }
            NeoBlockCount latestNeoBlock = neow3j.getBlockCount().send();
            return latestNeoBlock.getBlockIndex();
        }
    }

}
