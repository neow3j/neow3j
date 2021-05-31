package io.neow3j.protocol;

import io.neow3j.utils.Async;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Contains variables that configure a {@link Neow3j} instance. In general this configuration
 * needs to match the configuration of the neo-node you connect to.
 */
public class Neow3jConfig {

    public static final int DEFAULT_BLOCK_TIME = 15 * 1000;
    public static final byte DEFAULT_ADDRESS_VERSION = 0x35;
    public static final int MAX_VALID_UNTIL_BLOCK_INCREMENT_BASE = 86400000;

    private static byte addressVersion = DEFAULT_ADDRESS_VERSION;
    private Long networkMagic = null;
    private int blockInterval = DEFAULT_BLOCK_TIME;
    private long maxValidUntilBlockIncrement = MAX_VALID_UNTIL_BLOCK_INCREMENT_BASE / blockInterval;
    private int pollingInterval = DEFAULT_BLOCK_TIME;
    private ScheduledExecutorService scheduledExecutorService = Async.defaultExecutorService();

    /**
     * Constructs a configuration instance with default values.
     */
    public Neow3jConfig() {
    }

    public Neow3jConfig(long networkMagic, int blockInterval, int pollingInterval,
            long maxValidUntilBlockIncrement, ScheduledExecutorService scheduledExecutorService) {

        this.networkMagic = networkMagic;
        this.blockInterval = blockInterval;
        this.maxValidUntilBlockIncrement = maxValidUntilBlockIncrement;
        this.pollingInterval = pollingInterval;
        this.scheduledExecutorService = scheduledExecutorService;

    }

    /**
     * Gets the interval in milliseconds in which {@code Neow3j} polls the neo-node for new block
     * information when observing the blockchain.
     *
     * @return The polling interval in milliseconds.
     * @see Neow3j#getPollingInterval()
     */
    public int getPollingInterval() {
        return pollingInterval;
    }

    /**
     * Set the interval in milliseconds in which {@code Neow3j} should poll the neo-node for new
     * block information when observing the blockchain.
     *
     * @param pollingInterval The polling interval in milliseconds.
     * @return this.
     */
    public Neow3jConfig setPollingInterval(int pollingInterval) {
        this.pollingInterval = pollingInterval;
        return this;
    }

    /**
     * Gets the executor service used for polling new blocks from the neo-node.
     *
     * @return The executor service.
     * @see Neow3j#getScheduledExecutorService()
     */
    public ScheduledExecutorService getScheduledExecutorService() {
        return scheduledExecutorService;
    }

    /**
     * Sets the executor service used for polling new blocks from the neo-node.
     *
     * @param executorService The desired executor service.
     * @return this.
     */
    public Neow3jConfig setScheduledExecutorService(ScheduledExecutorService executorService) {
        scheduledExecutorService = executorService;
        return this;
    }

    /**
     * Gets the configured address version.
     * <p>
     * The address version is used in the creation of Neo addresses from script hashes.
     * It defaults to {@link Neow3jConfig#DEFAULT_ADDRESS_VERSION}.
     * <p>
     * This method is static because it is necessary in code that can be used independent of a
     * connected neo-node.
     *
     * @return the address version.
     */
    public static byte getAddressVersion() {
        return addressVersion;
    }

    /**
     * Sets the address version.
     * <p>
     * This should match the configuration of the neo-node you connect to.
     *
     * @param version the desired address version.
     */
    public static void setAddressVersion(byte version) {
        addressVersion = version;
    }

    /**
     * Gets the configured network magic number.
     * <p>
     * The magic number is an ingredient, e.g., when generating the hash of a transaction.
     * <p>
     * The default value is null. Only once {@link Neow3j#getNetworkMagicNumber()} is called for
     * the first time the value is set. This is because the magic number is fetched directly from
     * the neo-node.
     * <p>
     * The magic number is represented as an unsigned 32-bit integer on the neo-node. Thus, it's
     * maximum possible value is 0xffffffff or 2<sup>32</sup>-1.
     *
     * @return The network's magic number.
     * @see Neow3j#getNetworkMagicNumber()
     */
    public Long getNetworkMagic() {
        return networkMagic;
    }

    /**
     * Sets the network magic number.
     * <p>
     * The magic number is an ingredient, e.g., when generating the hash of a transaction.
     * This should match the configuration of the neo-node you connect to.
     *
     * @param magic The network magic number.
     * @return this.
     */
    public Neow3jConfig setNetworkMagic(long magic) {
        if (magic > 0xFFFFFFFFL || magic < 0L)  {
            throw new IllegalArgumentException("The network magic number must fit into a 32-bit " +
                    "unsigned integer, i.e., it must be positive and not greater than 0xFFFFFFFF.");
        }
        networkMagic = magic;
        return this;
    }

    /**
     * Gets the block interval in milliseconds.
     *
     * @return The block interval in milliseconds.
     * @see Neow3j#getScheduledExecutorService()
     */
    public int getBlockInterval() {
        return blockInterval;
    }

    /**
     * Sets the interval in milliseconds in which blocks are produced.
     * <p>
     * This should match the block time of the blockchain network you connect to.
     *
     * @param blockInterval The block interval in milliseconds.
     * @return this.
     */
    public Neow3jConfig setBlockInterval(int blockInterval) {
        this.blockInterval = blockInterval;
        return this;
    }

    /**
     * Gets the maximum time in milliseconds that can pass from the construction of a transaction
     * until it gets included in a block. A transaction becomes invalid after this time increment
     * is surpassed.
     *
     * @return The maximum valid until block time increment.
     * @see Neow3j#getMaxValidUntilBlockIncrement()
     */
    public long getMaxValidUntilBlockIncrement() {
        return maxValidUntilBlockIncrement;
    }

    /**
     * Sets the maximum time in milliseconds that can pass from the construction of a transaction
     * until it gets included in a block. A transaction becomes invalid after this time increment
     * is surpassed.
     * <p>
     * This should match the configuration of the neo-node you connect to.
     *
     * @param maxValidUntilBlockIncrement The maximum valid until block time increment.
     * @return this
     */
    public Neow3jConfig setMaxValidUntilBlockIncrement(long maxValidUntilBlockIncrement) {
        this.maxValidUntilBlockIncrement = maxValidUntilBlockIncrement;
        return this;
    }
}
