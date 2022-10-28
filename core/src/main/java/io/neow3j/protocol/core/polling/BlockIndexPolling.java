package io.neow3j.protocol.core.polling;

import io.neow3j.protocol.Neow3j;
import io.reactivex.ObservableEmitter;
import io.reactivex.disposables.Disposables;

import java.math.BigInteger;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;

public class BlockIndexPolling {

    private BigInteger currentBlockIdx;

    public void run(Neow3j neow3j, ObservableEmitter<BigInteger> emitter,
            ScheduledExecutorService scheduledExecutorService, long pollingInterval) {

        // If a task takes longer than the specified period the next task starts late and no concurrent with
        // the previous one. Thus, we don't have to synchronize anything.
        ScheduledFuture<?> schedule = scheduledExecutorService.scheduleAtFixedRate(
                () -> {
                    try {
                        BigInteger latestBlockIdx = neow3j.getBlockCount().send().getBlockCount()
                                .subtract(BigInteger.ONE);
                        if (this.currentBlockIdx == null) {
                            this.currentBlockIdx = latestBlockIdx;
                        }
                        if (latestBlockIdx.compareTo(currentBlockIdx) > 0) {
                            LongStream.rangeClosed(
                                    currentBlockIdx.add(BigInteger.ONE).intValue(),
                                    latestBlockIdx.intValue()
                            ).forEachOrdered((blockIndex) -> {
                                emitter.onNext(BigInteger.valueOf(blockIndex));
                                this.currentBlockIdx = this.currentBlockIdx.add(BigInteger.ONE);
                            });
                        }
                    } catch (Throwable e) {
                        emitter.onError(e);
                    }
                },
                0, pollingInterval, TimeUnit.MILLISECONDS);

        emitter.setDisposable(Disposables.fromAction(() -> schedule.cancel(false)));
    }

}
