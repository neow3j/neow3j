package io.neow3j.protocol;

import io.neow3j.protocol.core.response.NeoGetVersion;
import io.neow3j.types.Hash160;
import io.neow3j.utils.Async;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Contains variables that configure a {@link Neow3j} instance.
 */
public class Neow3jConfig {

    private static final long MAINNET_NETWORK_MAGIC = 860833102;
    private static final Hash160 MAINNET_NNS_CONTRACT_HASH = new Hash160("0x50ac1c37690cc2cfc594472833cf57505d5f46de");

    public static final byte DEFAULT_ADDRESS_VERSION = 0x35;

    // Static configuration
    private static byte staticAddressVersion = DEFAULT_ADDRESS_VERSION;

    // Node-specific configuration values that remain constant during the lifetime of a running node and are used
    // for building and signing transactions.
    private Long nodeNetworkMagic = null;
    private Long nodeMaxValidUntilBlockIncrement = null;

    // Neow3j configuration
    private Long pollingInterval = null; // There's no default polling interval needed. This is only needed if
    // there is a connection to a node. Then, this value is set to the node's milliseconds per block.
    private boolean allowTransmissionOnFault = false;

    // The NeoNameService resolver script hash. If another network than mainnet is used, this must be set manually if
    // neow3j functions that use the NeoNameService are used.
    private Hash160 nnsResolver = null;

    private ScheduledExecutorService scheduledExecutorService = Async.defaultExecutorService();

    /**
     * Constructs a configuration instance with default values.
     */
    private Neow3jConfig() {
    }

    /**
     * @return a Neow3jConfig object with default configuration.
     */
    public static Neow3jConfig defaultNeow3jConfig() {
        return new Neow3jConfig();
    }

    void setNodeConfiguration(NeoGetVersion.NeoVersion.Protocol protocol) {
        this.nodeNetworkMagic = protocol.getNetwork();
        this.nodeMaxValidUntilBlockIncrement = protocol.getMaxValidUntilBlockIncrement();
        // The polling interval is set to the node's milliseconds per block if it has not been set manually.
        if (this.pollingInterval == null) {
            this.pollingInterval = protocol.getMilliSecondsPerBlock();
        }

        if (this.nnsResolver == null && isMainnet()) {
            this.nnsResolver = MAINNET_NNS_CONTRACT_HASH;
        }
    }

    private boolean isMainnet() {
        return this.nodeNetworkMagic == MAINNET_NETWORK_MAGIC;
    }

    // region static configuration values

    /**
     * Gets the static address version.
     * <p>
     * The address version is used in the creation of Neo addresses from script hashes. If not set manually,
     * this value is equal to {@link Neow3jConfig#DEFAULT_ADDRESS_VERSION} by default.
     * <p>
     * This method is static because it is necessary in code that can be used independent of a connected Neo node.
     *
     * @return the address version.
     */
    public static byte getStaticAddressVersion() {
        return staticAddressVersion;
    }

    /**
     * Sets the static address version.
     * <p>
     * The static address version is only used if no neow3j instance is available to provide the address version of
     * the connected node. For example, this is the case when using the {@link io.neow3j.utils.AddressUtils} class or
     * some of the static functions of {@link io.neow3j.wallet.Account}.
     *
     * @param version the desired static address version.
     */
    public static void setStaticAddressVersion(byte version) {
        staticAddressVersion = version;
    }

    // endregion
    // region node-specific configuration values

    /**
     * Gets the network magic number of the connected node.
     * <p>
     * The magic number is an ingredient, e.g., when generating the hash of a transaction.
     * <p>
     * The magic number is represented as an unsigned 32-bit integer on the Neo node. Thus, it's maximum possible
     * value is 0xffffffff or 2<sup>32</sup>-1.
     *
     * @return the network's magic number.
     * @see Neow3j#getNetworkMagic()
     */
    public Long getNetworkMagic() {
        return this.nodeNetworkMagic;
    }

    /**
     * Gets the maximum time in milliseconds that can pass from the construction of a transaction until it gets
     * included in a block. A transaction becomes invalid after this time increment is surpassed.
     *
     * @return the maximum valid until block time increment.
     * @see Neow3j#getMaxValidUntilBlockIncrement()
     */
    public long getMaxValidUntilBlockIncrement() {
        if (this.nodeMaxValidUntilBlockIncrement == null) {
            throw new IllegalStateException("The max valid until block increment is not set.");
        }
        return this.nodeMaxValidUntilBlockIncrement;
    }

    // endregion
    // region neow3j configuration values

    /**
     * Gets the interval in milliseconds in which {@code Neow3j} polls the neo-node for new block information when
     * observing the blockchain.
     *
     * @return the polling interval in milliseconds.
     * @see Neow3j#getPollingInterval()
     */
    public long getPollingInterval() {
        return pollingInterval;
    }

    /**
     * Set the interval in milliseconds in which {@code Neow3j} should poll the neo-node for new block information
     * when observing the blockchain.
     *
     * @param pollingInterval The polling interval in milliseconds.
     * @return this.
     */
    public Neow3jConfig setPollingInterval(long pollingInterval) {
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
        this.scheduledExecutorService = executorService;
        return this;
    }

    /**
     * @return the NeoNameService resolver script hash.
     */
    public Hash160 getNNSResolver() {
        if (this.nnsResolver == null) {
            throw new IllegalStateException("The NNS resolver script hash is not set.");
        }
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

    // endregion

}
