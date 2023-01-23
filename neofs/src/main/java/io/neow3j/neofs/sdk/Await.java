package io.neow3j.neofs.sdk;

import io.neow3j.neofs.sdk.exceptions.NeoFSClientException;
import io.neow3j.neofs.sdk.exceptions.UnexpectedResponseTypeException;
import io.neow3j.wallet.Account;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

/**
 * Utility class used to wait for NeoFS related events like the creation  of a transaction in a block or the
 * deployment of a contract. The maximum time that these utility methods will wait for a specific event is set in
 * {@link Await#MAX_WAIT_TIME}. After that time, the methods will timeout and throw an exception.
 */
public class Await {

    private final static int MAX_WAIT_TIME = 60;

    /**
     * Checks and waits until the container with {@code containerId} is readable with the NeoFS client.
     *
     * @param neofsClient the NeoFS client.
     * @param containerId the container id.
     */
    public static void waitUntilContainerPersists(NeoFSClient neofsClient, String containerId) {
        waitUntil(callableContainerPersists(neofsClient, containerId), Matchers.is(true));
    }

    /**
     * Checks and waits until the object header of the provided object with {@code objectId} in the provided
     * container with {@code containerId} is readable.
     *
     * @param neofsClient   the NeoFS client.
     * @param containerId   the container id.
     * @param objectId      the object id.
     * @param signerAccount the signer account.
     */
    public static void waitUntilObjectPersists(NeoFSClient neofsClient, String containerId, String objectId,
            Account signerAccount) {
        waitUntil(callableObjectPersists(neofsClient, containerId, objectId, signerAccount), Matchers.is(true));
    }

    /**
     * Waits until the {@code callable} function returns a condition that is satisfied by the {@code matcher}. Uses
     * the default {@link Await#MAX_WAIT_TIME} and {@link TimeUnit#SECONDS}.
     *
     * @param callable the function to be called.
     * @param matcher  the condition to be evaluated.
     * @param <T>      the type that the callable function and condition should be compatible with.
     */
    public static <T> void waitUntil(Callable<T> callable, Matcher<? super T> matcher) {
        waitUntil(callable, matcher, MAX_WAIT_TIME, TimeUnit.SECONDS);
    }

    /**
     * Waits until the {@code callable} function returns a condition that is satisfied by the {@code matcher}.
     *
     * @param callable    the function to be called.
     * @param matcher     the condition to be evaluated.
     * @param maxWaitTime the maximum amount of time to wait.
     * @param unit        the time unit for the {@code maxWaitTime} param.
     * @param <T>         the type that the callable function and condition should be compatible with.
     */
    public static <T> void waitUntil(Callable<T> callable, Matcher<? super T> matcher, int maxWaitTime, TimeUnit unit) {
        await().timeout(maxWaitTime, unit).until(callable, matcher);
    }

    private static Callable<Object> callableContainerPersists(NeoFSClient neofsClient, String containerId) {
        return () -> {
            try {
                neofsClient.getContainer(containerId);
            } catch (NeoFSClientException | UnexpectedResponseTypeException e) {
                return false;
            }
            return true;
        };
    }

    private static Callable<Boolean> callableObjectPersists(NeoFSClient neofsClient, String containerId,
            String objectId, Account signerAccount) {

        return () -> {
            try {
                neofsClient.getObjectHeader(containerId, objectId, signerAccount);
            } catch (NeoFSClientException | UnexpectedResponseTypeException e) {
                return false;
            }
            return true;
        };
    }

}
