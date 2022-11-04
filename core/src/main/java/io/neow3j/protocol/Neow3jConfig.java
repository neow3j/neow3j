package io.neow3j.protocol;

import io.neow3j.types.Hash160;
import io.neow3j.utils.Async;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Contains variables that configure a {@link Neow3j} instance. In general this configuration needs to match the
 * configuration of the Neo node you connect to.
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
    private boolean allowTransmissionOnFault = false;

    private static final Hash160 MAINNET_NNS_CONTRACT_HASH = new Hash160("0x50ac1c37690cc2cfc594472833cf57505d5f46de");
    private Hash160 nnsResolver = MAINNET_NNS_CONTRACT_HASH;

    /**
     * Constructs a configuration instance with default values.
     */
    public Neow3jConfig() {
    }

    public Neow3jConfig(long networkMagic, int blockInterval, int pollingInterval, long maxValidUntilBlockIncrement,
            ScheduledExecutorService scheduledExecutorService) {
        this(networkMagic, blockInterval, pollingInterval, maxValidUntilBlockIncrement, scheduledExecutorService,
                MAINNET_NNS_CONTRACT_HASH);
    }

    public Neow3jConfig(long networkMagic, int blockInterval, int pollingInterval, long maxValidUntilBlockIncrement,
            ScheduledExecutorService scheduledExecutorService, Hash160 neoNameServiceScriptHash) {
        this.networkMagic = networkMagic;
        this.blockInterval = blockInterval;
        this.maxValidUntilBlockIncrement = maxValidUntilBlockIncrement;
        this.pollingInterval = pollingInterval;
        this.scheduledExecutorService = scheduledExecutorService;
        this.nnsResolver = neoNameServiceScriptHash;
    }

    /**
     * Gets the interval in milliseconds in which {@code Neow3j} polls the neo-node for new block information when
     * observing the blockchain.
     *
     * @return the polling interval in milliseconds.
     * @see Neow3j#getPollingInterval()
     */
    public int getPollingInterval() {
        return pollingInterval;
    }

    /**
     * Set the interval in milliseconds in which {@code Neow3j} should poll the neo-node for new block information
     * when observing the blockchain.
     *
     * @param pollingInterval The polling interval in milliseconds.
     * @return this.
     */
    public Neow3jConfig setPollingInterval(int pollingInterval) {
        this.pollingInterval = pollingInterval;
        return this;
    }

    /**
     * @return the executor service used for polling new blocks from the Neo node.
     * @see Neow3j#getScheduledExecutorService()
     */
    public ScheduledExecutorService getScheduledExecutorService() {
        return scheduledExecutorService;
    }

    /**
     * Sets the executor service used for polling new blocks from the neo-node.
     *
     * @param executorService the desired executor service.
     * @return this.
     */
    public Neow3jConfig setScheduledExecutorService(ScheduledExecutorService executorService) {
        scheduledExecutorService = executorService;
        return this;
    }

    /**
     * Gets the configured address version.
     * <p>
     * The address version is used in the creation of Neo addresses from script hashes. It defaults to
     * {@link Neow3jConfig#DEFAULT_ADDRESS_VERSION}.
     * <p>
     * This method is static because it is necessary in code that can be used independent of a connected Neo node.
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
     * The default value is null. Only once {@link Neow3j#getNetworkMagicNumberBytes()} or
     * {@link Neow3j#getNetworkMagicNumber()} is called for the first time the value is set. This is because the
     * magic number is fetched directly from the neo-node.
     * <p>
     * The magic number is represented as an unsigned 32-bit integer on the neo-node. Thus, it's maximum possible
     * value is 0xffffffff or 2<sup>32</sup>-1.
     *
     * @return the network's magic number.
     * @see Neow3j#getNetworkMagicNumber()
     */
    public Long getNetworkMagic() {
        return networkMagic;
    }

    /**
     * Sets the network magic number.
     * <p>
     * The magic number is an ingredient, e.g., when generating the hash of a transaction. This should match the
     * configuration of the neo-node you connect to.
     *
     * @param magic the network's magic number.
     * @return this.
     */
    public Neow3jConfig setNetworkMagic(long magic) {
        if (magic > 0xFFFFFFFFL || magic < 0L) {
            throw new IllegalArgumentException("The network magic number must fit into a 32-bit unsigned integer, " +
                    "i.e., it must be positive and not greater than 0xFFFFFFFF.");
        }
        networkMagic = magic;
        return this;
    }

    /**
     * @return the block interval in milliseconds.
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
     * @param blockInterval the block interval in milliseconds.
     * @return this.
     */
    public Neow3jConfig setBlockInterval(int blockInterval) {
        this.blockInterval = blockInterval;
        return this;
    }

    /**
     * Gets the maximum time in milliseconds that can pass from the construction of a transaction until it gets
     * included in a block. A transaction becomes invalid after this time increment is surpassed.
     *
     * @return the maximum valid until block time increment.
     * @see Neow3j#getMaxValidUntilBlockIncrement()
     */
    public long getMaxValidUntilBlockIncrement() {
        return maxValidUntilBlockIncrement;
    }

    /**
     * Sets the maximum time in milliseconds that can pass from the construction of a transaction until it gets
     * included in a block. A transaction becomes invalid after this time increment is surpassed.
     * <p>
     * This should match the configuration of the neo-node you connect to.
     *
     * @param maxValidUntilBlockIncrement the maximum valid until block time increment.
     * @return this.
     */
    public Neow3jConfig setMaxValidUntilBlockIncrement(long maxValidUntilBlockIncrement) {
        this.maxValidUntilBlockIncrement = maxValidUntilBlockIncrement;
        return this;
    }

    /**
     * @return the NeoNameService resolver script hash.
     */
    public Hash160 getNNSResolver() {
        return this.nnsResolver;
    }

    /**
     * Sets the NeoNameService resolver script hash.
     *
     * @param resolver the NeoNameService script hash.
     * @return this.
     */
    public Neow3jConfig setNNSResolver(Hash160 resolver) {
        this.nnsResolver = resolver;
        return this;
    }

    /**
     * @return true if transmission is allowed when the provided script leads to a
     * {@link io.neow3j.types.NeoVMStateType#FAULT}. False, otherwise.
     */
    public boolean transmissionOnFaultIsAllowed() {
        return this.allowTransmissionOnFault;
    }

    /**
     * Allow the transmission of scripts that lead to a {@link io.neow3j.types.NeoVMStateType#FAULT}.
     *
     * @return this.
     */
    public Neow3jConfig allowTransmissionOnFault() {
        this.allowTransmissionOnFault = true;
        return this;
    }

    /**
     * Prevent the transmission of scripts that lead to a {@link io.neow3j.types.NeoVMStateType#FAULT}.
     * <p>
     * This is set by default.
     *
     * @return this.
     */
    public Neow3jConfig preventTransmissionOnFault() {
        this.allowTransmissionOnFault = false;
        return this;
    }

}
