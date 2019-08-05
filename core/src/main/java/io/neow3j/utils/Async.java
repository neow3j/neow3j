package io.neow3j.utils;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.Optional.ofNullable;

/**
 * Async task facilitation.
 */
public class Async {

    private static final ExecutorService DEFAULT_EXECUTOR = Executors.newCachedThreadPool();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdown(DEFAULT_EXECUTOR)));
    }

    public static <T> CompletableFuture<T> run(Callable<T> callable, ExecutorService executor) {
        ExecutorService executorService = ofNullable(executor).orElse(DEFAULT_EXECUTOR);
        CompletableFuture<T> result = new CompletableFuture<>();
        CompletableFuture.runAsync(() -> {
            // we need to explicitly catch any exceptions,
            // otherwise they will be silently discarded
            try {
                result.complete(callable.call());
            } catch (Throwable e) {
                result.completeExceptionally(e);
            }
        }, executorService);
        return result;
    }

    public static <T> CompletableFuture<T> run(Callable<T> callable) {
        return run(callable, DEFAULT_EXECUTOR);
    }

    /**
     * Get the default {@link ExecutorService} used on asynchronous calls.
     *
     * @return the default instance of {@link ExecutorService}.
     */
    public static ExecutorService getDefaultExecutor() {
        return DEFAULT_EXECUTOR;
    }

    private static int getCpuCount() {
        return Runtime.getRuntime().availableProcessors();
    }

    /**
     * Provide a new ScheduledExecutorService instance.
     *
     * <p>A shutdown hook is created to terminate the thread pool on application termination.
     *
     * @return new ScheduledExecutorService
     */
    public static ScheduledExecutorService defaultExecutorService() {
        ScheduledExecutorService scheduledExecutorService =
                Executors.newScheduledThreadPool(getCpuCount());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdown(scheduledExecutorService)));

        return scheduledExecutorService;
    }

    /**
     * Shutdown as per {@link ExecutorService} Javadoc recommendation.
     *
     * @param executorService executor service we wish to shut down.
     */
    private static void shutdown(ExecutorService executorService) {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    System.err.println("Thread pool did not terminate");
                }
            }
        } catch (InterruptedException ie) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
