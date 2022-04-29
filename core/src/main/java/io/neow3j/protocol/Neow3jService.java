package io.neow3j.protocol;

import io.neow3j.protocol.core.Request;
import io.neow3j.protocol.core.Response;
import io.neow3j.protocol.notifications.Notification;
import io.reactivex.Observable;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * Services API.
 */
public interface Neow3jService {

    /**
     * Performs a synchronous JSON-RPC request.
     *
     * @param request      the request to perform.
     * @param responseType the class of a data item returned by the request.
     * @param <T>          the type of a data item returned by the request.
     * @return the deserialized JSON-RPC response.
     * @throws IOException if the request could not be performed.
     */
    <T extends Response> T send(Request request, Class<T> responseType) throws IOException;

    /**
     * Performs an asynchronous JSON-RPC request.
     *
     * @param request      the request to perform.
     * @param responseType the class of a data item returned by the request.
     * @param <T>          the type of a data item returned by the request.
     * @return a CompletableFuture that will be completed when a result is returned or the request has failed.
     */
    <T extends Response> CompletableFuture<T> sendAsync(Request request, Class<T> responseType);

    /**
     * Subscribe to a stream of notifications. A stream of notifications is opened by by performing a specified
     * JSON-RPC request and is closed by calling the unsubscribe method. Different WebSocket implementations use
     * different pair of subscribe/unsubscribe methods.
     * <p>
     * This method creates an Observable that can be used to subscribe to new notifications. When a client
     * unsubscribes from this Observable the service unsubscribes from the underlying stream of events.
     *
     * @param request           the JSON-RPC request that will be send to subscribe to a stream of events.
     * @param unsubscribeMethod the method that will be called to unsubscribe from a stream of notifications.
     * @param responseType      the class of incoming events objects in a stream.
     * @param <T>               the type of incoming event objects.
     * @return an Observable that emits incoming events.
     */
    <T extends Notification<?>> Observable<T> subscribe(
            Request request,
            String unsubscribeMethod,
            Class<T> responseType);

    /**
     * Closes resources used by the service.
     *
     * @throws IOException if a service failed to close all resources.
     */
    void close() throws IOException;

}
