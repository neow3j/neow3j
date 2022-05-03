package io.neow3j.protocol;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.neow3j.protocol.core.Request;
import io.neow3j.protocol.core.Response;
import io.neow3j.protocol.notifications.Notification;
import io.neow3j.utils.Async;
import io.reactivex.Observable;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import static java.lang.String.format;

/**
 * Base service implementation.
 */
public abstract class Service implements Neow3jService {

    protected final ObjectMapper objectMapper;

    protected ExecutorService asyncExecutorService;

    /**
     * Creates a Service.
     *
     * @param executorService     an external ExecutorService where {@link Request} calls should run.
     * @param includeRawResponses whether to include raw responses on the {@link Response} object.
     */
    public Service(ExecutorService executorService, boolean includeRawResponses) {
        objectMapper = ObjectMapperFactory.getObjectMapper(includeRawResponses);
        asyncExecutorService = executorService;
    }

    /**
     * Creates a Service.
     *
     * @param includeRawResponses whether to include raw responses on the {@link Response} object.
     */
    public Service(boolean includeRawResponses) {
        objectMapper = ObjectMapperFactory.getObjectMapper(includeRawResponses);
    }

    protected abstract InputStream performIO(String payload) throws IOException;

    @Override
    public <T extends Response> T send(Request request, Class<T> responseType) throws IOException {
        String payload = objectMapper.writeValueAsString(request);

        try (InputStream result = performIO(payload)) {
            if (result != null) {
                return objectMapper.readValue(result, responseType);
            } else {
                return null;
            }
        }
    }

    @Override
    public <T extends Response> CompletableFuture<T> sendAsync(Request jsonRpc20Request, Class<T> responseType) {
        return Async.run(() -> send(jsonRpc20Request, responseType), asyncExecutorService);
    }

    @Override
    public <T extends Notification<?>> Observable<T> subscribe(Request request, String unsubscribeMethod,
            Class<T> responseType) {
        throw new UnsupportedOperationException(
                format("Service %s does not support subscriptions", this.getClass().getSimpleName()));
    }

}
