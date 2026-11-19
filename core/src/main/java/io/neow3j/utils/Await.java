package io.neow3j.utils;

import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.Response.Error;
import io.neow3j.protocol.core.response.ContractState;
import io.neow3j.protocol.core.response.NeoGetContractState;
import io.neow3j.protocol.core.response.NeoGetNep17Balances.Nep17Balance;
import io.neow3j.protocol.core.response.NeoGetTransactionHeight;
import io.neow3j.protocol.exceptions.RpcResponseErrorException;
import io.neow3j.types.Hash160;
import io.neow3j.types.Hash256;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Utility class used to wait for blockchain related events like the inclusion of a transaction in a block or the
 * deployment of a contract. The maximum time that these utility methods will wait for a specific event is set in
 * {@link Await#MAX_WAIT_TIME_SECONDS}. After that time, the methods will timeout and throw an exception. Communication
 * failures and unexpected RPC errors end the wait and are passed to the caller. Recognized unknown transaction and
 * unknown contract responses are retried while waiting for inclusion or deployment.
 */
public class Await {

    private final static int MAX_WAIT_TIME_SECONDS = 60;

    private static final int LEGACY_NOT_FOUND = -100;

    private static final int UNKNOWN_CONTRACT = -102;

    private static final Duration TRANSACTION_POLL_INTERVAL = Duration.ofMillis(100);

    /**
     * Checks and waits until the token balance of the given script hash is greater than zero.
     *
     * @param scriptHash the account's script hash.
     * @param token      the script hash of the token to check the balance for.
     * @param neow3j     the {@code Neow3j} object to use to connect to a neo-node.
     * @throws IOException               if communication with the node fails.
     * @throws RpcResponseErrorException if the node returns an unexpected RPC error.
     */
    public static void waitUntilBalancesIsGreaterThanZero(Hash160 scriptHash, Hash160 token, Neow3j neow3j)
            throws IOException {
        waitForRpcCondition(callableGetBalance(scriptHash, token, neow3j), greaterThan(0L));
    }

    /**
     * Checks and waits until the block count (height) is greater than {@code blockCount}.
     *
     * @param neow3j     the {@code Neow3j} object to use to connect to a neo-node.
     * @param blockCount the block count (height) that is waited for.
     * @throws IOException               if communication with the node fails.
     * @throws RpcResponseErrorException if the node returns an unexpected RPC error.
     */
    public static void waitUntilBlockCountIsGreaterThan(Neow3j neow3j, BigInteger blockCount) throws IOException {
        waitForRpcCondition(callableGetBlockCount(neow3j), greaterThan(blockCount));
    }

    /**
     * Checks and waits until the block count (height) is greater than zero.
     *
     * @param neow3j the {@code Neow3j} object to use to connect to a neo-node.
     * @throws IOException               if communication with the node fails.
     * @throws RpcResponseErrorException if the node returns an unexpected RPC error.
     */
    public static void waitUntilBlockCountIsGreaterThanZero(Neow3j neow3j) throws IOException {
        waitUntilBlockCountIsGreaterThan(neow3j, BigInteger.ZERO);
    }

    /**
     * Checks and waits until the contract with the given script hash is seen on the blockchain.
     *
     * @param contract the contract's script hash.
     * @param neow3j   the {@code Neow3j} object to use to connect to a neo-node.
     * @throws IOException               if communication with the node fails.
     * @throws RpcResponseErrorException if the node returns an unexpected RPC error.
     */
    public static void waitUntilContractIsDeployed(Hash160 contract, Neow3j neow3j) throws IOException {
        waitForRpcCondition(callableGetContractState(contract, neow3j), Matchers.is(true));
    }

    /**
     * Checks and waits until the transaction with the given hash is seen on the blockchain.
     *
     * @param txHash the transaction hash.
     * @param neow3j the {@code Neow3j} object to use to connect to a neo-node.
     * @throws IOException               if communication with the node fails.
     * @throws RpcResponseErrorException if the node returns an unexpected RPC error.
     */
    public static void waitUntilTransactionIsExecuted(Hash256 txHash, Neow3j neow3j) throws IOException {
        waitForRpcCondition(callableGetTransactionHeight(txHash, neow3j), notNullValue());
    }

    /**
     * Checks and waits until the transaction with the given hash is seen on the blockchain.
     *
     * @param txHash  the transaction hash.
     * @param neow3j  the {@code Neow3j} object to use to connect to a neo-node.
     * @param timeout the maximum time to wait.
     * @throws IOException               if communication with the node fails.
     * @throws RpcResponseErrorException if the node returns an unexpected RPC error.
     */
    public static void waitUntilTransactionIsExecuted(Hash256 txHash, Neow3j neow3j, Duration timeout)
            throws IOException {
        if (timeout == null || timeout.isZero() || timeout.isNegative()) {
            throw new IllegalArgumentException("The timeout must be a positive duration.");
        }
        long startNanos = System.nanoTime();
        await().ignoreNoExceptions().pollDelay(Duration.ZERO).pollInterval((pollCount, previousInterval) -> {
            // Awaitility sleeps for the full interval before checking the timeout again.
            Duration remaining = timeout.minusNanos(System.nanoTime() - startNanos);
            if (remaining.isNegative()) {
                return Duration.ZERO;
            }
            return remaining.compareTo(TRANSACTION_POLL_INTERVAL) < 0 ? remaining : TRANSACTION_POLL_INTERVAL;
        }).atMost(timeout).until(callableGetTransactionHeight(txHash, neow3j), notNullValue());
    }

    /**
     * Checks and waits until the wallet open on the neo-node has a {@code token}balance greater or equal to {@code
     * amount}.
     *
     * @param amount the amount to compare the balance to.
     * @param token  the token's script hash.
     * @param neow3j the {@code Neow3j} object to use to connect to a neo-node.
     * @throws IOException               if communication with the node fails.
     * @throws RpcResponseErrorException if the node returns an unexpected RPC error.
     */
    public static void waitUntilOpenWalletHasBalanceGreaterThanOrEqualTo(String amount, Hash160 token, Neow3j neow3j)
            throws IOException {
        waitForRpcCondition(callableGetBalance(token, neow3j), greaterThanOrEqualTo(new BigDecimal(amount)));
    }

    /**
     * Waits until the {@code callable} function returns a condition that is satisfied by the {@code matcher}. Uses
     * the default {@link Await#MAX_WAIT_TIME_SECONDS} in seconds.
     * Exceptions from the callable follow Awaitility's configured exception handling, including checked exceptions.
     *
     * @param callable the function to be called.
     * @param matcher  the condition to be evaluated.
     * @param <T>      the type that the callable function and condition should be compatible with.
     */
    public static <T> void waitUntil(Callable<T> callable, Matcher<? super T> matcher) {
        waitUntil(callable, matcher, MAX_WAIT_TIME_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * Waits until the {@code callable} function returns a condition that is satisfied by the {@code matcher}.
     * Exceptions from the callable follow Awaitility's configured exception handling, including checked exceptions.
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

    /**
     * Waits for an RPC result while propagating failures even if global Awaitility settings ignore exceptions.
     * Expected missing-resource responses are handled by the callable as an unsatisfied condition.
     *
     * @param callable the RPC lookup.
     * @param matcher  the condition to be evaluated.
     * @param <T>      the type of the RPC result.
     * @throws IOException if communication with the node fails.
     */
    private static <T> void waitForRpcCondition(Callable<T> callable, Matcher<? super T> matcher) throws IOException {
        await().ignoreNoExceptions().timeout(MAX_WAIT_TIME_SECONDS, TimeUnit.SECONDS).until(callable, matcher);
    }

    private static Callable<Boolean> callableGetContractState(Hash160 contractHash, Neow3j neow3j) {
        return () -> {
            NeoGetContractState response = neow3j.getContractState(contractHash).send();
            Error error = response.getError();
            if (error != null && (error.getCode() == UNKNOWN_CONTRACT
                    || (error.getCode() == LEGACY_NOT_FOUND && "Unknown contract".equals(error.getMessage())))) {
                return false;
            }
            ContractState contract = response.getContractState();
            return contract != null && contractHash.equals(contract.getHash());
        };
    }

    private static Callable<BigInteger> callableGetBlockCount(Neow3j neow3j) {
        return () -> neow3j.getBlockCount().send().getBlockCount();
    }

    private static Callable<Long> callableGetBalance(Hash160 scriptHash, Hash160 tokenHash, Neow3j neow3j) {
        return () -> {
            List<Nep17Balance> balances = neow3j.getNep17Balances(scriptHash).send().getBalances().getBalances();
            return balances.stream()
                    .filter(b -> b.getAssetHash().equals(tokenHash))
                    .findFirst()
                    .map(b -> Long.valueOf(b.getAmount()))
                    .orElse(0L);
        };
    }

    private static Callable<BigInteger> callableGetTransactionHeight(Hash256 txHash, Neow3j neow3j) {
        return () -> {
            NeoGetTransactionHeight response = neow3j.getTransactionHeight(txHash).send();
            return response.reportsUnknownTransaction() ? null : response.getHeight();
        };
    }

    private static Callable<BigDecimal> callableGetBalance(Hash160 tokenHash, Neow3j neow3j) {
        return () -> new BigDecimal(neow3j.getWalletBalance(tokenHash).send().getWalletBalance().getBalance());
    }

}
