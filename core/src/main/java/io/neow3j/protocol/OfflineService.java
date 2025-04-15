package io.neow3j.protocol;

import io.neow3j.protocol.core.Request;
import io.neow3j.protocol.core.Response;
import io.neow3j.protocol.exceptions.OfflineServiceException;
import io.neow3j.protocol.notifications.Notification;
import io.reactivex.Observable;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * An implementation of the Service API that restricts using any of its functions and throws appropriate exceptions
 * if they are used.
 */
public class OfflineService implements Neow3jService {

    private OfflineService() {
    }

    /**
     * @return a new instance of {@link OfflineService}.
     */
    public static Neow3jService newInstance() {
        return new OfflineService();
    }

    /**
     * Immediately throws an {@link OfflineServiceException}.
     * <p>
     * See {@link Neow3jService#send(Request, Class)} for more details.
     * <p>
     * If you want to connect to a Neo node, make sure to use a {@link Neow3jService} implementation that can connect
     * to a node.
     *
     * @param request      irrelevant.
     * @param responseType irrelevant.
     * @param <T>          irrelevant.
     * @return irrelevant.
     */
    @Override
    public <T extends Response> T send(Request request, Class<T> responseType) throws IOException {
        throw new OfflineServiceException();
    }

    /**
     * Immediately throws an {@link OfflineServiceException}.
     * <p>
     * See {@link Neow3jService#sendAsync(Request, Class)} for more details.
     * <p>
     * If you want to connect to a Neo node, make sure to use a {@link Neow3jService} implementation that can connect
     * to a node.
     *
     * @param request      irrelevant.
     * @param responseType irrelevant.
     * @param <T>          irrelevant.
     * @return irrelevant.
     */
    @Override
    public <T extends Response> CompletableFuture<T> sendAsync(Request request, Class<T> responseType) {
        throw new OfflineServiceException();
    }

    /**
     * Immediately throws an {@link OfflineServiceException}.
     * <p>
     * See {@link Neow3jService#subscribe(Request, String, Class)} for more details.
     * <p>
     * If you want to connect to a Neo node, make sure to use a {@link Neow3jService} implementation that can connect
     * to a node.
     *
     * @param request           irrelevant.
     * @param unsubscribeMethod irrelevant.
     * @param responseType      irrelevant.
     * @param <T>               irrelevant.
     * @return irrelevant.
     */
    @Override
    public <T extends Notification<?>> Observable<T> subscribe(Request request, String unsubscribeMethod,
            Class<T> responseType) {
        throw new OfflineServiceException();
    }

    /**
     * Immediately throws an {@link OfflineServiceException}.
     * <p>
     * See {@link Neow3jService#close()} for more details.
     * <p>
     * If you want to connect to a Neo node, make sure to use a {@link Neow3jService} implementation that can connect
     * to a node.
     *
     * @throws IOException irrelevant. This is required to implement {@link Neow3jService}.
     */
    @Override
    public void close() throws IOException {
        throw new OfflineServiceException();
    }

}
