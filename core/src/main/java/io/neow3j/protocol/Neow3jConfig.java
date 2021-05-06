package io.neow3j.protocol;

import io.neow3j.utils.Async;

import java.util.concurrent.ScheduledExecutorService;

public class Neow3jConfig {

    public static final int DEFAULT_BLOCK_TIME = 15 * 1000;
    public static final byte DEFAULT_ADDRESS_VERSION = 0x35;
    private static final int MAX_VALID_UNTIL_BLOCK_INCREMENT_BASE = 86400000;

    private static byte addressVersion = DEFAULT_ADDRESS_VERSION;
    private Integer networkMagic = null;
    private int blockInterval = DEFAULT_BLOCK_TIME;
    private int pollingInterval = DEFAULT_BLOCK_TIME;
    private ScheduledExecutorService scheduledExecutorService = Async.defaultExecutorService();

    public Neow3jConfig() {
    }

    public Neow3jConfig(int networkMagic, int blockInterval, int pollingInterval,
            ScheduledExecutorService scheduledExecutorService) {

        this.networkMagic = networkMagic;
        this.blockInterval = blockInterval;
        this.pollingInterval = pollingInterval;
        this.scheduledExecutorService = scheduledExecutorService;
    }

    public int getPollingInterval() {
        return pollingInterval;
    }

    public Neow3jConfig setPollingInterval(int pollingInterval) {
        this.pollingInterval = pollingInterval;
        return this;
    }

    public ScheduledExecutorService getScheduledExecutorService() {
        return scheduledExecutorService;
    }

    public Neow3jConfig setScheduledExecutorService(ScheduledExecutorService executorService) {
        scheduledExecutorService = executorService;
        return this;
    }

    public static byte getAddressVersion() {
        return addressVersion;
    }

    public static void setAddressVersion(byte version) {
        addressVersion = version;
    }

    public Integer getNetworkMagic() {
        return networkMagic;
    }

    public Neow3jConfig setNetworkMagic(int magic) {
        networkMagic = magic;
        return this;
    }

    public int getBlockInterval() {
        return blockInterval;
    }

    public Neow3jConfig setBlockInterval(int blockInterval) {
        this.blockInterval = blockInterval;
        return this;
    }

    public long getMaxValidUntilBlockIncrement() {
        return MAX_VALID_UNTIL_BLOCK_INCREMENT_BASE / getBlockInterval();
    }
}
