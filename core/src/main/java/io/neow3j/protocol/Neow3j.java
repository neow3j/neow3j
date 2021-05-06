package io.neow3j.protocol;

import io.neow3j.protocol.core.JsonRpc2_0Neow3j;
import io.neow3j.protocol.core.Neo;
import io.neow3j.protocol.rx.Neow3jRx;
import io.neow3j.utils.Async;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ScheduledExecutorService;

/**
 * JSON-RPC Request object building factory.
 */
public abstract class Neow3j implements Neo, Neow3jRx {

    public static final int DEFAULT_BLOCK_TIME = 15 * 1000;
    public static final byte DEFAULT_ADDRESS_VERSION = 0x35;
    private static final int MAX_VALID_UNTIL_BLOCK_INCREMENT_BASE = 86400000;

    private final Config config;
    private static byte addressVersion = DEFAULT_ADDRESS_VERSION;

    public Neow3j(Config config) {
        this.config = config;
    }

    /**
     * Construct a new Neow3j instance.
     *
     * @param neow3jService neow3j service instance - i.e. HTTP or IPC
     * @return new Neow3j instance
     */
    public static Neow3j build(Neow3jService neow3jService) {
        return new JsonRpc2_0Neow3j(neow3jService, new Config());
    }

    /**
     * Construct a new Neow3j instance.
     *
     * @param neow3jService neow3j service instance - i.e. HTTP or IPC
     * @return new Neow3j instance.
     */
    public static Neow3j build(Neow3jService neow3jService, Config config) {
        return new JsonRpc2_0Neow3j(neow3jService, config);
    }

    /**
     * Shutdowns a Neow3j instance and closes opened resources.
     */
    public abstract void shutdown();

    /**
     * Gets the configured network magic number.
     *
     * @return The network's magic number.
     */
    public byte[] getNetworkMagicNumber() throws IOException {
        if (config.getNetworkMagic() == null) {
            config.setNetworkMagic(getVersion().send().getVersion().getNetwork());
        }
        byte[] array = new byte[4];
        ByteBuffer.wrap(array).order(ByteOrder.LITTLE_ENDIAN).putInt(config.getNetworkMagic());
        return array;
    }

    public ScheduledExecutorService getScheduledExecutorService() {
        return config.getScheduledExecutorService();
    }

    public int getBlockInterval() {
        return config.getBlockInterval();
    }

    public int getPollingInterval() {
        return config.getPollingInterval();
    }

    public long getMaxValidUntilBlockIncrement() {
        return config.getMaxValidUntilBlockIncrement();
    }

    /**
     * Gets the configured address version number to use for address creation and verification.
     * <p>
     * The default address version is 53.
     *
     * @return The address version.
     */
    public static byte getAddressVersion() {
        return addressVersion;
    }

    /**
     * Sets the address version to use for address creation and verification.
     *
     * @param version The address version.
     */
    public static void setAddressVersion(byte version) {
        addressVersion = version;
    }

    public static class Config {

        private byte addressVersion = Neow3j.addressVersion;
        private Integer networkMagic = null;
        private int blockInterval = DEFAULT_BLOCK_TIME;
        private int pollingInterval = DEFAULT_BLOCK_TIME;
        private ScheduledExecutorService scheduledExecutorService =
                Async.defaultExecutorService();

        public Config() {
        }

        public Config(byte addressVersion, int networkMagic, int blockInterval, int pollingInterval,
                ScheduledExecutorService scheduledExecutorService) {

            this.addressVersion = addressVersion;
            this.networkMagic = networkMagic;
            this.blockInterval = blockInterval;
            this.pollingInterval = pollingInterval;
            this.scheduledExecutorService = scheduledExecutorService;
        }

        private int getPollingInterval() {
            return pollingInterval;
        }

        public Config setPollingInterval(int pollingInterval) {
            this.pollingInterval = pollingInterval;
            return this;
        }

        private ScheduledExecutorService getScheduledExecutorService() {
            return scheduledExecutorService;
        }

        public Config setScheduledExecutorService(ScheduledExecutorService executorService) {
            scheduledExecutorService = executorService;
            return this;
        }

        private byte getAddressVersion() {
            return addressVersion;
        }

        private Integer getNetworkMagic() {
            return networkMagic;
        }

        public Config setNetworkMagic(int magic) {
            networkMagic = magic;
            return this;
        }

        private int getBlockInterval() {
            return blockInterval;
        }

        public Config setBlockInterval(int blockInterval) {
            this.blockInterval = blockInterval;
            return this;
        }

        public long getMaxValidUntilBlockIncrement() {
            return MAX_VALID_UNTIL_BLOCK_INCREMENT_BASE / getBlockInterval();
        }
    }

}
