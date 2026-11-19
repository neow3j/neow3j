package io.neow3j.utils;

import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.Neow3jService;
import io.neow3j.protocol.core.JsonRpc2_0Neow3j;
import io.neow3j.protocol.core.Response.Error;
import io.neow3j.protocol.core.response.ContractState;
import io.neow3j.protocol.core.response.NeoBlockCount;
import io.neow3j.protocol.core.response.NeoGetContractState;
import io.neow3j.protocol.core.response.NeoGetNep17Balances;
import io.neow3j.protocol.core.response.NeoGetTransactionHeight;
import io.neow3j.protocol.core.response.NeoGetVersion.NeoVersion.Protocol;
import io.neow3j.protocol.core.response.NeoGetWalletBalance;
import io.neow3j.protocol.exceptions.RpcResponseErrorException;
import io.neow3j.types.Hash160;
import io.neow3j.types.Hash256;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.SQLException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.Callable;

import static io.neow3j.protocol.Neow3jConfig.defaultNeow3jConfig;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Isolated
class AwaitTest {

    private final Neow3jService service = mock(Neow3jService.class);

    private Neow3j neow3j;

    @BeforeEach
    void setUp() {
        Protocol protocol = new Protocol();
        protocol.setNetwork(768L);
        protocol.setMilliSecondsPerBlock(1000L);
        neow3j = new JsonRpc2_0Neow3j(service, defaultNeow3jConfig(), protocol);
    }

