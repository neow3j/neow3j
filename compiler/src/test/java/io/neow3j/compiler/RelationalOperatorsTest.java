package io.neow3j.compiler;

import static io.neow3j.protocol.ObjectMapperFactory.getObjectMapper;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.JavaType;
import io.neow3j.compiler.Compiler.CompilationResult;
import io.neow3j.contract.ContractParameter;
import io.neow3j.contract.GasToken;
import io.neow3j.contract.NeoToken;
import io.neow3j.contract.ScriptHash;
import io.neow3j.contract.SmartContract;
import io.neow3j.model.NeoConfig;
import io.neow3j.protocol.Neow3jService;
import io.neow3j.protocol.ObjectMapperFactory;
import io.neow3j.protocol.core.JsonRpc2_0Neow3j;
import io.neow3j.protocol.core.methods.response.ArrayStackItem;
import io.neow3j.protocol.core.methods.response.NeoGetContractState;
import io.neow3j.protocol.core.methods.response.NeoGetNep5Balances.Nep5Balance;
import io.neow3j.protocol.core.methods.response.NeoGetTransactionHeight;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.protocol.core.methods.response.NeoSendRawTransaction;
import io.neow3j.protocol.core.methods.response.NeoSendToAddress;
import io.neow3j.protocol.core.methods.response.StackItem;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.transaction.Signer;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.Wallet;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.utility.MountableFile;

public class RelationalOperatorsTest {

    private static final String NODE_WALLET_FILE = "wallet.json";
    private static final String NODE_WALLET_PASSWORD = "neo";

    private static final String NEO3_PRIVATENET_CONTAINER_IMG =
            "docker.pkg.github.com/axlabs/neo3-privatenet-docker/neo-cli-with-plugins:latest";

    // Exposed port of the neo node running in the docker container.
    private static int EXPOSED_JSONRPC_PORT = 40332;
    private static String CONTRACT_NAME = "RelationalOperators";

    private static final ScriptHash NEO_SCRIPT_HASH = NeoToken.SCRIPT_HASH;
    private static final ScriptHash GAS_SCRIPT_HASH = GasToken.SCRIPT_HASH;
    private static Account acc1;
    private static Account multiSigAcc;
    private static Wallet wallet;

    private static Neow3jWrapper neow3jWrapper;

    private static SmartContract relationalOperatorsContract;

    private static String getNodeUrl(GenericContainer container) {
        return "http://" + container.getContainerIpAddress() +
                ":" + container.getMappedPort(EXPOSED_JSONRPC_PORT);
    }

    @BeforeClass
    public static void setUp() throws Exception {
        NeoConfig.setMagicNumber(new byte[]{0x01, 0x03, 0x00, 0x0}); // Magic number 769
        acc1 = Account.fromWIF("L1WMhxazScMhUrdv34JqQb1HFSQmWeN2Kpc1R9JGKwL7CDNP21uR");
        multiSigAcc = Account.createMultiSigAccount(
                Arrays.asList(acc1.getECKeyPair().getPublicKey()), 1);
        wallet = Wallet.withAccounts(acc1, multiSigAcc);

        neow3jWrapper = new Neow3jWrapper(new HttpService(getNodeUrl(privateNetContainer)));
        neow3jWrapper.openWallet();

        relationalOperatorsContract = deployContract("io.neow3j.compiler.contracts." + CONTRACT_NAME);
        neow3jWrapper.waitUntilContractIsDeployed(relationalOperatorsContract.getScriptHash());
    }

    private static SmartContract deployContract(String fullyQualifiedName) throws IOException {
        CompilationResult res = new Compiler().compileClass(fullyQualifiedName);
        SmartContract sc = new SmartContract(res.getNef(), res.getManifest(), neow3jWrapper);
        NeoSendRawTransaction response = sc.deploy()
                .withWallet(wallet)
                .withSigners(Signer.calledByEntry(multiSigAcc.getScriptHash()))
                .build().sign().send();
        if (response.hasError()) {
            throw new RuntimeException(response.getError().getMessage());
        }
        return sc;
    }

