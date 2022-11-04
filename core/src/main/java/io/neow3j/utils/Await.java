package io.neow3j.utils;

import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.response.NeoBlockCount;
import io.neow3j.protocol.core.response.NeoGetContractState;
import io.neow3j.protocol.core.response.NeoGetNep17Balances.Nep17Balance;
import io.neow3j.protocol.core.response.NeoGetTransactionHeight;
import io.neow3j.protocol.core.response.NeoGetWalletBalance;
import io.neow3j.protocol.exceptions.RpcResponseErrorException;
import io.neow3j.types.Hash160;
import io.neow3j.types.Hash256;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
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
 * {@link Await#MAX_WAIT_TIME}. After that time, the methods will timeout and throw an exception.
 */
public class Await {

    private final static int MAX_WAIT_TIME = 60;

    /**
     * Checks and waits until the token balance of the given script hash is greater than zero.
     *
     * @param scriptHash the account's script hash.
     * @param token      the script hash of the token to check the balance for.
     * @param neow3j     the {@code Neow3j} object to use to connect to a neo-node.
     */
    public static void waitUntilBalancesIsGreaterThanZero(Hash160 scriptHash, Hash160 token, Neow3j neow3j) {
        waitUntil(callableGetBalance(scriptHash, token, neow3j), Matchers.greaterThan(0L));
    }

    /**
     * Checks and waits until the block count (height) is greater than {@code blockCount}.
     *
     * @param neow3j     the {@code Neow3j} object to use to connect to a neo-node.
     * @param blockCount the block count (height) that is waited for.
     */
    public static void waitUntilBlockCountIsGreaterThan(Neow3j neow3j, BigInteger blockCount) {
        waitUntil(callableGetBlockCount(neow3j), greaterThan(blockCount));
    }

    /**
     * Checks and waits until the block count (height) is greater than zero.
     *
     * @param neow3j the {@code Neow3j} object to use to connect to a neo-node.
     */
    public static void waitUntilBlockCountIsGreaterThanZero(Neow3j neow3j) {
        waitUntilBlockCountIsGreaterThan(neow3j, BigInteger.ZERO);
    }

    /**
     * Checks and waits until the contract with the given script hash is seen on the blockchain.
     *
     * @param contract the contract's script hash.
     * @param neow3j   the {@code Neow3j} object to use to connect to a neo-node.
     */
    public static void waitUntilContractIsDeployed(Hash160 contract, Neow3j neow3j) {
        waitUntil(callableGetContractState(contract, neow3j), Matchers.is(true));
    }

    /**
     * Checks and waits until the transaction with the given hash is seen on the blockchain.
     *
     * @param txHash the transaction hash.
     * @param neow3j the {@code Neow3j} object to use to connect to a neo-node.
     */
    public static void waitUntilTransactionIsExecuted(Hash256 txHash, Neow3j neow3j) {
        waitUntil(callableGetTxHash(txHash, neow3j), notNullValue());
    }

    /**
     * Checks and waits until the wallet open on the neo-node has a {@code token}balance greater or equal to {@code
     * amount}.
     *
     * @param amount the amount to compare the balance to.
     * @param token  the token's script hash.
     * @param neow3j the {@code Neow3j} object to use to connect to a neo-node.
     */
    public static void waitUntilOpenWalletHasBalanceGreaterThanOrEqualTo(String amount, Hash160 token, Neow3j neow3j) {
        waitUntil(callableGetBalance(token, neow3j), greaterThanOrEqualTo(new BigDecimal(amount)));
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

    private static Callable<Boolean> callableGetContractState(Hash160 contractHash160, Neow3j neow3j) {
        return () -> {
            try {
                NeoGetContractState response = neow3j.getContractState(contractHash160).send();
                return response.getContractState().getHash().equals(contractHash160);
            } catch (IOException | RpcResponseErrorException e) {
                return false;
            }
        };
    }

    private static Callable<BigInteger> callableGetBlockCount(Neow3j neow3j) {
        return () -> {
            try {
                NeoBlockCount response = neow3j.getBlockCount().send();
                return response.getBlockCount();
            } catch (IOException | RpcResponseErrorException e) {
                return BigInteger.ZERO;
            }
        };
    }

    private static Callable<Long> callableGetBalance(Hash160 scriptHash, Hash160 tokenHash160, Neow3j neow3j) {
        return () -> {
            try {
                List<Nep17Balance> balances = neow3j.getNep17Balances(scriptHash).send().getBalances().getBalances();
                return balances.stream()
                        .filter(b -> b.getAssetHash().equals(tokenHash160))
                        .findFirst()
                        .map(b -> Long.valueOf(b.getAmount()))
                        .orElse(0L);
            } catch (IOException | RpcResponseErrorException e) {
                return 0L;
            }
        };
    }

    private static Callable<Long> callableGetTxHash(Hash256 txHash, Neow3j neow3j) {
        return () -> {
            try {
                NeoGetTransactionHeight tx = neow3j.getTransactionHeight(txHash).send();
                if (tx.hasError()) {
                    return null;
                }
                return tx.getHeight().longValue();
            } catch (IOException | RpcResponseErrorException e) {
                return null;
            }
        };
    }

    private static Callable<BigDecimal> callableGetBalance(Hash160 token, Neow3j neow3j) {
        return () -> {
            try {
                NeoGetWalletBalance response = neow3j.getWalletBalance(token).send();
                String balance = response.getWalletBalance().getBalance();
                return new BigDecimal(balance);
            } catch (IOException | RpcResponseErrorException e) {
                return BigDecimal.ZERO;
            }
        };
    }

}
