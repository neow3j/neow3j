package io.neow3j.compiler;

import io.neow3j.NeoTestContainer;
import io.neow3j.contract.ContractManagement;
import io.neow3j.contract.ContractParameter;
import io.neow3j.contract.Hash160;
import io.neow3j.contract.Hash256;
import io.neow3j.contract.SmartContract;
import io.neow3j.crypto.Base64;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.methods.response.NeoApplicationLog;
import io.neow3j.protocol.core.methods.response.NeoGetApplicationLog;
import io.neow3j.protocol.core.methods.response.NeoGetStorage;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.protocol.core.methods.response.NeoSendRawTransaction;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.transaction.Signer;
import io.neow3j.utils.Numeric;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.Wallet;
import org.junit.rules.TestName;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.testcontainers.containers.ContainerState;

import java.io.IOException;
import java.math.BigDecimal;

import static io.neow3j.TestProperties.defaultAccountWIF;
import static io.neow3j.NeoTestContainer.getNodeUrl;
import static io.neow3j.utils.Await.waitUntilContractIsDeployed;
import static io.neow3j.utils.Await.waitUntilTransactionIsExecuted;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ContractTestRule implements TestRule {

    public static final String VM_STATE_HALT = "HALT";
    public static final String VM_STATE_FAULT = "FAULT";

    private NeoTestContainer neoTestContainer;
    private final String fullyQualifiedClassName;
    private Account defaultAccount;
    private Account committee;
    private Wallet wallet;
    private Neow3j neow3j;
    private SmartContract contract;
    private String contractName;
    private Hash256 deployTxHash;
    private Hash256 blockHashOfDeployTx;

    private boolean signAsCommittee = false;
    private boolean signWithDefaultAccount = false;

    public ContractTestRule(String fullyQualifiedName) {
        this.fullyQualifiedClassName = fullyQualifiedName;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                neoTestContainer = new NeoTestContainer();
                try {
                    neoTestContainer.start();
                    setUp(fullyQualifiedClassName, getNodeUrl(neoTestContainer));
                    base.evaluate();
                } finally {
                    neoTestContainer.stop();
                }
            }
        };
    }

    public void setUp(String name, String containerURL) throws Throwable {
        defaultAccount = Account.fromWIF(defaultAccountWIF());
        committee = Account.createMultiSigAccount(
                singletonList(defaultAccount.getECKeyPair().getPublicKey()), 1);
        wallet = Wallet.withAccounts(defaultAccount, committee);
        neow3j = neow3j.build(new HttpService(containerURL));
        contractName = name;
        contract = deployContract(contractName);
        waitUntilContractIsDeployed(getContract().getScriptHash(), neow3j);
    }

    protected SmartContract deployContract(String fullyQualifiedName) throws Throwable {
        CompilationUnit res = new Compiler().compileClass(fullyQualifiedName);
        NeoSendRawTransaction response = new ContractManagement(neow3j)
                .deploy(res.getNefFile(), res.getManifest())
                .wallet(wallet)
                .signers(Signer.calledByEntry(committee.getScriptHash()))
                .sign()
                .send();
        if (response.hasError()) {
            throw new RuntimeException(response.getError().getMessage());
        }

        // Remember the transaction and its block.
        deployTxHash = response.getSendRawTransaction().getHash();
        waitUntilTransactionIsExecuted(deployTxHash, neow3j);
        blockHashOfDeployTx = neow3j.getTransaction(deployTxHash).send()
                .getTransaction().getBlockHash();
        // Get the contract address from the application logs.
        NeoApplicationLog appLog = neow3j.getApplicationLog(
                response.getSendRawTransaction().getHash()).send().getApplicationLog();
        NeoApplicationLog.Execution execution = appLog.getExecutions().get(0);
        if (execution.getState().equals(VM_STATE_FAULT)) {
            throw new IllegalStateException(format("Failed deploying the contract '%s'. Exception "
                    + "message was: '%s'", fullyQualifiedName, execution.getException()));
        }
        String scriptHashHex = execution.getStack().get(0).getList().get(2).getHexString();
        Hash160 scriptHash = new Hash160(Numeric.hexStringToByteArray(scriptHashHex));
        return new SmartContract(scriptHash, neow3j);
    }

    public Account getDefaultAccount() {
        return defaultAccount;
    }

    public Account getCommittee() {
        return committee;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public Neow3j getNeow3j() {
        return neow3j;
    }

    public SmartContract getContract() {
        return contract;
    }

    public String getContractName() {
        return contractName;
    }

    public Hash256 getDeployTxHash() {
        return deployTxHash;
    }

    public Hash256 getBlockHashOfDeployTx() {
        return blockHashOfDeployTx;
    }

    /**
     * Does an {@code invokefunction} JSON-RPC to the setup contract, the function with the current
     * test's name, and the given parameters.
     *
     * @param params The parameters to provide to the function call.
     * @return The result of the call.
     * @throws IOException if something goes wrong in the communication with the neo-node.
     */
    public NeoInvokeFunction callInvokeFunction(TestName testName, ContractParameter... params)
            throws IOException {
        return callInvokeFunction(testName.getMethodName(), params);
    }

    /**
     * Does a {@code invokefunction} JSON-RPC to the contract under test, the given function, with
     * the given parameters and signer.
     *
     * @param function The function to call.
     * @param params   The parameters to provide to the function call.
     * @return The result of the call.
     * @throws IOException if something goes wrong in the communication with the neo-node.
     */
    public NeoInvokeFunction callInvokeFunction(String function, ContractParameter... params)
            throws IOException {

        if (signAsCommittee) {
            return contract.callInvokeFunction(function, asList(params),
                    Signer.global(committee.getScriptHash()));
        }
        if (signWithDefaultAccount) {
            return contract.callInvokeFunction(function, asList(params),
                    Signer.global(defaultAccount.getScriptHash()));
        }
        return contract.callInvokeFunction(function, asList(params));
    }

    /**
     * Asserts that the given expected value is in the contracts storage under the given key.
     * <p>
     * The key is converted into a UTF-8 encoded byte array.
     *
     * @param key           The key to check.
     * @param expectedValue The expected value.
     */
    public void assertStorageContains(String key, String expectedValue) throws IOException {
        NeoGetStorage response = neow3j.getStorage(getContract().getScriptHash().toString(),
                Numeric.toHexStringNoPrefix(key.getBytes(UTF_8))).send();
        String value = new String(Base64.decode(response.getStorage()), UTF_8);

        assertThat(value, is(expectedValue));
    }

    /**
     * Builds and sends a transaction that invokes the contract under test, the function with the
     * name of the current test method, with the given parameters.
     *
     * @param params The parameters to pass with the function call.
     * @return the hash of the sent transaction.
     */
    public Hash256 invokeFunction(TestName testName, ContractParameter... params) throws Throwable {
        return invokeFunction(testName.getMethodName(), params);
    }

    /**
     * Transfers the given amount of GAS to the {@code to} account. The amount is taken from the
     * committee account.
     *
     * @param to     The receiving account.
     * @param amount The amount to transfer.
     * @return the hash of the transfer transaction.
     * @throws Throwable if an error occurs when communicating the the neo-node, or when
     *                   constructing the transaction object.
     */
    public Hash256 transferGas(Hash160 to, String amount) throws Throwable {
        io.neow3j.contract.GasToken gasToken = new io.neow3j.contract.GasToken(neow3j);
        return gasToken.transferFromSpecificAccounts(wallet, to, new BigDecimal(amount),
                committee.getScriptHash())
                .sign()
                .send()
                .getSendRawTransaction().getHash();
    }

    /**
     * Transfers the given amount of NEO to the {@code to} account. The amount is taken from the
     * committee account.
     *
     * @param to     The receiving account.
     * @param amount The amount to transfer.
     * @return the hash of the transfer transaction.
     * @throws Throwable if an error occurs when communicating the the neo-node, or when
     *                   constructing the transaction object.
     */
    public Hash256 transferNeo(Hash160 to, String amount) throws Throwable {
        io.neow3j.contract.NeoToken neoToken = new io.neow3j.contract.NeoToken(neow3j);
        return neoToken.transferFromSpecificAccounts(wallet, to, new BigDecimal(amount),
                committee.getScriptHash())
                .sign()
                .send()
                .getSendRawTransaction().getHash();
    }

    /**
     * Builds and sends a transaction that invokes the contract under test, the given function, with
     * the given parameters.
     *
     * @param function The function to call.
     * @param params   The parameters to pass with the function call.
     * @return the hash of the sent transaction.
     */
    public Hash256 invokeFunction(String function, ContractParameter... params)
            throws Throwable {

        Signer signer;
        if (signAsCommittee) {
            signer = Signer.global(committee.getScriptHash());
        } else {
            signer = Signer.global(defaultAccount.getScriptHash());
        }
        NeoSendRawTransaction response = contract.invokeFunction(function, params)
                .wallet(wallet)
                .signers(signer)
                .sign()
                .send();

        if (response.hasError()) {
            throw new RuntimeException(response.getError().getMessage());
        }
        return response.getSendRawTransaction().getHash();
    }

    /**
     * Builds and sends a transaction that invokes the contract under test, the function with the
     * name of the current test method, with the given parameters. Sleeps until the transaction is
     * included in a block.
     * <p>
     * The multi-sig account at {@link ContractTestRule#getCommittee()} is used to sign
     * the transaction.
     *
     * @param params The parameters to pass with the function call.
     * @return the hash of the transaction.
     */
    public Hash256 invokeFunctionAndAwaitExecution(TestName testName, ContractParameter... params)
            throws Throwable {
        return invokeFunctionAndAwaitExecution(testName.getMethodName(), params);
    }

    /**
     * Builds and sends a transaction that invokes the contract under test, the given function, with
     * the given parameters. Sleeps until the transaction is included in a block.
     * <p>
     * The multi-sig account at {@link ContractTestRule#getCommittee()} is used to sign
     * the transaction.
     *
     * @param function The function to call.
     * @param params   The parameters to pass with the function call.
     * @return the hash of the transaction.
     */
    public Hash256 invokeFunctionAndAwaitExecution(String function, ContractParameter... params)
            throws Throwable {

        Hash256 txHash = invokeFunction(function, params);
        waitUntilTransactionIsExecuted(txHash, neow3j);
        return txHash;
    }

    public void assertVMExitedWithHalt(Hash256 hash) throws IOException {
        NeoGetApplicationLog response = neow3j.getApplicationLog(hash).send();
        assertThat(response.getApplicationLog().getExecutions().get(0).getState(),
                is(VM_STATE_HALT));
    }

    public void signWithCommitteeAccount() {
        signWithDefaultAccount = false;
        signAsCommittee = true;
    }

    public void signWithDefaultAccount() {
        signAsCommittee = false;
        signWithDefaultAccount = true;
    }

    public ContainerState getNeoTestContainer() {
        return neoTestContainer;
    }
}
