package io.neow3j.protocol.http;

import io.neow3j.protocol.core.Request;
import io.neow3j.protocol.core.methods.response.NeoBlockCount;
import io.neow3j.protocol.exceptions.ClientConnectionException;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;

public class HttpServiceTest {

    private static final Logger LOG = LoggerFactory.getLogger(HttpServiceTest.class);

    private HttpService httpService = new HttpService();

    @Test
    public void testAddHeader() {
        String headerName = "customized_header0";
        String headerValue = "customized_value0";
        httpService.addHeader(headerName, headerValue);
        assertTrue(httpService.getHeaders().get(headerName).equals(headerValue));
    }

    @Test
    public void testAddHeaders() {
        String headerName1 = "customized_header1";
        String headerValue1 = "customized_value1";

        String headerName2 = "customized_header2";
        String headerValue2 = "customized_value2";

        HashMap<String, String> headersToAdd = new HashMap<>();
        headersToAdd.put(headerName1, headerValue1);
        headersToAdd.put(headerName2, headerValue2);

        httpService.addHeaders(headersToAdd);

        assertTrue(httpService.getHeaders().get(headerName1).equals(headerValue1));
        assertTrue(httpService.getHeaders().get(headerName2).equals(headerValue2));
    }

    @Test
    public void httpException() throws IOException {
        String content = "400 error";
        Response response = new Response.Builder()
                .code(400)
                .message("")
                .body(ResponseBody.create(null, content))
                .request(new okhttp3.Request.Builder()
                        .url(HttpService.DEFAULT_URL)
                        .build())
                .protocol(Protocol.HTTP_1_1)
                .build();

        OkHttpClient httpClient = Mockito.mock(OkHttpClient.class);
        Mockito.when(httpClient.newCall(Mockito.any()))
                .thenAnswer(invocation -> {
                    Call call = Mockito.mock(Call.class);
                    Mockito.when(call.execute()).thenReturn(response);
                    return call;
                });
        HttpService mockedHttpService = new HttpService(httpClient);

        Request<String, NeoBlockCount> request = new Request<>(
                "getblockcount",
                Collections.emptyList(),
                mockedHttpService,
                NeoBlockCount.class);
        try {
            mockedHttpService.send(request, NeoBlockCount.class);
        } catch (ClientConnectionException e) {
            Assert.assertEquals(
                    e.getMessage(),
                    "Invalid response received: "
                            + response.code() + "; " + content);
            return;
        }

        Assert.fail("No exception");
    }

    @Test
    public void testAsyncWithExternalExecutor() throws ExecutionException, InterruptedException {

        TestExecutorService executor = new TestExecutorService();

        String content = "200";
        Response response = new Response.Builder()
                .code(200)
                .message("")
                .body(ResponseBody.create(null, content))
                .request(new okhttp3.Request.Builder()
                        .url(HttpService.DEFAULT_URL)
                        .build())
                .protocol(Protocol.HTTP_1_1)
                .build();


        OkHttpClient httpClient = Mockito.mock(OkHttpClient.class);
        Mockito.when(httpClient.newCall(Mockito.any()))
                .thenAnswer(invocation -> {
                    Call call = Mockito.mock(Call.class);
                    Mockito.when(call.execute()).thenReturn(response);
                    return call;
                });

        HttpService mockedHttpService1 = new HttpService(httpClient, executor);

        Request<String, NeoBlockCount> request1 = new Request<>(
                "getblockcount",
                Collections.emptyList(),
                mockedHttpService1,
                NeoBlockCount.class);

        mockedHttpService1.sendAsync(request1, NeoBlockCount.class);
        Assert.assertThat(executor.isCalled(), is(true));

        HttpService mockedHttpService2 = new HttpService(executor, false);

        Request<String, NeoBlockCount> request2 = new Request<>(
                "getblockcount",
                Collections.emptyList(),
                mockedHttpService2,
                NeoBlockCount.class);

        CompletableFuture<NeoBlockCount> result = mockedHttpService2.sendAsync(request2, NeoBlockCount.class);
        Assert.assertThat(executor.isCalled(), is(true));
    }

    private class TestExecutorService implements ExecutorService {

        private boolean isCalled = false;

        @Override
        public void shutdown() {

        }

        @NotNull
        @Override
        public List<Runnable> shutdownNow() {
            return null;
        }

        @Override
        public boolean isShutdown() {
            return false;
        }

        @Override
        public boolean isTerminated() {
            return false;
        }

        @Override
        public boolean awaitTermination(long timeout, @NotNull TimeUnit unit) throws InterruptedException {
            return false;
        }

        @NotNull
        @Override
        public <T> Future<T> submit(@NotNull Callable<T> task) {
            return null;
        }

        @NotNull
        @Override
        public <T> Future<T> submit(@NotNull Runnable task, T result) {
            return null;
        }

        @NotNull
        @Override
        public Future<?> submit(@NotNull Runnable task) {
            System.out.println("aaaaaaa");
            return null;
        }

        @NotNull
        @Override
        public <T> List<Future<T>> invokeAll(@NotNull Collection<? extends Callable<T>> tasks) throws InterruptedException {
            return null;
        }

        @NotNull
        @Override
        public <T> List<Future<T>> invokeAll(@NotNull Collection<? extends Callable<T>> tasks, long timeout, @NotNull TimeUnit unit) throws InterruptedException {
            return null;
        }

        @NotNull
        @Override
        public <T> T invokeAny(@NotNull Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
            return null;
        }

        @Override
        public <T> T invokeAny(@NotNull Collection<? extends Callable<T>> tasks, long timeout, @NotNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return null;
        }

        @Override
        public void execute(@NotNull Runnable command) {
            isCalled = true;
            LOG.info("execute() method called.");
        }

        public boolean isCalled() {
            return isCalled;
        }
    }


}