    @Test
    void pollingHelpersPropagateCommunicationFailures() throws IOException {
        IOException failure = new IOException("Connection closed");
        when(service.send(any(), any())).thenThrow(failure);
        Callable<Boolean> failingLookup = () -> {
            throw failure;
        };
        List<Executable> polls = asList(
                () -> Await.waitUntilBalancesIsGreaterThanZero(Hash160.ZERO, Hash160.ZERO, neow3j),
                () -> Await.waitUntilBlockCountIsGreaterThanZero(neow3j),
                () -> Await.waitUntilContractIsDeployed(Hash160.ZERO, neow3j),
                () -> Await.waitUntilTransactionIsExecuted(Hash256.ZERO, neow3j),
                () -> Await.waitUntilTransactionIsExecuted(Hash256.ZERO, neow3j, Duration.ofSeconds(2)),
                () -> Await.waitUntilOpenWalletHasBalanceGreaterThanOrEqualTo("1", Hash160.ZERO, neow3j),
                () -> Await.waitUntil(failingLookup, is(true)),
                () -> Await.waitUntil(failingLookup, is(true), 2, SECONDS));

        for (Executable poll : polls) {
            IOException thrown = assertThrows(IOException.class, poll);
            assertSame(failure, thrown);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    @Timeout(5)
    void rpcWaitsPropagateFailuresDespiteGlobalIgnoreSettings(boolean rpcError) throws IOException {
        Exception failure = rpcError
                ? new RpcResponseErrorException(new Error(-32601, "Method not found"))
                : new IOException("Connection closed");
        when(service.send(any(), any())).thenThrow(failure);
        List<Executable> polls = asList(
                () -> Await.waitUntilBalancesIsGreaterThanZero(Hash160.ZERO, Hash160.ZERO, neow3j),
                () -> Await.waitUntilBlockCountIsGreaterThanZero(neow3j),
                () -> Await.waitUntilContractIsDeployed(Hash160.ZERO, neow3j),
                () -> Await.waitUntilTransactionIsExecuted(Hash256.ZERO, neow3j),
                () -> Await.waitUntilTransactionIsExecuted(Hash256.ZERO, neow3j, Duration.ofSeconds(2)),
                () -> Await.waitUntilOpenWalletHasBalanceGreaterThanOrEqualTo("1", Hash160.ZERO, neow3j));

        Awaitility.ignoreExceptionByDefault(failure.getClass());
        try {
            for (Executable poll : polls) {
                assertSame(failure, assertThrows(failure.getClass(), poll));
            }
        } finally {
            Awaitility.ignoreExceptionsByDefaultMatching(e -> false);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void genericWaitsRespectGlobalExceptionHandling() throws Exception {
        SQLException failure = new SQLException("Query failed");
        Callable<Boolean> query = mock(Callable.class);
        when(query.call()).thenThrow(failure).thenReturn(true);
        Awaitility.ignoreExceptionByDefault(SQLException.class);
        try {
            Await.waitUntil(query, is(true), 2, SECONDS);

            verify(query, times(2)).call();
        } finally {
            Awaitility.ignoreExceptionsByDefaultMatching(e -> false);
        }
    }

    @Test
    void genericWaitsPropagateOtherCheckedExceptions() {
        SQLException failure = new SQLException("Query failed");
        Callable<Boolean> failingQuery = () -> {
            throw failure;
        };

        assertSame(failure, assertThrows(SQLException.class, () -> Await.waitUntil(failingQuery, is(true))));
        assertSame(failure, assertThrows(SQLException.class, () -> Await.waitUntil(failingQuery, is(true), 2, SECONDS)));
    }

    @Test
    void blockAndBalancePollingPropagateUnexpectedRpcErrors() throws IOException {
        Error error = new Error(-32601, "Method not found");
        NeoBlockCount blockCount = new NeoBlockCount();
        blockCount.setError(error);
        when(service.send(any(), eq(NeoBlockCount.class))).thenReturn(blockCount);
        NeoGetNep17Balances balances = new NeoGetNep17Balances();
        balances.setError(error);
        when(service.send(any(), eq(NeoGetNep17Balances.class))).thenReturn(balances);
        NeoGetWalletBalance walletBalance = new NeoGetWalletBalance();
        walletBalance.setError(error);
        when(service.send(any(), eq(NeoGetWalletBalance.class))).thenReturn(walletBalance);

        assertThrows(RpcResponseErrorException.class, () -> Await.waitUntilBlockCountIsGreaterThanZero(neow3j));
        assertThrows(RpcResponseErrorException.class,
                () -> Await.waitUntilBalancesIsGreaterThanZero(Hash160.ZERO, Hash160.ZERO, neow3j));
        assertThrows(RpcResponseErrorException.class,
                () -> Await.waitUntilOpenWalletHasBalanceGreaterThanOrEqualTo("1", Hash160.ZERO, neow3j));
    }

    @ParameterizedTest
    @ValueSource(ints = {-103, -100})
    void waitsForAnUnknownTransactionToBeIncluded(int errorCode) throws IOException {
        NeoGetTransactionHeight pending = new NeoGetTransactionHeight();
        pending.setError(new Error(errorCode, "Unknown transaction"));
        NeoGetTransactionHeight included = new NeoGetTransactionHeight();
        included.setResult(BigInteger.ONE);
        when(service.send(any(), eq(NeoGetTransactionHeight.class))).thenReturn(pending, included);

        Await.waitUntilTransactionIsExecuted(Hash256.ZERO, neow3j, Duration.ofSeconds(2));

        verify(service, times(2)).send(any(), eq(NeoGetTransactionHeight.class));
    }

    @ParameterizedTest
    @ValueSource(ints = {-32601, -100})
    void transactionPollingPropagatesUnexpectedRpcErrors(int errorCode) throws IOException {
        NeoGetTransactionHeight response = new NeoGetTransactionHeight();
        response.setError(new Error(errorCode, "Unexpected failure"));
        when(service.send(any(), eq(NeoGetTransactionHeight.class))).thenReturn(response);

        assertThrows(RpcResponseErrorException.class,
                () -> Await.waitUntilTransactionIsExecuted(Hash256.ZERO, neow3j, Duration.ofSeconds(2)));

        verify(service).send(any(), eq(NeoGetTransactionHeight.class));
    }

    @ParameterizedTest
    @ValueSource(ints = {-102, -100})
    void waitsForAnUnknownContractToBeDeployed(int errorCode) throws IOException {
        NeoGetContractState pending = new NeoGetContractState();
        pending.setError(new Error(errorCode, "Unknown contract"));
        ContractState contract = mock(ContractState.class);
        when(contract.getHash()).thenReturn(Hash160.ZERO);
        NeoGetContractState deployed = new NeoGetContractState();
        deployed.setResult(contract);
        when(service.send(any(), eq(NeoGetContractState.class))).thenReturn(pending, deployed);

        Await.waitUntilContractIsDeployed(Hash160.ZERO, neow3j);

        verify(service, times(2)).send(any(), eq(NeoGetContractState.class));
    }

    @Test
    void contractPollingPropagatesUnexpectedRpcErrors() throws IOException {
        NeoGetContractState response = new NeoGetContractState();
        response.setError(new Error(-32601, "Method not found"));
        when(service.send(any(), eq(NeoGetContractState.class))).thenReturn(response);

        assertThrows(RpcResponseErrorException.class,
                () -> Await.waitUntilContractIsDeployed(Hash160.ZERO, neow3j));

        verify(service).send(any(), eq(NeoGetContractState.class));
    }

}
