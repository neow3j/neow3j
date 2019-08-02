package io.neow3j.protocol.http;

import io.neow3j.protocol.Service;
import io.neow3j.protocol.core.Request;
import io.neow3j.protocol.core.Response;
import io.neow3j.protocol.exceptions.ClientConnectionException;
import io.neow3j.utils.Async;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import okio.Buffer;
import okio.BufferedSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * HTTP implementation of the Service API.
 */
public class HttpService extends Service {

    public static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    public static final String DEFAULT_URL = "http://localhost:10333/";

    private static final Logger log = LoggerFactory.getLogger(HttpService.class);
    private final String url;
    private final boolean includeRawResponses;
    private OkHttpClient httpClient;
    private HashMap<String, String> headers = new HashMap<>();

    /**
     * Create an {@link HttpService} instance.
     *
     * @param url                 the URL to the HTTP service (JSON-RPC).
     * @param httpClient          the HTTP client instance.
     * @param executorService     an external ExecutorService where asynchronous {@link Request} calls should run.
     * @param includeRawResponses option to include or not raw responses on the {@link Response} object.
     */
    public HttpService(String url, OkHttpClient httpClient,
                       ExecutorService executorService,
                       boolean includeRawResponses) {
        super(executorService, includeRawResponses);
        this.url = url;
        this.httpClient = httpClient;
        this.includeRawResponses = includeRawResponses;
    }

    /**
     * <p>Create an {@link HttpService} instance.</p>
     * <br>
     * <p>An internal {@link ExecutorService} is used to run asynchronous {@link Request} calls,
     * defined by {@link Async#defaultExecutorService()}.</p>
     *
     * @param url                 the URL to the HTTP service (JSON-RPC).
     * @param httpClient          the HTTP client instance.
     * @param includeRawResponses option to include or not raw responses on the {@link Response} object.
     */
    public HttpService(String url, OkHttpClient httpClient, boolean includeRawResponses) {
        this(url, httpClient, null, includeRawResponses);
    }

    /**
     * <p>Create an {@link HttpService} instance.</p>
     * <br>
     * <p>The URL is set to {@link HttpService#DEFAULT_URL}.</p>
     *
     * @param httpClient          the HTTP client instance.
     * @param executorService     an external ExecutorService where asynchronous {@link Request} calls should run.
     * @param includeRawResponses option to include or not raw responses on the {@link Response} object.
     */
    public HttpService(OkHttpClient httpClient, ExecutorService executorService, boolean includeRawResponses) {
        this(DEFAULT_URL, httpClient, executorService, includeRawResponses);
    }

    /**
     * <p>Create an {@link HttpService} instance.</p>
     * <br>
     * <p>The URL is set to {@link HttpService#DEFAULT_URL}.</p>
     * <br>
     * <p>An internal {@link ExecutorService} is used to run asynchronous {@link Request} calls,
     * defined by {@link Async#defaultExecutorService()}.</p>
     *
     * @param httpClient          the HTTP client instance.
     * @param includeRawResponses option to include or not raw responses on the {@link Response} object.
     */
    public HttpService(OkHttpClient httpClient, boolean includeRawResponses) {
        this(DEFAULT_URL, httpClient, includeRawResponses);
    }

    /**
     * <p>Create an {@link HttpService} instance.</p>
     * <br>
     * <p>The {@link #includeRawResponses} is set to false.</p>
     *
     * @param url             the URL to the HTTP service (JSON-RPC).
     * @param httpClient      the HTTP client instance.
     * @param executorService an external ExecutorService where asynchronous {@link Request} calls should run.
     */
    public HttpService(String url, OkHttpClient httpClient, ExecutorService executorService) {
        this(url, httpClient, executorService, false);
    }

