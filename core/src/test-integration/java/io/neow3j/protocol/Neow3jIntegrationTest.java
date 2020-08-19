package io.neow3j.protocol;

import io.neow3j.contract.ContractParameter;
import io.neow3j.contract.ScriptHash;
import io.neow3j.protocol.core.methods.response.NeoGetWalletBalance;
import io.neow3j.protocol.core.methods.response.NeoGetTransactionHeight;
import io.neow3j.protocol.core.methods.response.NeoSendToAddress;
import io.neow3j.protocol.core.JsonRpc2_0Neow3j;
import io.neow3j.protocol.http.HttpService;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThan;

import org.junit.Before;
import org.hamcrest.Matcher;
import org.testcontainers.containers.GenericContainer;

abstract class Neow3jIntegrationTest {

    // Wallet password for node-config/wallet.json
    protected static final String NODE_WALLET_PASSWORD = "neo";
    protected static final String NEO_HASH = "de5f57d430d3dece511cf975a8d37848cb9e0525";
    protected static final String NEO_HASH_WITH_PREFIX = "0xde5f57d430d3dece511cf975a8d37848cb9e0525";
    protected static final String GAS_HASH_WITH_PREFIX = "0x668e0c1f9d7b70a99dd9e06eadd4c784d641afbc";
    protected static final int TOTAL_NEO_SUPPLY = 100000000;

    // The transaction information that is initially sent
    protected static String TX_HASH;
    protected static final String TX_AMOUNT = "2500";
    protected static final String TX_GAS_CONSUMED = "9007990";
    protected static final long TX_BLOCK_IDX = 2L;
    protected static final int TX_HASH_LENGTH_WITH_PREFIX = 66;
    protected static final int TX_VERSION = 0;
    protected static final String TX_SCRIPT = "AcQJDBTXhdxFuBA/Rv+5MO5//k7/XYa79wwUCJjqIZc3j2I6dnCXRFREhXbQrq8TwAwIdHJhbnNmZXIMFCUFnstIeNOodfkcUc7e0zDUV1/eQWJ9W1I4";
    protected static final String TX_SIGNER = "0x969a77db482f74ce27105f760efa139223431394";
    protected static final int RAW_TX_LENGTH = 496;
    protected static final String VM_STATE_HALT = "HALT";
    protected static final String KEY_TO_LOOKUP_AS_HEX = "14941343239213fa0e765f1027ce742f48db779a96";
    protected static final int STORAGE_LENGTH = 92;
    protected static final String APPLICATION_LOG_TRIGGER = "Application";

    // Invoke function variables
    protected static final String INVOKE_SYMBOL = "symbol";
    protected static final String INVOKE_BALANCE = "balanceOf";
    protected static final String INVOKE_TRANSFER = "transfer";

    // Invoke script
    protected static final String INVOKE_SCRIPT = "FQwUCJjqIZc3j2I6dnCXRFREhXbQrq8MFAiY6iGXN49iOnZwl0RURIV20K6vE8AMCHRyYW5zZmVyDBQlBZ7LSHjTqHX5HFHO3tMw1Fdf3kFifVtSOA==";

    // This is the last unspent transaction of address AK2nJJ... after a clean start of the container.
    protected static final String NEO3_PRIVATENET_CONTAINER_IMG = "docker.pkg.github.com" +
            "/axlabs/neo3-privatenet-docker/neo-cli-with-plugins:latest";

