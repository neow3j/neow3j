package io.neow3j.protocol;

import io.neow3j.protocol.core.JsonRpc2_0Neow3j;
import io.neow3j.protocol.core.Neo;
import io.neow3j.protocol.rx.Neow3jRx;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ScheduledExecutorService;

/**
 * JSON-RPC Request object building factory.
 */
public abstract class Neow3j implements Neo, Neow3jRx {

    private final Neow3jConfig config;

    public Neow3j(Neow3jConfig config) {
        this.config = config;
    }

    /**
     * Construct a new Neow3j instance.
     *
     * @param neow3jService neow3j service instance - i.e. HTTP or IPC
     * @return new Neow3j instance
     */
    public static Neow3j build(Neow3jService neow3jService) {
        return new JsonRpc2_0Neow3j(neow3jService, new Neow3jConfig());
    }

    /**
     * Construct a new Neow3j instance.
     *
     * @param neow3jService neow3j service instance - i.e. HTTP or IPC
     * @return new Neow3j instance.
     */
    public static Neow3j build(Neow3jService neow3jService, Neow3jConfig config) {
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

}