    /**
     * <p>Create an {@link HttpService} instance.</p>
     * <br>
     * <p>An internal {@link ExecutorService} is used to run asynchronous {@link Request} calls,
     * defined by {@link Async#defaultExecutorService()}.</p>
     * <br>
     * <p>The {@link #includeRawResponses} is set to false.</p>
     *
     * @param url        the URL to the HTTP service (JSON-RPC).
     * @param httpClient the HTTP client instance.
     */
    public HttpService(String url, OkHttpClient httpClient) {
        this(url, httpClient, false);
    }

    /**
     * <p>Create an {@link HttpService} instance.</p>
     * <br>
     * <p>The HTTP client used is set by default by {@link #createOkHttpClient()}.</p>
     * <br>
     * <p>The {@link #includeRawResponses} is set to false.</p>
     *
     * @param url             the URL to the HTTP service (JSON-RPC).
     * @param executorService an external ExecutorService where asynchronous {@link Request} calls should run.
     */
    public HttpService(String url, ExecutorService executorService) {
        this(url, createOkHttpClient(), executorService);
    }

    /**
     * <p>Create an {@link HttpService} instance.</p>
     * <br>
     * <p>An internal {@link ExecutorService} is used to run asynchronous {@link Request} calls,
     * defined by {@link Async#defaultExecutorService()}.</p>
     * <br>
     * <p>The HTTP client used is set by default by {@link #createOkHttpClient()}.</p>
     * <br>
     * <p>The {@link #includeRawResponses} is set to false.</p>
     *
     * @param url the URL to the HTTP service (JSON-RPC).
     */
    public HttpService(String url) {
        this(url, createOkHttpClient());
    }

    /**
     * <p>Create an {@link HttpService} instance.</p>
     * <br>
     * <p>The HTTP client used is set by default by {@link #createOkHttpClient()}.</p>
     *
     * @param url                 the URL to the HTTP service (JSON-RPC).
     * @param executorService     an external ExecutorService where asynchronous {@link Request} calls should run.
     * @param includeRawResponses option to include or not raw responses on the {@link Response} object.
     */
    public HttpService(String url, ExecutorService executorService, boolean includeRawResponses) {
        this(url, createOkHttpClient(), executorService, includeRawResponses);
    }

    /**
     * <p>Create an {@link HttpService} instance.</p>
     * <br>
     * <p>An internal {@link ExecutorService} is used to run asynchronous {@link Request} calls,
     * defined by {@link Async#defaultExecutorService()}.</p>
     * <br>
     * <p>The HTTP client used is set by default by {@link #createOkHttpClient()}.</p>
     *
     * @param url                 the URL to the HTTP service (JSON-RPC).
     * @param includeRawResponses option to include or not raw responses on the {@link Response} object.
     */
    public HttpService(String url, boolean includeRawResponses) {
        this(url, createOkHttpClient(), includeRawResponses);
    }

    /**
     * <p>Create an {@link HttpService} instance.</p>
     * <br>
     * <p>The URL is set to {@link HttpService#DEFAULT_URL}.</p>
     * <br>
     * <p>The {@link #includeRawResponses} is set to false.</p>
     *
     * @param httpClient      the HTTP client instance.
     * @param executorService an external ExecutorService where asynchronous {@link Request} calls should run.
     */
    public HttpService(OkHttpClient httpClient, ExecutorService executorService) {
        this(DEFAULT_URL, httpClient, executorService);
    }

    /**
     * <p>Create an {@link HttpService} instance.</p>
     * <br>
     * <p>An internal {@link ExecutorService} is used to run asynchronous {@link Request} calls,
     * defined by {@link Async#defaultExecutorService()}.</p>
     * <br>
     * <p>The URL is set to {@link HttpService#DEFAULT_URL}.</p>
     * <br>
     * <p>The {@link #includeRawResponses} is set to false.</p>
     *
     * @param httpClient the HTTP client instance.
     */
    public HttpService(OkHttpClient httpClient) {
        this(DEFAULT_URL, httpClient);
    }

