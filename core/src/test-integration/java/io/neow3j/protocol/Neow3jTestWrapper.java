package io.neow3j.protocol;

import static io.neow3j.protocol.IntegrationTestHelper.NEO_HASH;
import static io.neow3j.protocol.IntegrationTestHelper.NEO_TOTAL_SUPPLY;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.notNullValue;

import io.neow3j.protocol.core.JsonRpc2_0Neow3j;
import io.neow3j.protocol.core.methods.response.NeoGetTransactionHeight;
import io.neow3j.protocol.core.methods.response.NeoGetWalletBalance;
import io.neow3j.protocol.core.methods.response.NeoSendToAddress;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import org.hamcrest.Matcher;

public class Neow3jTestWrapper extends JsonRpc2_0Neow3j {


    public Neow3jTestWrapper(Neow3jService neow3jService) {
        super(neow3jService);
    }

    void waitUntilWalletHasBalanceGreaterThanOrEqualToOne() {
        waitUntilWalletHasBalance(greaterThanOrEqualTo(BigDecimal.ONE));
    }

    private void waitUntilWalletHasBalance(Matcher matcher) {
        waitUntil(callableGetBalance(), matcher);
    }

    protected void waitUntilTxHash(String txHash) {
        waitUntil(callableGetTxHash(txHash), notNullValue());
    }

    // as soon as the transaction txHash appears in a block, the according block number is
    // returned
    private Callable<Integer> callableGetTxHash(String txHash) {
        return () -> {
            try {
                NeoGetTransactionHeight tx = super.getTransactionHeight(txHash).send();
                if (tx.hasError()) {
                    return null;
                }
                return tx.getHeight().intValue();
            } catch (IOException e) {
                return null;
            }
        };
    }

    private void waitUntil(Callable<?> callable, Matcher matcher) {
        await().timeout(30, TimeUnit.SECONDS).until(callable, matcher);
    }

    private Callable<BigDecimal> callableGetBalance() {
        return () -> {
            try {
                NeoGetWalletBalance getWalletBalance = super.getWalletBalance(NEO_HASH).send();
                String balance = getWalletBalance.getWalletBalance().getBalance();
                return new BigDecimal(balance);
            } catch (IOException e) {
                return BigDecimal.ZERO;
            }
        };
    }

    String performNeoTransfer(String toAddress, String amount) throws IOException {
        NeoSendToAddress send = super.sendToAddress(NEO_HASH, toAddress, amount).send();
        // ensure that the transaction is sent
        waitUntilSendToAddressTransactionHasBeenExecuted();
        // store the transaction hash to use this transaction in the tests
        return send.getSendToAddress().getHash();
    }

    void waitUntilSendToAddressTransactionHasBeenExecuted() {
        waitUntilWalletHasBalance(lessThan(BigDecimal.valueOf(NEO_TOTAL_SUPPLY)));
    }
}
