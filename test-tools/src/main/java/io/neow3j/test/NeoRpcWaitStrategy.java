package io.neow3j.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.containers.ContainerLaunchException;
import org.testcontainers.containers.wait.strategy.AbstractWaitStrategy;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Waits until the node can read its initialized blockchain through RPC. An open port alone does not guarantee that
 * the genesis block has been persisted. Startup failures are retried within the configured container startup timeout.
 */
final class NeoRpcWaitStrategy extends AbstractWaitStrategy {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final byte[] BLOCK_COUNT_REQUEST =
            "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"getblockcount\",\"params\":[]}".getBytes(UTF_8);
    private static final int POLL_INTERVAL_MILLIS = 100;
    private static final int REQUEST_TIMEOUT_MILLIS = 1000;

    private final int rpcPort;

    NeoRpcWaitStrategy(int rpcPort) {
        this.rpcPort = rpcPort;
    }

    @Override
    protected void waitUntilReady() {
        URL url;
        try {
            url = new URL("http", waitStrategyTarget.getHost(), waitStrategyTarget.getMappedPort(rpcPort), "/");
        } catch (IOException e) {
            throw new ContainerLaunchException("Invalid Neo RPC URL", e);
        }

        long startNanos = System.nanoTime();
        IOException lastFailure = null;
        SocketTimeoutException lastTimeout = null;
        Duration remaining = startupTimeout;
        while (!remaining.isNegative() && !remaining.isZero()) {
            try {
                checkBlockCount(url, remaining);
                return;
            } catch (SocketTimeoutException e) {
                lastTimeout = e;
            } catch (IOException e) {
                lastFailure = e;
            }

            remaining = startupTimeout.minusNanos(System.nanoTime() - startNanos);
            if (remaining.isNegative() || remaining.isZero()) {
                break;
            }
            try {
                TimeUnit.NANOSECONDS.sleep(Math.min(TimeUnit.MILLISECONDS.toNanos(POLL_INTERVAL_MILLIS),
                        remaining.toNanos()));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ContainerLaunchException("Interrupted while waiting for Neo RPC at " + url, e);
            }
            remaining = startupTimeout.minusNanos(System.nanoTime() - startNanos);
        }
        // Preserve the last response or connection failure as the cause. An expiring request must not hide an
        // earlier RPC error; keep its timeout as additional diagnostic information.
        ContainerLaunchException failure = new ContainerLaunchException(
                "Timed out waiting for Neo RPC at " + url + " to return a positive block count",
                lastFailure != null ? lastFailure : lastTimeout);
        if (lastFailure != null && lastTimeout != null) {
            failure.addSuppressed(lastTimeout);
        }
        throw failure;
    }

    /**
     * Reads the block count without changing blockchain state. RPC errors, malformed responses and a missing genesis
     * block indicate that the node is not ready yet. Individual network operations are bounded by the remaining wait.
     *
     * @param url       the node's RPC URL.
     * @param remaining the remaining startup timeout.
     * @throws IOException if the request fails or the node does not report an initialized blockchain.
     */
    private static void checkBlockCount(URL url, Duration remaining) throws IOException {
        long requestStartNanos = System.nanoTime();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        int timeoutMillis = requestTimeoutMillis(remaining, requestStartNanos);
        connection.setConnectTimeout(timeoutMillis);
        connection.setReadTimeout(timeoutMillis);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        connection.setFixedLengthStreamingMode(BLOCK_COUNT_REQUEST.length);
        try {
            try (OutputStream output = connection.getOutputStream()) {
                output.write(BLOCK_COUNT_REQUEST);
            }
            connection.setReadTimeout(requestTimeoutMillis(remaining, requestStartNanos));
            int status = connection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                throw new IOException("Neo RPC readiness request returned HTTP " + status);
            }
            // Socket timeouts apply to individual reads. Also check the overall deadline so that a response arriving
            // in small chunks cannot keep this request alive past the startup timeout.
            try (InputStream input = new FilterInputStream(connection.getInputStream()) {
                @Override
                public int read() throws IOException {
                    requestTimeoutMillis(remaining, requestStartNanos);
                    return super.read();
                }

                @Override
                public int read(byte[] bytes, int offset, int length) throws IOException {
                    requestTimeoutMillis(remaining, requestStartNanos);
                    return super.read(bytes, offset, length);
                }
            }) {
                JsonNode response = OBJECT_MAPPER.readTree(input);
                JsonNode result = response == null ? null : response.get("result");
                JsonNode error = response == null ? null : response.get("error");
                if ((error != null && !error.isNull()) || result == null || !result.isIntegralNumber()
                        || result.bigIntegerValue().signum() <= 0) {
                    throw new IOException("Neo RPC does not report an initialized blockchain: " + response);
                }
                requestTimeoutMillis(remaining, requestStartNanos);
            }
        } finally {
            connection.disconnect();
        }
    }

    /**
     * Caps a network timeout to the time still available for this request and rejects an expired startup deadline.
     *
     * @param budget            the startup time available when the request began.
     * @param requestStartNanos the request's monotonic start time.
     * @return the network timeout in milliseconds, which is always positive because zero means no socket timeout.
     * @throws SocketTimeoutException if the startup deadline has expired.
     */
    private static int requestTimeoutMillis(Duration budget, long requestStartNanos) throws SocketTimeoutException {
        Duration remaining = budget.minusNanos(System.nanoTime() - requestStartNanos);
        if (remaining.isNegative() || remaining.isZero()) {
            throw new SocketTimeoutException("Neo RPC readiness request exceeded the remaining startup time");
        }
        return (int) Math.max(1, Math.min(REQUEST_TIMEOUT_MILLIS, remaining.toMillis()));
    }

}