    /**
     * <p>Create an {@link HttpService} instance.</p>
     * <br>
     * <p>The HTTP client used is set by default by {@link #createOkHttpClient()}.</p>
     * <br>
     * <p>The URL is set to {@link HttpService#DEFAULT_URL}.</p>
     *
     * @param executorService     an external ExecutorService where asynchronous {@link Request} calls should run.
     * @param includeRawResponses option to include or not raw responses on the {@link Response} object.
     */
    public HttpService(ExecutorService executorService, boolean includeRawResponses) {
        this(DEFAULT_URL, executorService, includeRawResponses);
    }

    /**
     * <p>Create an {@link HttpService} instance.</p>
     * <br>
     * <p>An internal {@link ExecutorService} is used to run asynchronous {@link Request} calls,
     * defined by {@link Async#defaultExecutorService()}.</p>
     * <br>
     * <p>The HTTP client used is set by default by {@link #createOkHttpClient()}.</p>
     * <br>
     * <p>The URL is set to {@link HttpService#DEFAULT_URL}.</p>
     *
     * @param includeRawResponses option to include or not raw responses on the {@link Response} object.
     */
    public HttpService(boolean includeRawResponses) {
        this(DEFAULT_URL, includeRawResponses);
    }

    public HttpService() {
        this(DEFAULT_URL);
    }

    private static OkHttpClient createOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        configureLogging(builder);
        return builder.build();
    }

    private static void configureLogging(OkHttpClient.Builder builder) {
        if (log.isDebugEnabled()) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor(log::debug);
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(logging);
        }
    }

    @Override
    protected InputStream performIO(String request) throws IOException {

        RequestBody requestBody = RequestBody.create(JSON_MEDIA_TYPE, request);
        Headers headers = buildHeaders();

        okhttp3.Request httpRequest = new okhttp3.Request.Builder()
                .url(url)
                .headers(headers)
                .post(requestBody)
                .build();

        okhttp3.Response response = httpClient.newCall(httpRequest).execute();
        ResponseBody responseBody = response.body();
        if (response.isSuccessful()) {
            if (responseBody != null) {
                return buildInputStream(responseBody);
            } else {
                return null;
            }
        } else {
            int code = response.code();
            String text = responseBody == null ? "N/A" : responseBody.string();

            throw new ClientConnectionException("Invalid response received: " + code + "; " + text);
        }
    }

    private InputStream buildInputStream(ResponseBody responseBody) throws IOException {
        InputStream inputStream = responseBody.byteStream();

        if (includeRawResponses) {
            // we have to buffer the entire input payload, so that after processing
            // it can be re-read and used to populate the rawResponse field.

            BufferedSource source = responseBody.source();
            source.request(Long.MAX_VALUE); // Buffer the entire body
            Buffer buffer = source.buffer();

            long size = buffer.size();
            if (size > Integer.MAX_VALUE) {
                throw new UnsupportedOperationException("Non-integer input buffer size specified: " + size);
            }

            int bufferSize = (int) size;
            BufferedInputStream bufferedinputStream = new BufferedInputStream(inputStream, bufferSize);

            bufferedinputStream.mark(inputStream.available());
            return bufferedinputStream;

        } else {
            return inputStream;
        }
    }

    private Headers buildHeaders() {
        return Headers.of(headers);
    }

    /**
     * Adds an HTTP header to all {@link Request}
     * calls used by this service.
     *
     * @param key   the header name (e.g., "Authorization").
     * @param value the header value (e.g., "Bearer secretBearer").
     */
    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    /**
     * Adds multiple HTTP headers to all {@link Request}
     * calls used by this service.
     *
     * @param headersToAdd a key-value map containing keys (e.g., "Authorization")
     *                     and values (e.g., "Bearer secretBearer").
     */
    public void addHeaders(Map<String, String> headersToAdd) {
        headers.putAll(headersToAdd);
    }

    /**
     * Get all custom headers set to this service.
     *
     * @return the map containing custom headers set.
     */
    public HashMap<String, String> getHeaders() {
        return headers;
    }

    @Override
    public void close() {

    }
}
