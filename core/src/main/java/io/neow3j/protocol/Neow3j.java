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

    private Integer networkMagicNumber;

    /**
     * Construct a new Neow3j instance.
     *
     * @param neow3jService neow3j service instance - i.e. HTTP or IPC
     * @return new Neow3j instance
     */
    public static Neow3j build(Neow3jService neow3jService) {
        return new JsonRpc2_0Neow3j(neow3jService);
    }

    /**
     * Construct a new Neow3j instance.
     *
     * @param neow3jService            neow3j service instance - i.e. HTTP or IPC
     * @param pollingInterval          polling interval for responses from network nodes
     * @param scheduledExecutorService executor service to use for scheduled tasks.
     *                                 <strong>You are responsible for terminating this thread
     *                                 pool</strong>
     * @return new Neow3j instance
     */
    public static Neow3j build(
            Neow3jService neow3jService, long pollingInterval,
            ScheduledExecutorService scheduledExecutorService) {
        return new JsonRpc2_0Neow3j(neow3jService, pollingInterval, scheduledExecutorService);
    }

    /**
     * Shutdowns a Neow3j instance and closes opened resources.
     */
    public abstract void shutdown();

    /**
     * Gets the magic number of the connect Neo network.
     * <p>
     * If the magic number is not explicitly set with {@link Neow3j#setNetworkMagicNumber(int)}, it
     * is retrieved from the connected neo-node.
     *
     * @return The network's magic number.
     * @throws IOException if an error occurs when tyring to fetch the magic number from the
     *                     connected neo-node.
     */
    public byte[] getNetworkMagicNumber() throws IOException {
        if (networkMagicNumber == null) {
            networkMagicNumber = getVersion().send().getVersion().getMagic();
        }
        byte[] array = new byte[4];
        ByteBuffer.wrap(array).order(ByteOrder.LITTLE_ENDIAN).putInt(networkMagicNumber);
        return array;
    }

    public void setNetworkMagicNumber(int magicNumber) {
        networkMagicNumber = magicNumber;
    }

}
