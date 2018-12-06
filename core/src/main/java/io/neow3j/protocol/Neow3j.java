package io.neow3j.protocol;

import io.neow3j.protocol.core.JsonRpc2_0Neow3j;
import io.neow3j.protocol.core.Neo;
import io.neow3j.protocol.rx.Neow3jRx;

import java.util.concurrent.ScheduledExecutorService;

/**
 * JSON-RPC Request object building factory.
 */
public interface Neow3j extends Neo, Neow3jRx {

    /**
     * Construct a new Neow3j instance.
     *
     * @param neow3jService neow3j service instance - i.e. HTTP or IPC
     * @return new Neow3j instance
     */
    static Neow3j build(Neow3jService neow3jService) {
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
    static Neow3j build(
            Neow3jService neow3jService, long pollingInterval,
            ScheduledExecutorService scheduledExecutorService) {
        return new JsonRpc2_0Neow3j(neow3jService, pollingInterval, scheduledExecutorService);
    }

    /**
     * Shutdowns a Neow3j instance and closes opened resources.
     */
    void shutdown();
}
