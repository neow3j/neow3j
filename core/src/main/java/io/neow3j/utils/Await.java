package io.neow3j.utils;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;

import io.neow3j.contract.Hash160;
import io.neow3j.contract.Hash256;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.methods.response.NeoBlockCount;
import io.neow3j.protocol.core.methods.response.NeoGetContractState;
import io.neow3j.protocol.core.methods.response.NeoGetNep17Balances.Nep17Balance;
import io.neow3j.protocol.core.methods.response.NeoGetTransactionHeight;
import io.neow3j.protocol.core.methods.response.NeoGetWalletBalance;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

/**
 * Utility class used to wait for blockchain related events like the inclusion of a transaction in a
 * block or the deployment of a contract. The maximum time that these utility methods will wait for
 * a specific event is set in {@link Await#MAX_WAIT_TIME}. After that time, the methods will timeout
 * and throw an exception.
 */
public class Await {

    private final static int MAX_WAIT_TIME = 30;

    /**
     * Checks and waits until the token balance of the given address is greater than zero.
     *
     * @param address The account's address.
     * @param token   The script hash of the token to check the balance for.
     * @param neow3j  The {@code Neow3j} object to use to connect to a neo-node.
     */
    public static void waitUntilBalancesIsGreaterThanZero(String address,
            Hash160 token, Neow3j neow3j) {
        waitUntil(callableGetBalance(address, token, neow3j), Matchers.greaterThan(0L));
    }

    /**
     * Checks and waits until the block count (height) is greater than zero.
     *
     * @param neow3j  The {@code Neow3j} object to use to connect to a neo-node.
     */
    public static void waitUntilBlockCountIsGreaterThanZero(Neow3j neow3j) {
        waitUntil(callableGetBlockCount(neow3j), Matchers.greaterThan(BigInteger.ZERO));
    }

    /**
     * Checks and waits until the contract with the given script hash is seen on the blockchain.
     *
     * @param contract The contract's script hash.
     * @param neow3j   The {@code Neow3j} object to use to connect to a neo-node.
     */
    public static void waitUntilContractIsDeployed(Hash160 contract, Neow3j neow3j) {
        waitUntil(callableGetContractState(contract, neow3j), Matchers.is(true));
    }

    /**
     * Checks and waits until the transaction with the given hash is seen on the blockchain.
     *
     * @param txHash The transaction hash.
     * @param neow3j The {@code Neow3j} object to use to connect to a neo-node.
     */
    public static void waitUntilTransactionIsExecuted(Hash256 txHash, Neow3j neow3j) {
        waitUntil(callableGetTxHash(txHash, neow3j), notNullValue());
    }

    /**
     * Checks and waits until the wallet open on the neo-node has a {@code token}balance greater
     * or equal to {@code amount}.
     *
     * @param amount The amount to compare the balance to.
     * @param token  The token's script hash.
     * @param neow3j The {@code Neow3j} object to use to connect to a neo-node.
     */
    public static void waitUntilOpenWalletHasBalanceGreaterThanOrEqualTo(String amount,
            Hash160 token, Neow3j neow3j) {

        waitUntil(callableGetBalance(token, neow3j), greaterThanOrEqualTo(new BigDecimal(amount)));
    }

    /**
     * Waits until the {@code callable} function returns a condition that is satisfied by the
     * {@code matcher}. Uses the default {@link Await#MAX_WAIT_TIME} and {@link TimeUnit#SECONDS}.
     *
     * @param callable The function to be called.
     * @param matcher  The condition to be evaluated.
     */
    public static <T> void waitUntil(Callable<T> callable, Matcher<? super T> matcher) {
        waitUntil(callable, matcher, MAX_WAIT_TIME, TimeUnit.SECONDS);
    }

    /**
     * Waits until the {@code callable} function returns a condition that is satisfied by the
     * {@code matcher}.
     *
     * @param callable      The function to be called.
     * @param matcher       The condition to be evaluated.
     * @param maxWaitTime   The maximum amount of time to wait.
     * @param unit          The time unit for the {@code maxWaitTime} param.
     */
    public static <T> void waitUntil(Callable<T> callable, Matcher<? super T> matcher,
            int maxWaitTime, TimeUnit unit) {
        await().timeout(maxWaitTime, unit).until(callable, matcher);
    }

    private static Callable<Boolean> callableGetContractState(Hash160 contractHash160,
            Neow3j neow3j) {
        return () -> {
            try {
                NeoGetContractState response =
                        neow3j.getContractState(contractHash160).send();
                if (response.hasError()) {
                    return false;
                }
                return response.getContractState().getHash().equals(contractHash160);
            } catch (IOException e) {
                return false;
            }
        };
    }

    private static Callable<BigInteger> callableGetBlockCount(Neow3j neow3j) {
        return () -> {
            try {
                NeoBlockCount getBlockCount = neow3j.getBlockCount().send();
                return getBlockCount.getBlockIndex();
            } catch (IOException e) {
                return BigInteger.ZERO;
            }
        };
    }

    private static Callable<Long> callableGetBalance(String address, Hash160 tokenHash160,
            Neow3j neow3j) {
        return () -> {
            try {
                List<Nep17Balance> balances = neow3j.getNep17Balances(address).send()
                        .getBalances().getBalances();
                return balances.stream()
                        .filter(b -> b.getAssetHash().equals(tokenHash160))
                        .findFirst()
                        .map(b -> Long.valueOf(b.getAmount()))
                        .orElse(0L);
            } catch (IOException e) {
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
            } catch (IOException e) {
                return null;
            }
        };
    }

    private static Callable<BigDecimal> callableGetBalance(Hash160 token, Neow3j neow3j) {
        return () -> {
            try {
                NeoGetWalletBalance response = neow3j.getWalletBalance(token.toString()).send();
                String balance = response.getWalletBalance().getBalance();
                return new BigDecimal(balance);
            } catch (IOException e) {
                return BigDecimal.ZERO;
            }
        };
    }

}
