package io.neow3j.compiler;

import static io.neow3j.protocol.ObjectMapperFactory.getObjectMapper;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import io.neow3j.contract.ContractParameter;
import io.neow3j.contract.GasToken;
import io.neow3j.contract.NeoToken;
import io.neow3j.contract.ScriptHash;
import io.neow3j.contract.SmartContract;
import io.neow3j.model.NeoConfig;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.HexParameter;
import io.neow3j.protocol.core.methods.response.NeoGetContractState;
import io.neow3j.protocol.core.methods.response.NeoGetNep5Balances.Nep5Balance;
import io.neow3j.protocol.core.methods.response.NeoGetStorage;
import io.neow3j.protocol.core.methods.response.NeoGetTransaction;
import io.neow3j.protocol.core.methods.response.NeoGetTransactionHeight;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.protocol.core.methods.response.NeoSendRawTransaction;
import io.neow3j.protocol.core.methods.response.StackItem;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.transaction.Signer;
import io.neow3j.utils.Numeric;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.Wallet;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.MountableFile;

public class CompilerTest {

    // Exposed port of the neo node running in the docker container.
    protected static int EXPOSED_JSONRPC_PORT = 40332;
    protected static final String NEO3_PRIVATENET_CONTAINER_IMG =
            "docker.pkg.github.com/axlabs/neo3-privatenet-docker/neo-cli-with-plugins:latest";

    protected static final ScriptHash NEO_SCRIPT_HASH = NeoToken.SCRIPT_HASH;
    protected static final ScriptHash GAS_SCRIPT_HASH = GasToken.SCRIPT_HASH;
    protected static final String VM_STATE_HALT = "HALT";
    protected static final String VM_STATE_FAULT = "FAULT";

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
    protected static SmartContract contract;
    protected static String contractName;

    @Rule
    public TestName testName = new TestName();

    protected String getTestName() {
        return testName.getMethodName();
    }

    protected static void setUp(String name) throws Throwable {
        NeoConfig.setMagicNumber(new byte[]{0x01, 0x03, 0x00, 0x0}); // Magic number 769
        account = Account.fromWIF("L1WMhxazScMhUrdv34JqQb1HFSQmWeN2Kpc1R9JGKwL7CDNP21uR");
        multiSigAcc = Account.createMultiSigAccount(
                Arrays.asList(account.getECKeyPair().getPublicKey()), 1);
        wallet = Wallet.withAccounts(account, multiSigAcc);
        neow3j = Neow3j.build(new HttpService(getNodeUrl(privateNetContainer)));
        contractName = name;
        contract = deployContract(contractName);
        waitUntilContractIsDeployed(contract.getScriptHash());
    }

    protected static String getResultFilePath(String testClassName, String methodName) {
        return "responses/" + testClassName + "/" + methodName + ".json";
    }

    protected static String getNodeUrl(GenericContainer container) {
        return "http://" + container.getContainerIpAddress() +
                ":" + container.getMappedPort(EXPOSED_JSONRPC_PORT);
    }

    protected static SmartContract deployContract(String fullyQualifiedName) throws Throwable {
        CompilationUnit res = new Compiler().compileClass(fullyQualifiedName);
        SmartContract sc = new SmartContract(res.getNefFile(), res.getManifest(), neow3j);
        NeoSendRawTransaction response = sc.deploy()
                .wallet(wallet)
                .signers(Signer.calledByEntry(multiSigAcc.getScriptHash()))
                .sign().send();
        if (response.hasError()) {
            throw new RuntimeException(response.getError().getMessage());
        }
        return sc;
    }

    /**
     * Does a {@code invokefunction} JSON-RPC to the setup contract, the function with the current
     * test's name, and the given parameters.
     *
     * @param params The parameters to provide to the function call.
     * @return The result of the call.
     * @throws IOException if something goes wrong in the communication with the neo-node.
     */
    protected NeoInvokeFunction callInvokeFunction(ContractParameter... params) throws IOException {
        return callInvokeFunction(getTestName(), params);
    }