    @ClassRule
    public static GenericContainer privateNetContainer = new GenericContainer(
            NEO3_PRIVATENET_CONTAINER_IMG)
            .withClasspathResourceMapping("/node-config/config.json",
                    "/neo-cli/config.json", BindMode.READ_ONLY)
            .withClasspathResourceMapping("/node-config/protocol.json",
                    "/neo-cli/protocol.json", BindMode.READ_ONLY)
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("/node-config/wallet.json", 777),
                    "/neo-cli/wallet.json")
            .withClasspathResourceMapping("/node-config/rpcserver.config.json",
                    "/neo-cli/Plugins/RpcServer/config.json", BindMode.READ_ONLY)
            .withExposedPorts(EXPOSED_JSONRPC_PORT)
            .waitingFor(Wait.forListeningPort());

    private static String getResultFilePath(String testClassName, String methodName) {
        return "responses/" + testClassName + "/" + methodName + ".json";
    }

    @Test
    public void testIntegerRelationalOperators() throws IOException, InterruptedException {
        String methodName = "integerRelationalOperators";
        NeoInvokeFunction response = relationalOperatorsContract.invokeFunction(
                methodName,
                ContractParameter.integer(1),
                ContractParameter.integer(0));
        InputStream s = this.getClass().getClassLoader()
                .getResourceAsStream(getResultFilePath(CONTRACT_NAME, methodName));
        ArrayStackItem expected = getObjectMapper().readValue(s, ArrayStackItem.class);

        assertThat(response.getInvocationResult().getStack().get(0), is(expected));
    }

    protected static class Neow3jWrapper extends JsonRpc2_0Neow3j {

        private Neow3jWrapper(Neow3jService neow3jService) {
            super(neow3jService);
        }

        private void openWallet() throws Exception {
            super.openWallet(NODE_WALLET_FILE, NODE_WALLET_PASSWORD).send();
        }

        protected void waitUntilTxHash(String txHash) {
            waitUntil(callableGetTxHash(txHash), greaterThanOrEqualTo(1L));
        }

        // as soon as the transaction txHash appears in a block, the according block number is
        // returned
        private Callable<Long> callableGetTxHash(String txHash) {
            return () -> {
                try {
                    NeoGetTransactionHeight tx = super.getTransactionHeight(txHash).send();
                    if (tx.hasError()) {
                        return null;
                    }
                    return tx.getHeight().longValue();
                } catch (IOException e) {
                    return null;
                }
            };
        }

        private <T> void waitUntil(Callable<T> callable, Matcher<? super T> matcher) {
            await().timeout(30, TimeUnit.SECONDS).until(callable, matcher);
        }

        private Callable<Long> callableGetBalance(ScriptHash tokenScriptHash) {
            return () -> {
                try {
                    List<Nep5Balance> balances = super.getNep5Balances(acc1.getAddress()).send()
                            .getBalances().getBalances();
                    return balances.stream()
                            .filter(b -> b.getAssetHash().equals("0x" + tokenScriptHash.toString()))
                            .findFirst()
                            .map(b -> Long.valueOf(b.getAmount()))
                            .orElse(0L);
                } catch (IOException e) {
                    return 0L;
                }
            };
        }

        private Callable<Boolean> callableGetContractState(ScriptHash contractScriptHash) {
            return () -> {
                try {
                    NeoGetContractState response = getContractState(contractScriptHash.toString())
                            .send();
                    if (response.hasError()) {
                        return false;
                    }
                    if (response.getContractState().getHash().equals("0x" +
                            contractScriptHash.toString())) {
                        return true;
                    }
                    return false;
                } catch (IOException e) {
                    return false;
                }
            };
        }

        private void transferTokensToAddress(ScriptHash tokenScriptHash, String address,
                long amount) throws IOException {

            NeoSendToAddress r = super.sendToAddress(tokenScriptHash.toString(), address,
                    String.valueOf(amount)).send();
            // ensure that the transaction is sent
            waitUntilBalancesIsGreaterThanZero(tokenScriptHash);
        }

        public void waitUntilBalancesIsGreaterThanZero(ScriptHash tokenScriptHash) {
            waitUntil(callableGetBalance(tokenScriptHash), Matchers.greaterThan(0L));
        }

        public void waitUntilContractIsDeployed(ScriptHash contractScripHash) {
            waitUntil(callableGetContractState(contractScripHash), Matchers.is(true));
        }

    }
}
