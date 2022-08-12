package io.neow3j.compiler;

import io.neow3j.contract.ContractManagement;
import io.neow3j.contract.NefFile;
import io.neow3j.contract.SmartContract;
import io.neow3j.crypto.Base64;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.response.ContractManifest;
import io.neow3j.protocol.core.response.InvocationResult;
import io.neow3j.protocol.core.response.NeoApplicationLog;
import io.neow3j.protocol.core.response.NeoGetApplicationLog;
import io.neow3j.protocol.core.response.NeoGetStorage;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import io.neow3j.protocol.core.response.NeoSendRawTransaction;
import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.test.NeoTestContainer;
import io.neow3j.transaction.AccountSigner;
import io.neow3j.transaction.Signer;
import io.neow3j.transaction.Transaction;
import io.neow3j.transaction.TransactionBuilder;
import io.neow3j.transaction.Witness;
import io.neow3j.types.ContractParameter;
import io.neow3j.types.Hash160;
import io.neow3j.types.Hash256;
import io.neow3j.types.NeoVMStateType;
import io.neow3j.utils.Numeric;
import io.neow3j.wallet.Account;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.ContainerState;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static io.neow3j.crypto.Sign.signMessage;
import static io.neow3j.test.TestProperties.client1AccountWIF;
import static io.neow3j.test.TestProperties.client2AccountWIF;
import static io.neow3j.test.TestProperties.defaultAccountWIF;
import static io.neow3j.transaction.Witness.createMultiSigWitness;
import static io.neow3j.types.ContractParameter.hash160;
import static io.neow3j.utils.ArrayUtils.reverseArray;
import static io.neow3j.utils.Await.waitUntilBlockCountIsGreaterThanZero;
import static io.neow3j.utils.Await.waitUntilContractIsDeployed;
import static io.neow3j.utils.Await.waitUntilTransactionIsExecuted;
import static io.neow3j.utils.Numeric.hexStringToByteArray;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ContractTestExtension implements AfterAllCallback, BeforeAllCallback {

    private NeoTestContainer neoTestContainer;
    private String fullyQualifiedClassName;
    private Account defaultAccount;
    private Account committee;
    private Account client1;
    private Account client2;
    private Neow3j neow3j;
    private SmartContract contract;
    private Hash256 deployTxHash;
    private Hash256 blockHashOfDeployTx;

    private boolean signAsCommittee = false;
    private boolean signWithDefaultAccount = false;

    private static final String SET_HASH = "setHash";
    private static final int DEFAULT_ITERATOR_COUNT = 100;

    public ContractTestExtension(String fullyQualifiedName) {
        this.fullyQualifiedClassName = fullyQualifiedName;
    }

    public ContractTestExtension() {
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        neoTestContainer = new NeoTestContainer();
        try {
            neoTestContainer.start();
            setUp(neoTestContainer.getNodeUrl());
            if (fullyQualifiedClassName != null) {
                contract = deployContract(fullyQualifiedClassName);
                waitUntilContractIsDeployed(getContract().getScriptHash(), neow3j);
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void afterAll(ExtensionContext context) {
        neoTestContainer.stop();
    }

    public void setUp(String containerURL) throws Throwable {
        defaultAccount = Account.fromWIF(defaultAccountWIF());
        committee = Account.createMultiSigAccount(asList(defaultAccount.getECKeyPair().getPublicKey()), 1);
        client1 = Account.fromWIF(client1AccountWIF());
        client2 = Account.fromWIF(client2AccountWIF());
        neow3j = Neow3j.build(new HttpService(containerURL, true));
        waitUntilBlockCountIsGreaterThanZero(neow3j);
    }

    public SmartContract deployContract(NefFile nef, ContractManifest manifest) throws Throwable {
        Transaction tx = new ContractManagement(neow3j)
                .deploy(nef, manifest)
                .signers(AccountSigner.calledByEntry(committee))
                .getUnsignedTransaction();
        Witness multiSigWitness = createMultiSigWitness(
                asList(signMessage(tx.getHashData(), defaultAccount.getECKeyPair())),
                committee.getVerificationScript());
        NeoSendRawTransaction response = tx.addWitness(multiSigWitness).send();
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
        if (execution.getState().equals(NeoVMStateType.FAULT)) {
            throw new IllegalStateException(format("Failed deploying the contract '%s'. Exception "
                    + "message was: '%s'", manifest.getName(), execution.getException()));
        }
        String scriptHashHex = execution.getStack().get(0).getList().get(2).getHexString();
        Hash160 scriptHash = new Hash160(reverseArray(hexStringToByteArray(scriptHashHex)));
        return new SmartContract(scriptHash, neow3j);
    }

    public SmartContract deployContract(String fullyQualifiedName) throws Throwable {
        CompilationUnit res = new Compiler().compile(fullyQualifiedName);
        return deployContract(res.getNefFile(), res.getManifest());
    }

    public Account getDefaultAccount() {
        return defaultAccount;
    }

    public Account getCommittee() {
        return committee;
    }

    public Account getClient1() {
        return client1;
    }

    public Account getClient2() {
        return client2;
    }

    public Neow3j getNeow3j() {
        return neow3j;
    }

    public SmartContract getContract() {
        return contract;
    }

    public Hash256 getDeployTxHash() {
        return deployTxHash;
    }

    public Hash256 getBlockHashOfDeployTx() {
        return blockHashOfDeployTx;
    }

    /**
     * Does an {@code invokefunction} JSON-RPC to the setup contract, the function with the current test's name, and
     * the given parameters.
     *
     * @param testInfo the test method information.
     * @param params   the parameters to provide to the function call.
     * @return the result of the call.
     * @throws IOException if something goes wrong in the communication with the neo-node.
     */
    public NeoInvokeFunction callInvokeFunction(TestInfo testInfo, ContractParameter... params) throws IOException {
        return callInvokeFunction(
                testInfo.getTestMethod().orElseThrow(() -> new IllegalArgumentException("Could not get test method."))
                        .getName(),
                params);
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
    public NeoInvokeFunction callInvokeFunction(String function, ContractParameter... params) throws IOException {
        if (signAsCommittee) {
            return contract.callInvokeFunction(function, asList(params),
                    AccountSigner.global(committee.getScriptHash()));
        }
        if (signWithDefaultAccount) {
            return contract.callInvokeFunction(function, asList(params),
                    AccountSigner.global(defaultAccount.getScriptHash()));
        }
        return contract.callInvokeFunction(function, asList(params));
    }

    public List<StackItem> callAndTraverseIterator(String function, ContractParameter... params) throws IOException {
        InvocationResult invocationResult = callInvokeFunction(function, params).getInvocationResult();
        String sessionId = invocationResult.getSessionId();
        String iteratorId = invocationResult.getStack().get(0).getIteratorId();
        return traverseIterator(sessionId, iteratorId);
    }

    public List<StackItem> traverseIterator(String sessionId, String iteratorId) throws IOException {
        List<StackItem> iter = neow3j.traverseIterator(sessionId, iteratorId, DEFAULT_ITERATOR_COUNT).send()
                .getTraverseIterator();
        neow3j.terminateSession(sessionId).send();
        return iter;
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
        NeoGetStorage response = neow3j.getStorage(getContract().getScriptHash(),
                Numeric.toHexStringNoPrefix(key.getBytes(UTF_8))).send();
        String value = new String(Base64.decode(response.getStorage()), UTF_8);

        assertThat(value, is(expectedValue));
    }

    /**
     * Transfers the given amount of GAS to the {@code to} account. The amount is taken from the committee account.
     *
     * @param to     The receiving account.
     * @param amount The amount to transfer.
     * @return the hash of the transfer transaction.
     * @throws Throwable if an error occurs when communicating the neo-node, or when
     *                   constructing the transaction object.
     */
    public Hash256 transferGas(Hash160 to, BigInteger amount) throws Throwable {
        io.neow3j.contract.GasToken gasToken = new io.neow3j.contract.GasToken(neow3j);
        Transaction tx = gasToken.transfer(committee, to, amount).getUnsignedTransaction();
        Witness multiSigWitness = createMultiSigWitness(
                asList(signMessage(tx.getHashData(), defaultAccount.getECKeyPair())),
                committee.getVerificationScript());
        return tx.addWitness(multiSigWitness).send().getSendRawTransaction().getHash();
    }

    /**
     * Transfers the given amount of NEO to the {@code to} account. The amount is taken from the committee account.
     *
     * @param to     The receiving account.
     * @param amount The amount to transfer.
     * @return the hash of the transfer transaction.
     * @throws Throwable if an error occurs when communicating the neo-node, or when constructing the transaction
     *                   object.
     */
    public Hash256 transferNeo(Hash160 to, BigInteger amount) throws Throwable {
        io.neow3j.contract.NeoToken neoToken = new io.neow3j.contract.NeoToken(neow3j);
        Transaction tx = neoToken.transfer(committee, to, amount).getUnsignedTransaction();
        Witness multiSigWitness = createMultiSigWitness(
                asList(signMessage(tx.getHashData(), defaultAccount.getECKeyPair())),
                committee.getVerificationScript());
        return tx.addWitness(multiSigWitness).send().getSendRawTransaction().getHash();
    }

    /**
     * Builds and sends a transaction that invokes the contract under test, the given function, with the given
     * parameters.
     * <p>
     * The transaction sender is either the committee or the default account. The provided signers are appended.
     *
     * @param function The function to invoke.
     * @param params   The parameters to pass with the function call.
     * @return the hash of the sent transaction.
     */
    public Hash256 invokeFunction(String function, ContractParameter... params) throws Throwable {
        return invokeFunction(function, asList(params));
    }

    /**
     * Builds and sends a transaction that invokes the contract under test, the given function, with the given
     * parameters.
     * <p>
     * The transaction sender is either the committee or the default account. The provided signers are appended.
     *
     * @param function          the function to call.
     * @param params            the parameters to pass with the function call.
     * @param additionalSigners the additional signers.
     * @return the hash of the sent transaction.
     */
    public Hash256 invokeFunction(String function, List<ContractParameter> params, Signer... additionalSigners)
            throws Throwable {

        Transaction tx;
        NeoSendRawTransaction response;
        TransactionBuilder b = contract.invokeFunction(function, params.toArray(new ContractParameter[0]));
        List<Signer> signerList = new ArrayList<>(asList(additionalSigners));
        if (signAsCommittee) {
            signerList.add(AccountSigner.global(committee));
            Signer[] modifiedSigners = signerList.toArray(new Signer[0]);
            tx = b.signers(modifiedSigners).firstSigner(committee).getUnsignedTransaction();
            byte[] txHashData = tx.getHashData();
            Witness committeeMultiSigWitness = createMultiSigWitness(
                    asList(signMessage(txHashData, defaultAccount.getECKeyPair())),
                    committee.getVerificationScript());
            tx.addWitness(committeeMultiSigWitness);
            for (Signer s : modifiedSigners) {
                if (!s.getScriptHash().equals(committee.getScriptHash())) {
                    tx.addWitness(((AccountSigner) s).getAccount());
                }
            }
            response = tx.send();
        } else {
            signerList.add(AccountSigner.global(defaultAccount));
            Signer[] modifiedSigners = signerList.toArray(new Signer[0]);
            response = b.signers(modifiedSigners).firstSigner(defaultAccount).sign().send();
        }

        if (response.hasError()) {
            throw new RuntimeException(response.getError().getMessage());
        }
        return response.getSendRawTransaction().getHash();
    }

    /**
     * Builds and sends a transaction that invokes the contract under test, the given function, with
     * the given parameters. Sleeps until the transaction is included in a block.
     * <p>
     * The transaction sender is either the committee or the default account.
     *
     * @param function The function to call.
     * @param params   The parameters to pass with the function call.
     * @return the hash of the transaction.
     */
    public Hash256 invokeFunctionAndAwaitExecution(String function, ContractParameter... params) throws Throwable {
        Hash256 txHash = invokeFunction(function, params);
        waitUntilTransactionIsExecuted(txHash, neow3j);
        return txHash;
    }

    /**
     * Builds and sends a transaction that invokes the contract under test, the given function, with the given
     * parameters. Sleeps until the transaction is included in a block.
     * <p>
     * The transaction sender is either the committee or the default account. The provided signers are appended.
     *
     * @param function the function to invoke.
     * @param params   the parameters to pass with the function call.
     * @return the hash of the transaction.
     */
    public Hash256 invokeFunctionAndAwaitExecution(String function, List<ContractParameter> params,
            Signer... additionalSigners) throws Throwable {
        Hash256 txHash = invokeFunction(function, params, additionalSigners);
        waitUntilTransactionIsExecuted(txHash, neow3j);
        return txHash;
    }

    public void assertVMExitedWithHalt(Hash256 hash) throws IOException {
        NeoGetApplicationLog response = neow3j.getApplicationLog(hash).send();
        assertThat(response.getApplicationLog().getExecutions().get(0).getState(), is(NeoVMStateType.HALT));
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

    public void setHash(Hash160 scriptHash) throws Throwable {
        invokeFunctionAndAwaitExecution(SET_HASH, hash160(scriptHash));
    }

}