    // This is the port of one of the .NET nodes which is exposed internally by the container.
    protected static int EXPOSED_JSONRPC_PORT = 40332;
    protected static int BLOCK_HASH_LENGTH_WITH_PREFIX = 66;
    // First address held in wallet
    protected static final String ADDRESS_1 = "AGZLEiwUyCC4wiL5sRZA3LbxWPs9WrZeyN";
    protected static final String ADDR1_WIF = "L1WMhxazScMhUrdv34JqQb1HFSQmWeN2Kpc1R9JGKwL7CDNP21uR";
    protected static final ContractParameter ADDRESS_1_HASH160 =
            ContractParameter.hash160(ScriptHash.fromAddress(ADDRESS_1));
    protected static final String UNCLAIMED_GAS = "599985000";
    // Second address held in wallet
    protected static final String ADDRESS_2 = "AJunErzotcQTNWP2qktA7LgkXZVdHea97H";
    protected static final ContractParameter ADDRESS_2_HASH160 =
            ContractParameter.hash160(ScriptHash.fromAddress(ADDRESS_2));
    // Before the tests Neo is sent to this address to test GetTransaction method.
    protected static final String RECIPIENT_ADDRESS_1 = "AbRTHXb9zqdqn5sVh4EYpQHGZ536FgwCx2";
    protected static final ContractParameter RECIPIENT_ADDRESS_HASH160 =
            ContractParameter.hash160(ScriptHash.fromAddress(RECIPIENT_ADDRESS_1));
    protected static final String RECIPIENT_ADDRESS_2 = "AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y";
    // The address that is imported to the wallet
    protected static final String IMPORT_ADDRESS = "AaN1ksPFARY9dH73brS8AGoUBt86zqRDcQ";
    // The witness for the invokeFunction transfer
    protected static final String WITNESS = "afaed076854454449770763a628f379721ea9808";
    // The address from which Address 2 receives gas when sending Neo to the recipient address.
    protected static final String TX_GAS_ADDRESS = "AFmseVrdL9f9oyCzZefL9tG6UbvhPbdYzM";
    protected static final String TX_GAS_AMOUNT = "1200000000";

    protected static final int INVALID_PARAMS_CODE = -32602;
    protected static final String INVALID_PARAMS_MESSAGE = "Invalid params";

    protected static long BLOCK_0_IDX = 0;
    protected static String BLOCK_0_HASH = "0x78acf25f201970d882ed8e29480a8879b4379ea6b4ffc0c9797f971352377891";
    // block at index 1 changes at each setup
    protected static String BLOCK_0_HEADER_RAW_STRING =
            "0000000000000000000000000000000000000000000000000000000000000000000000002e0b8a0698b8fc0740928ba5b8bf7cd5a504b8ed7dcc21a4bcedac6ba19ba1e588ea19ef55010000000000000898ea2197378f623a7670974454448576d0aeaf0100011100";
    protected static Neow3jWrapper neow3jWrapper;

    protected static String getNodeUrl(GenericContainer container) {
        return "http://" + container.getContainerIpAddress() +
                ":" + container.getMappedPort(EXPOSED_JSONRPC_PORT);
    }

    @Before
    public void setUp() throws Exception {
        neow3jWrapper = new Neow3jWrapper(new HttpService(getNodeUrl(getPrivateNetContainer())));
        // open the wallet for JSON-RPC calls
        neow3jWrapper.openWallet();
        neow3jWrapper.listAddress().send().getAddresses().forEach(System.out::println);
        // ensure that the wallet with NEO/GAS is initialized for the tests
        neow3jWrapper.waitUntilWalletHasBalanceGreaterThanOrEqualToOne();
        // make a transaction that can be used for the tests
        neow3jWrapper.performSendToAddressTransaction();
    }

    protected abstract GenericContainer getPrivateNetContainer();

    protected static Neow3j getNeow3j() {
        return neow3jWrapper;
    }

    protected static class Neow3jWrapper extends JsonRpc2_0Neow3j {

        private Neow3jWrapper(Neow3jService neow3jService) {
            super(neow3jService);
        }

        private void openWallet() throws Exception {
            super.openWallet("wallet.json", NODE_WALLET_PASSWORD).send();
        }

        private void waitUntilWalletHasBalanceGreaterThanOrEqualToOne() {
            waitUntilWalletHasBalance(greaterThanOrEqualTo(BigDecimal.ONE));
        }

        private void waitUntilWalletHasBalance(Matcher matcher) {
            waitUntil(callableGetBalance(), matcher);
        }

        protected void waitUntilTxHash(String txHash) {
            waitUntil(callableGetTxHash(txHash), greaterThanOrEqualTo(1));
        }

        // as soon as the transaction txHash appears in a block, the according block number is returned
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

        private void performSendToAddressTransaction() throws IOException {
            NeoSendToAddress send = super.sendToAddress(NEO_HASH, RECIPIENT_ADDRESS_1, TX_AMOUNT).send();
            // ensure that the transaction is sent
            waitUntilSendToAddressTransactionHasBeenExecuted();
            // store the transaction hash to use this transaction in the tests
            setTxHash(send.getSendToAddress().getHash());
        }

        public void waitUntilSendToAddressTransactionHasBeenExecuted() {
            waitUntilWalletHasBalance(lessThan(BigDecimal.valueOf(TOTAL_NEO_SUPPLY)));
        }

        private void setTxHash(String txHash) {
            TX_HASH = txHash;
        }
    }

}
