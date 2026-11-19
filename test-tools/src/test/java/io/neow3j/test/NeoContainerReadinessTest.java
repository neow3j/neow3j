package io.neow3j.test;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.testcontainers.containers.ContainerLaunchException;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.containers.wait.strategy.WaitStrategyTarget;

import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NeoContainerReadinessTest {

    private static final String RPC_ERROR = "{\"jsonrpc\":\"2.0\",\"id\":1,\"error\":{\"code\":-2146232969,"
            + "\"message\":\"The given key was not present in the dictionary.\"}}";
    private static final String GENESIS_BLOCK_COUNT = "{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":1}";

    @RegisterExtension
    static final WireMockExtension rpc = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort()).build();

    @Test
    void resumeWaitsUntilBlockchainIsInitialized() throws Exception {
        rpc.stubFor(post(urlEqualTo("/")).withRequestBody(matchingJsonPath("$.method", equalTo("getblockcount")))
                .inScenario("startup").whenScenarioStateIs(STARTED)
                .willReturn(aResponse().withBody(RPC_ERROR))
                .willSetStateTo("initialized"));
        rpc.stubFor(post(urlEqualTo("/")).withRequestBody(matchingJsonPath("$.method", equalTo("getblockcount")))
                .inScenario("startup").whenScenarioStateIs("initialized")
                .willReturn(aResponse().withBody(GENESIS_BLOCK_COUNT)));

        NeoExpressTestContainer container = spy(new NeoExpressTestContainer());
        doReturn("Neo-express started.").when(container).execCommand(any(String[].class));
        doReturn("127.0.0.1").when(container).getHost();
        doReturn(rpc.getPort()).when(container).getMappedPort(NeoExpressTestContainer.EXPOSED_JSONRPC_PORT);
        container.withStartupTimeout(Duration.ofSeconds(2));

        assertThat(container.resume(), is("Neo-express started."));

        rpc.verify(2, postRequestedFor(urlEqualTo("/"))
                .withRequestBody(matchingJsonPath("$.method", equalTo("getblockcount"))));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void defaultContainerStrategyWaitsForRpcReadiness(boolean express) {
        rpc.stubFor(post(urlEqualTo("/")).inScenario("startup").whenScenarioStateIs(STARTED)
                .willReturn(aResponse().withBody(RPC_ERROR)).willSetStateTo("initialized"));
        rpc.stubFor(post(urlEqualTo("/")).inScenario("startup").whenScenarioStateIs("initialized")
                .willReturn(aResponse().withBody(GENESIS_BLOCK_COUNT)));

        containerStrategy(express, Duration.ofSeconds(2)).waitUntilReady(rpcTarget());

        rpc.verify(2, postRequestedFor(urlEqualTo("/"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(matchingJsonPath("$.method", equalTo("getblockcount"))));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"result\":0}", "{\"result\":-1}", "{\"result\":\"1\"}", "{\"result\":1.5}",
            "{}", "{\"result\":null}", "null", "not JSON", "{\"result\":1,\"error\":{\"code\":-1}}"
    })
    void retriesResponsesThatDoNotEstablishReadiness(String response) {
        rpc.stubFor(post(urlEqualTo("/")).inScenario("startup").whenScenarioStateIs(STARTED)
                .willReturn(aResponse().withBody(response)).willSetStateTo("initialized"));
        rpc.stubFor(post(urlEqualTo("/")).inScenario("startup").whenScenarioStateIs("initialized")
                .willReturn(aResponse().withBody(GENESIS_BLOCK_COUNT)));

        containerStrategy(false, Duration.ofSeconds(2)).waitUntilReady(rpcTarget());

        rpc.verify(2, postRequestedFor(urlEqualTo("/")));
    }

    @Test
    void acceptsExistingCheckpointWithoutWaitingForAnotherBlock() {
        rpc.stubFor(post(urlEqualTo("/")).willReturn(aResponse().withBody("{\"result\":12345}")));

        containerStrategy(true, Duration.ofSeconds(2)).waitUntilReady(rpcTarget());

        rpc.verify(1, postRequestedFor(urlEqualTo("/")));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void honorsConfiguredStartupTimeoutAndPreservesLastRpcError(boolean express) {
        rpc.stubFor(post(urlEqualTo("/")).willReturn(aResponse().withBody(RPC_ERROR)));
        WaitStrategy strategy = containerStrategy(express, Duration.ofMillis(250));
        long startNanos = System.nanoTime();

        ContainerLaunchException e = assertThrows(ContainerLaunchException.class,
                () -> strategy.waitUntilReady(rpcTarget()));

        assertThat(Duration.ofNanos(System.nanoTime() - startNanos).toMillis(), lessThan(2000L));
        assertThat(e.getMessage(), containsString("positive block count"));
        assertThat(e.getCause().getMessage(), containsString("-2146232969"));
        assertThat(e.getCause().getMessage(), containsString("The given key was not present in the dictionary."));
    }

    @Test
    void boundsRequestsToAnUnresponsiveRpcServer() {
        rpc.stubFor(post(urlEqualTo("/")).willReturn(aResponse().withBody(GENESIS_BLOCK_COUNT)
                .withFixedDelay(3000)));
        WaitStrategy strategy = containerStrategy(false, Duration.ofMillis(200));
        long startNanos = System.nanoTime();

        assertThrows(ContainerLaunchException.class, () -> strategy.waitUntilReady(rpcTarget()));

        assertThat(Duration.ofNanos(System.nanoTime() - startNanos).toMillis(), lessThan(2000L));
    }

    @Test
    void boundsRequestsWhenTheResponseArrivesSlowly() {
        rpc.stubFor(post(urlEqualTo("/")).willReturn(aResponse().withBody(GENESIS_BLOCK_COUNT)
                .withChunkedDribbleDelay(30, 3000)));
        WaitStrategy strategy = containerStrategy(false, Duration.ofMillis(250));
        long startNanos = System.nanoTime();

        assertThrows(ContainerLaunchException.class, () -> strategy.waitUntilReady(rpcTarget()));

        assertThat(Duration.ofNanos(System.nanoTime() - startNanos).toMillis(), lessThan(2000L));
    }

    @Test
    void retriesHttpFailuresDuringStartup() {
        rpc.stubFor(post(urlEqualTo("/")).inScenario("startup").whenScenarioStateIs(STARTED)
                .willReturn(aResponse().withStatus(503)).willSetStateTo("initialized"));
        rpc.stubFor(post(urlEqualTo("/")).inScenario("startup").whenScenarioStateIs("initialized")
                .willReturn(aResponse().withBody(GENESIS_BLOCK_COUNT)));

        containerStrategy(false, Duration.ofSeconds(2)).waitUntilReady(rpcTarget());

        rpc.verify(2, postRequestedFor(urlEqualTo("/")));
    }

    @Test
    void startupStrategiesCanBeReplaced() {
        WaitStrategy customStrategy = mock(WaitStrategy.class);
        InspectableNeoTestContainer neo = new InspectableNeoTestContainer();
        InspectableNeoExpressTestContainer express = new InspectableNeoExpressTestContainer();

        neo.waitingFor(customStrategy);
        express.waitingFor(customStrategy);

        assertSame(customStrategy, neo.strategy());
        assertSame(customStrategy, express.strategy());
    }

    @Test
    void resumeUsesCustomWaitStrategyAndRetainsBlockTime() throws Exception {
        NeoExpressTestContainer container = spy(new NeoExpressTestContainer());
        container.withSecondsPerBlock(5);
        doReturn("Neo-express started.").when(container).execCommand(any(String[].class));
        WaitStrategy customStrategy = mock(WaitStrategy.class);
        container.waitingFor(customStrategy);

        assertThat(container.resume(), is("Neo-express started."));

        verify(container).execCommand("/neoxp/neoxp-run.sh", "5");
        verify(customStrategy).waitUntilReady(container);
        rpc.verify(0, postRequestedFor(urlEqualTo("/")));
    }

    private static WaitStrategy containerStrategy(boolean express, Duration timeout) {
        if (express) {
            InspectableNeoExpressTestContainer container = new InspectableNeoExpressTestContainer();
            container.withStartupTimeout(timeout);
            return container.strategy();
        }
        InspectableNeoTestContainer container = new InspectableNeoTestContainer();
        container.withStartupTimeout(timeout);
        return container.strategy();
    }

    private static WaitStrategyTarget rpcTarget() {
        WaitStrategyTarget target = mock(WaitStrategyTarget.class);
        when(target.getHost()).thenReturn("127.0.0.1");
        when(target.getMappedPort(NeoTestContainer.EXPOSED_JSONRPC_PORT)).thenReturn(rpc.getPort());
        return target;
    }

    private static class InspectableNeoTestContainer extends NeoTestContainer {

        WaitStrategy strategy() {
            return getWaitStrategy();
        }

    }

    private static class InspectableNeoExpressTestContainer extends NeoExpressTestContainer {

        WaitStrategy strategy() {
            return getWaitStrategy();
        }

    }

}
