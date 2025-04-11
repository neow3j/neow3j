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

    /**
     * The interval in milliseconds in which {@code Neow3j} polls the Neo node for new block information when
     * observing the blockchain.
     * <p>
     * There is no default polling interval needed because this value is set when connecting to the Neo node and this
     * value is only needed if there is a connection to a node. Then, this value is set to the node's milliseconds
     * per block.
     */
    private Long pollingInterval = null; //
    /**
     * If set to true, allows
     */
    private boolean allowTransmissionOnFault = false;
    /**
     * The NeoNameService resolver script hash. If another network than mainnet is used, this must be set manually if
     * neow3j functions that use the NeoNameService are used.
     */
    private Hash160 nnsResolver = null;
    /**
     * The executor service used for polling new blocks from the Neo node.
     */
    private ScheduledExecutorService scheduledExecutorService = Async.defaultExecutorService();

    private Neow3jConfig() {
    }

    /**
     * @return a Neow3jConfig object with default configuration.
     */
    public static Neow3jConfig defaultNeow3jConfig() {
        return new Neow3jConfig();
    }

    void setConfigFromNodeProtocol(NeoGetVersion.NeoVersion.Protocol protocol) {
        // The polling interval is set to the node's milliseconds per block if it has not been set manually.
        if (this.pollingInterval == null) {
            this.pollingInterval = protocol.getMilliSecondsPerBlock();
        }
        if (this.nnsResolver == null && isMainnet(protocol)) {
            this.nnsResolver = MAINNET_NNS_CONTRACT_HASH;
        }
    }

    private boolean isMainnet(NeoGetVersion.NeoVersion.Protocol protocol) {
        return protocol.getNetwork().equals(MAINNET_NETWORK_MAGIC);
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