    /**
     * Does a {@code invokefunction} JSON-RPC to the contract under test, the given function and the
     * given parameters.
     *
     * @param function The function to call.
     * @param params   The parameters to provide to the function call.
     * @return The result of the call.
     * @throws IOException if something goes wrong in the communication with the neo-node.
     */
    protected NeoInvokeFunction callInvokeFunction(String function, ContractParameter... params)
            throws IOException {
        return contract.callInvokeFunction(function, Arrays.asList(params));
    }

    /**
     * Asserts that the given expected value is in the contracts storage under the given key.
     * <p>
     * The key is converted into a UTF-8 encoded byte array.
     *
     * @param key           The key to check.
     * @param expectedValue The expected value.
     */
    protected void assertStorageContains(String key, String expectedValue) throws IOException {
        NeoGetStorage response = neow3j.getStorage(contract.getScriptHash().toString(),
                HexParameter.valueOf(key)).send();
        String value = new String(
                Numeric.hexStringToByteArray(response.getStorage()),
                StandardCharsets.UTF_8);

        assertThat(value, is(expectedValue));
    }

    /**
     * Builds and sends a transaction that invokes the contract under test, the function with the
     * name of the current test method, with the given parameters.
     * <p>
     * The multi-sig account at {@link CompilerTest#multiSigAcc} is used to sign the transaction.
     *
     * @param params The parameters to pass with the function call.
     * @return the hash of the sent transaction.
     */
    protected String invokeFunction(ContractParameter... params) throws Throwable {
        return contract.invokeFunction(getTestName(), params)
                .wallet(wallet)
                .signers(Signer.calledByEntry(multiSigAcc.getScriptHash()))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
    }

    /**
     * Builds and sends a transaction that invokes the contract under test, the function with the
     * name of the current test method, with the given parameters. Sleeps until the transaction is
     * included in a block.
     * <p>
     * The multi-sig account at {@link CompilerTest#multiSigAcc} is used to sign the transaction.
     *
     * @param params The parameters to pass with the function call.
     * @return the hash of the transaction.
     */
    protected String invokeFunctionAndAwaitExecution(ContractParameter... params) throws Throwable {
        String txHash = invokeFunction(params);
        waitUntilTransactionIsExecuted(txHash);
        return txHash;
    }

    protected void assertVMExitedWithHalt(String hash) throws IOException {
        NeoGetTransaction response = neow3j.getTransaction(hash).send();
        assertThat(response.getTransaction().getVMState(), is(VM_STATE_HALT));
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

    public static void waitUntilBalancesIsGreaterThanZero(String address,
            ScriptHash tokenScriptHash) {
        waitUntil(callableGetBalance(address, tokenScriptHash), Matchers.greaterThan(0L));
    }

    public static void waitUntilContractIsDeployed(ScriptHash contractScripHash) {
        waitUntil(callableGetContractState(contractScripHash), Matchers.is(true));
    }

    public static void waitUntilTransactionIsExecuted(String txHash) {
        waitUntil(callableGetTxHash(txHash), notNullValue());
    }

    protected <T extends StackItem> T loadExpectedResultFile(Class<T> stackItemType)
            throws IOException {

        String[] splitName = contractName.split("\\.");
        InputStream s = this.getClass().getClassLoader().getResourceAsStream(
                getResultFilePath(splitName[splitName.length - 1], getTestName()));
        return getObjectMapper().readValue(s, stackItemType);
    }

    protected StackItem getFirstStackItem(NeoInvokeFunction response) {
        return response.getInvocationResult().getStack().get(0);
    }

//    protected void transferTokensTogTgT
//    NeoSendToAddress send = super.sendToAddress(NEO_HASH, toAddress, amount).send();
//    // ensure that the transaction is sent
//    waitUntilSendToAddressTransactionHasBeenExecuted();
//    // store the transaction hash to use this transaction in the tests
//        return send.getSendToAddress().getHash();
//
}
