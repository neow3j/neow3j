package com.axlabs.neow3j.protocol.core.polling;

import com.axlabs.neow3j.protocol.Neow3j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class BlockPolling {

    private static final Logger LOG = LoggerFactory.getLogger(BlockPolling.class);

    private BigInteger currentBlock;

    private final Neow3j neow3j;
    private final Callback<BigInteger> callback;

    private ScheduledFuture<?> schedule;

    public BlockPolling(Neow3j neow3j, Callback<BigInteger> callback) {
        this.neow3j = neow3j;
        this.callback = callback;
    }

    public BigInteger getCurrentBlock() {
        return currentBlock;
    }

    public synchronized void nextBlock() {
        this.currentBlock = this.currentBlock.add(BigInteger.ONE);
    }

    private BigInteger getLatestBlockIndex() throws IOException {
        return neow3j.getBlockCount().send().getBlockIndex();
    }

    public void run(ScheduledExecutorService scheduledExecutorService, long pollingInterval) {
        this.schedule = scheduledExecutorService.scheduleAtFixedRate(
                () -> {
                    try {
                        BigInteger latestBlockNumber = getLatestBlockIndex().subtract(BigInteger.ONE);
                        if (this.currentBlock == null) {
                            this.currentBlock = latestBlockNumber;
                        }
                        if (latestBlockNumber.compareTo(getCurrentBlock()) == 1) {
                            IntStream.rangeClosed(getCurrentBlock().add(BigInteger.ONE).intValue(), latestBlockNumber.intValue())
                                    .forEachOrdered((blockIndex) -> {
                                        callback.onEvent(BigInteger.valueOf(blockIndex));
                                        nextBlock();
                                    });
                        }
                    } catch (Throwable e) {
                        LOG.error("Error on polling: {}", e);
                        Observable.error(e);
                    }
                },
                0, pollingInterval, TimeUnit.MILLISECONDS);
    }

    public void cancel() {
        schedule.cancel(false);
    }

}
