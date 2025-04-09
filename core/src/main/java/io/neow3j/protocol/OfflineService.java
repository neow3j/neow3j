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

    public static Neow3jService newInstance() {
        return new OfflineService();
    }

    @Override
    public <T extends Response> T send(Request request, Class<T> responseType) throws IOException {
        throw new OfflineServiceException();
    }

    @Override
    public <T extends Response> CompletableFuture<T> sendAsync(Request request, Class<T> responseType) {
        throw new OfflineServiceException();
    }

    @Override
    public <T extends Notification<?>> Observable<T> subscribe(Request request, String unsubscribeMethod,
            Class<T> responseType) {
        throw new OfflineServiceException();
    }

    @Override
    public void close() throws IOException {
        throw new OfflineServiceException();
    }

}
