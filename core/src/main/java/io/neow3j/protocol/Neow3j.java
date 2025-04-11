package io.neow3j.protocol;

import io.neow3j.protocol.core.JsonRpc2_0Neow3j;
import io.neow3j.protocol.core.Neo;
import io.neow3j.protocol.core.response.NeoGetVersion;
import io.neow3j.protocol.rx.Neow3jRx;
import io.neow3j.types.Hash160;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import static io.neow3j.protocol.Neow3jConfig.defaultNeow3jConfig;

/**
 * JSON-RPC Request object building factory.
 */
public abstract class Neow3j implements Neo, Neow3jRx {

    private final Neow3jConfig config;

    protected Neow3j(Neow3jConfig config) {
        this.config = config;
    }

    /**
     * Constructs a new Neow3j instance with the default configuration.
     *
     * @param neow3jService a neow3j service instance, i.e., HTTP or IPC.
     * @return the new Neow3j instance.
     */
    public static Neow3j build(Neow3jService neow3jService) throws IOException {
        return build(neow3jService, defaultNeow3jConfig());
    }

    /**
     * Constructs a new Neow3j instance using the given configuration.
     *
     * @param neow3jService a neow3j service instance, i.e., HTTP or IPC.
     * @param config        the configuration to use.
     * @return the new Neow3j instance.
     */
    public static Neow3j build(Neow3jService neow3jService, Neow3jConfig config) throws IOException {
        return new JsonRpc2_0Neow3j(neow3jService, config);
    }

    /**
     * Constructs a new Neow3j instance with an offline service and the default configuration.
     * <p>
     * The returned Neow3j instance will not be able to perform any requests to a Neo node.
     *
     * @return a new Neow3j instance with an {@link OfflineService}.
     */
    public static Neow3j build() throws IOException {
        return build(OfflineService.newInstance());
    }

    /**
     * Sets Neow3j's configuration based on the connected Neo node's protocol. This does not overwrite any configuration
     * that was set manually.
     *
     * @throws IOException if something goes wrong when communicating with the Neo node.
     */
    protected void setConfigFromNodeProtocol() throws IOException {
        NeoGetVersion.NeoVersion.Protocol protocol = this.getVersion().send().getVersion().getProtocol();
        this.config.setConfigFromNodeProtocol(protocol);
    }

    /**
     * Shutdowns a Neow3j instance and closes opened resources.
     */
    public abstract void shutdown();

    /**
     * @return true if transmission is allowed when the provided script leads to a
     * {@link io.neow3j.types.NeoVMStateType#FAULT}. False, otherwise.
     */
    public boolean transmissionOnFaultIsAllowed() {
        return config.transmissionOnFaultIsAllowed();
    }

    /**
     * Allow the transmission of scripts that lead to a {@link io.neow3j.types.NeoVMStateType#FAULT}.
     */
    public void allowTransmissionOnFault() {
        config.allowTransmissionOnFault();
    }

    /**
     * Prevent the transmission of scripts that lead to a {@link io.neow3j.types.NeoVMStateType#FAULT}.
     * <p>
     * This is set by default.
     */
    public void preventTransmissionOnFault() {
        config.preventTransmissionOnFault();
    }

    /**
     * Gets the executor service used for polling new blocks from the Neo node.
     * <p>
     * The default executor service is a {@link ScheduledThreadPoolExecutor} with as many threads as CPUs available
     * to the JVM.
     *
     * @return the configured executor service.
     */
    public ScheduledExecutorService getScheduledExecutorService() {
        return config.getScheduledExecutorService();
    }

    /**
     * Gets the interval in milliseconds in which {@code Neow3j} should poll the Neo node for new block information
     * when observing the blockchain.
     * <p>
     * Defaults to {@link Neow3jConfig#DEFAULT_BLOCK_TIME}.
     *
     * @return the polling interval in milliseconds.
     */
    public long getPollingInterval() {
        return config.getPollingInterval();
    }

    /**
     * @return the NeoNameService resolver script hash that is configured in the {@link Neow3jConfig}.
     */
    public Hash160 getNNSResolver() {
        return config.getNNSResolver();
    }

    /**
     * Sets the NeoNameService script hash that should be used to resolve NNS domain names.
     *
     * @param nnsResolver the NNS resolver script hash.
     */
    public void setNNSResolver(Hash160 nnsResolver) {
        config.setNNSResolver(nnsResolver);
    }

    // region helper functions for important frequently used values

    /**
     * Gets the network magic number from the connected Neo node as an integer.
     * <p>
     * The magic number is an ingredient, e.g., when generating the hash of a transaction.
     *
     * @return the connected Neo node network's magic number.
     * @throws IOException if something goes wrong when communicating with the Neo node.
     */
    public long getNetworkMagic() throws IOException {
        return getProtocol().getNetwork();
    }

    /**
     * Gets the maximum time in milliseconds that can pass form the construction of a transaction until it gets
     * included in a block. A transaction becomes invalid after this time increment is surpassed.
     *
     * @return the maximum valid until block time increment.
     */
    public long getMaxValidUntilBlockIncrement() throws IOException {
        return getProtocol().getMaxValidUntilBlockIncrement();
    }

    private NeoGetVersion.NeoVersion.Protocol getProtocol() throws IOException {
        return getVersion().send().getVersion().getProtocol();
    }

}
