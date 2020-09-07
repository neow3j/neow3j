package io.neow3j.compiler;

import static io.neow3j.protocol.ObjectMapperFactory.getObjectMapper;
import static org.awaitility.Awaitility.await;

import io.neow3j.compiler.Compiler.CompilationResult;
import io.neow3j.contract.GasToken;
import io.neow3j.contract.NeoToken;
import io.neow3j.contract.ScriptHash;
import io.neow3j.contract.SmartContract;
import io.neow3j.model.NeoConfig;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.methods.response.NeoGetContractState;
import io.neow3j.protocol.core.methods.response.NeoGetNep5Balances.Nep5Balance;
import io.neow3j.protocol.core.methods.response.NeoGetTransactionHeight;
import io.neow3j.protocol.core.methods.response.NeoSendRawTransaction;
import io.neow3j.protocol.core.methods.response.NeoSendToAddress;
import io.neow3j.protocol.core.methods.response.StackItem;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.transaction.Signer;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.Wallet;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.MountableFile;

@RunWith(Suite.class)
@Suite.SuiteClasses({RelationalOperatorsTest.class})
public class CompilerTestSuite {

    private static final String NODE_WALLET_FILE = "wallet.json";
    private static final String NODE_WALLET_PASSWORD = "neo";

    protected static final ScriptHash NEO_SCRIPT_HASH = NeoToken.SCRIPT_HASH;
    protected static final ScriptHash GAS_SCRIPT_HASH = GasToken.SCRIPT_HASH;

    private static final String NEO3_PRIVATENET_CONTAINER_IMG =
            "docker.pkg.github.com/axlabs/neo3-privatenet-docker/neo-cli-with-plugins:latest";

    // Exposed port of the neo node running in the docker container.
    protected static int EXPOSED_JSONRPC_PORT = 40332;

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

    protected static Account account;
    protected static Account multiSigAcc;
    protected static Wallet wallet;

    protected static Neow3j neow3j;

    protected static void setUp() throws Exception {
        NeoConfig.setMagicNumber(new byte[]{0x01, 0x03, 0x00, 0x0}); // Magic number 769

        account = Account.fromWIF("L1WMhxazScMhUrdv34JqQb1HFSQmWeN2Kpc1R9JGKwL7CDNP21uR");
        multiSigAcc = Account.createMultiSigAccount(
                Arrays.asList(account.getECKeyPair().getPublicKey()), 1);
        wallet = Wallet.withAccounts(account, multiSigAcc);

        neow3j = Neow3j.build(new HttpService(getNodeUrl(privateNetContainer)));
        openWallet();
    }

    @AfterClass
    public static void tearDown() {
        privateNetContainer.stop();
    }

    protected static String getResultFilePath(String testClassName, String methodName) {
        return "responses/" + testClassName + "/" + methodName + ".json";
    }

    protected static String getNodeUrl(GenericContainer container) {
        return "http://" + container.getContainerIpAddress() +
                ":" + container.getMappedPort(EXPOSED_JSONRPC_PORT);
    }

    protected static SmartContract deployContract(String fullyQualifiedName) throws IOException {
        CompilationResult res = new Compiler().compileClass(fullyQualifiedName);
        SmartContract sc = new SmartContract(res.getNef(), res.getManifest(), neow3j);
        NeoSendRawTransaction response = sc.deploy()
                .withWallet(wallet)
                .withSigners(Signer.calledByEntry(multiSigAcc.getScriptHash()))
                .build().sign().send();
        if (response.hasError()) {
            throw new RuntimeException(response.getError().getMessage());
        }
        return sc;
    }

    protected static <T extends StackItem> T loadExpectedResultFile(String contractFqn,
            String methodName, Class<T> stackItemType) throws IOException {

        String[] splitName = contractFqn.split("\\.");
        InputStream s = CompilerTestSuite.class.getClassLoader()
                .getResourceAsStream(getResultFilePath(splitName[splitName.length-1], methodName));
        return getObjectMapper().readValue(s, stackItemType);


    protected static Neow3j getNeow3j() {
        return neow3j;
    }

    protected static void openWallet() throws Exception {
        neow3j.openWallet(NODE_WALLET_FILE, NODE_WALLET_PASSWORD).send();
    }

    private static Callable<Long> callableGetTxHash(String txHash) {
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

    private static <T> void waitUntil(Callable<T> callable, Matcher<? super T> matcher) {
        await().timeout(30, TimeUnit.SECONDS).until(callable, matcher);
    }

    private static Callable<Long> callableGetBalance(String address, ScriptHash tokenScriptHash) {
        return () -> {
            try {
                List<Nep5Balance> balances = neow3j.getNep5Balances(address).send()
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

    private static Callable<Boolean> callableGetContractState(ScriptHash contractScriptHash) {
        return () -> {
            try {
                NeoGetContractState response =
                        neow3j.getContractState(contractScriptHash.toString()).send();
                if (response.hasError()) {
                    return false;
                }
                return response.getContractState().getHash().equals("0x" +
                        contractScriptHash.toString());
            } catch (IOException e) {
                return false;
            }
        };
    }

    private void transferTokensToAddress(ScriptHash tokenScriptHash, String address,
            long amount) throws IOException {

        NeoSendToAddress r = neow3j.sendToAddress(tokenScriptHash.toString(), address,
                String.valueOf(amount)).send();
        // ensure that the transaction is sent
        waitUntilBalancesIsGreaterThanZero(address, tokenScriptHash);
    }

    public static void waitUntilBalancesIsGreaterThanZero(String address,
            ScriptHash tokenScriptHash) {
        waitUntil(callableGetBalance(address, tokenScriptHash), Matchers.greaterThan(0L));
    }

    public static void waitUntilContractIsDeployed(ScriptHash contractScripHash) {
        waitUntil(callableGetContractState(contractScripHash), Matchers.is(true));
    }

}

