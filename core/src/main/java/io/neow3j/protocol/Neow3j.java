package io.neow3j.protocol;

import io.neow3j.protocol.core.JsonRpc2_0Neow3j;
import io.neow3j.protocol.core.Neo;
import io.neow3j.protocol.core.response.NeoGetVersion;
import io.neow3j.protocol.rx.Neow3jRx;
import io.neow3j.types.Hash160;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import static io.neow3j.protocol.Neow3jConfig.defaultNeow3jConfig;
import static io.neow3j.protocol.core.JsonRpc2_0Neow3j.initializedJsonRpc2_0Neow3j;

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
    public static Neow3j build(Neow3jService neow3jService) {
        return build(neow3jService, defaultNeow3jConfig());
    }

    /**
     * Constructs a new Neow3j instance using the given configuration.
     *
     * @param neow3jService a neow3j service instance, i.e., HTTP or IPC.
     * @param config        the configuration to use.
     * @return the new Neow3j instance.
     */
    public static Neow3j build(Neow3jService neow3jService, Neow3jConfig config) {
        return new JsonRpc2_0Neow3j(neow3jService, config);
    }

    /**
     * Constructs a new Neow3j instance using the given configuration.
     * <p>
     * Fetches version specific information from the node to be used in this instance's configuration. This will
     * overwrite variables in the config for the variables contained in {@link Neow3jConfig.ConfigValue}. Note, that
     * it only overwrites default values. If a variable (e.g., msPerBlock) has been manually set in the config
     * parameter (i.e., with {@link Neow3jConfig#setBlockInterval(int)}), this variable will NOT be overwritten by
     * the node value. That means: {@code user-set value >> node-provided value >> default values} | where {@code a
     * >> b} means that a will be used if a is present.
     *
     * @param neow3jService a neow3j service instance, i.e., HTTP or IPC.
     * @param config        the configuration to use.
     * @return the new Neow3j instance.
     * @throws IOException if something goes wrong when communicating with the Neo node.
     */
    public static Neow3j init(Neow3jService neow3jService, Neow3jConfig config) throws IOException {
        return initializedJsonRpc2_0Neow3j(neow3jService, config);
    }

    protected void setNodeVersionInfo(NeoGetVersion.NeoVersion.Protocol protocol) {
         config.setNodeVersionInfo(protocol);
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
     * Gets the configured network magic number.
     * <p>
     * The magic number is an ingredient, e.g., when generating the hash of a transaction.
     * <p>
     * The default value is null. Only once this method is called for the first time the value is fetched from the
     * connected Neo node.
     *
     * @return the network's magic number.
     * @throws IOException if something goes wrong when communicating with the Neo node.
     */
    public byte[] getNetworkMagicNumberBytes() throws IOException {
        if (config.getNetworkMagic() == null) {
            config.setNetworkMagic(getVersion().send().getVersion().getProtocol().getNetwork());
        }
        // transform from long to unsigned int:
        int networkMagicAsInt = (int) (config.getNetworkMagic() & 0xFFFFFFFFL);
        byte[] array = new byte[4];
        ByteBuffer.wrap(array).order(ByteOrder.LITTLE_ENDIAN).putInt(networkMagicAsInt);
        return array;
    }

    /**
     * Gets the configured network magic number as an integer.
     * <p>
     * The magic number is an ingredient, e.g., when generating the hash of a transaction.
     * <p>
     * The default value is null. Only once this method is called for the first time the value is fetched from the
     * connected Neo node.
     *
     * @return the network's magic number.
     * @throws IOException if something goes wrong when communicating with the Neo node.
     */
    public long getNetworkMagicNumber() throws IOException {
        if (config.getNetworkMagic() == null) {
            config.setNetworkMagic(getVersion().send().getVersion().getProtocol().getNetwork());
        }
        return config.getNetworkMagic();
    }

    /**
     * @return the NeoNameService resolver script hash that is configured in the {@link Neow3jConfig}.
     */
    public Hash160 getNNSResolver() {
        return config.getNNSResolver();
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
     * Gets the interval in milliseconds in which blocks are produced.
     * <p>
     * Defaults to {@link Neow3jConfig#DEFAULT_BLOCK_TIME}.
     *
     * @return the block interval in milliseconds.
     */
    public int getBlockInterval() {
        return config.getBlockInterval();
    }

    /**
     * Gets the interval in milliseconds in which {@code Neow3j} should poll the Neo node for new block information
     * when observing the blockchain.
     * <p>
     * Defaults to {@link Neow3jConfig#DEFAULT_BLOCK_TIME}.
     *
     * @return the polling interval in milliseconds.
     */
    public int getPollingInterval() {
        return config.getPollingInterval();
    }

    /**
     * Gets the maximum time in milliseconds that can pass form the construction of a transaction until it gets
     * included in a block. A transaction becomes invalid after this time increment is surpassed.
     *
     * @return the maximum valid until block time increment.
     */
    public long getMaxValidUntilBlockIncrement() {
        return config.getMaxValidUntilBlockIncrement();
    }

    /**
     * Sets the NeoNameService script hash that should be used to resolve NNS domain names.
     *
     * @param nnsResolver the NNS resolver script hash.
     */
    public void setNNSResolver(Hash160 nnsResolver) {
        config.setNNSResolver(nnsResolver);
    }

}
