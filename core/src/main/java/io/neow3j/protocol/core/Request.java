package io.neow3j.protocol.core;

import io.neow3j.protocol.Neow3jService;
import io.reactivex.Observable;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

/**
 * JSON-RPC request type.
 *
 * @param <S> the type of the response's parameters.
 * @param <T> the response type.
 */
public class Request<S, T extends Response> {
    private static AtomicLong nextId = new AtomicLong(0);

    private String jsonrpc = "2.0";
    private String method;
    private List<S> params;
    private long id;

    private Neow3jService neow3jService;

    // Unfortunately require an instance of the type too, see
    // http://stackoverflow.com/a/3437930/3211687
    private Class<T> responseType;

    /**
     * Creates a new empty JSON-RPC request.
     */
    public Request() {
    }

    /**
     * Creates a new JSON-RPC request.
     *
     * @param method        the method.
     * @param params        the parameters.
     * @param neow3jService the neow3j service to use for sending the request.
     * @param type          the class of the response type.
     */
    public Request(String method, List<S> params, Neow3jService neow3jService, Class<T> type) {
        this.method = method;
        this.params = params;
        this.id = nextId.getAndIncrement();
        this.neow3jService = neow3jService;
        this.responseType = type;
    }

    /**
     * @return the JSON-RPC version.
     */
    public String getJsonrpc() {
        return jsonrpc;
    }

    /**
     * Sets the JSON-RPC version.
     *
     * @param jsonrpc the JSON-RPC version.
     */
    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    /**
     * @return the request method.
     */
    public String getMethod() {
        return method;
    }

    /**
     * Sets the request method.
     *
     * @param method the method.
     */
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * @return the request parameters.
     */
    public List<S> getParams() {
        return params;
    }

    /**
     * Sets the request parameters.
     *
     * @param params the parameters.
     */
    public void setParams(List<S> params) {
        this.params = params;
    }

    /**
     * @return the request Id.
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the request Id.
     *
     * @param id the request Id.
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Sends the request to the Neo node and returns the deserialized JSON-RPC response.
     *
     * @return the deserialized JSON-RPC response.
     * @throws IOException if the request could not be performed.
     */
    public T send() throws IOException {
        return neow3jService.send(this, responseType);
    }

    /**
     * Sends the request to the Neo node and returns a CompletableFuture that will be completed when a result is
     * returned or the request has failed.
     *
     * @return a CompletableFuture that will be completed when a result is returned or the request has failed.
     */
    public CompletableFuture<T> sendAsync() {
        return neow3jService.sendAsync(this, responseType);
    }

    /**
     * @return an Observable that emits incoming events.
     */
    public Observable<T> observable() {
        return new RemoteCall<>(this::send).observable();
    }

}
