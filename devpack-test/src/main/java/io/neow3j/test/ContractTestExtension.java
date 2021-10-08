package io.neow3j.test;

import io.neow3j.compiler.CompilationUnit;
import io.neow3j.compiler.Compiler;
import io.neow3j.contract.ContractManagement;
import io.neow3j.contract.SmartContract;
import io.neow3j.crypto.WIF;
import io.neow3j.protocol.Neow3jExpress;
import io.neow3j.protocol.ObjectMapperFactory;
import io.neow3j.protocol.core.response.NeoApplicationLog;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.script.VerificationScript;
import io.neow3j.transaction.AccountSigner;
import io.neow3j.transaction.Transaction;
import io.neow3j.transaction.TransactionBuilder;
import io.neow3j.types.ContractParameter;
import io.neow3j.types.Hash160;
import io.neow3j.types.Hash256;
import io.neow3j.types.NeoVMStateType;
import io.neow3j.utils.Await;
import io.neow3j.utils.Numeric;
import io.neow3j.wallet.Account;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ContractTestExtension implements BeforeAllCallback, AfterAllCallback {

    private static final String DEFAULT_NEOXP_CONFIG = "default.neo-express";

    // Extension Context Store
    final static String CONTAINER_STORE_KEY = "neoExpressContainer";
    final static String NEOW3J_STORE_KEY = "neow3j";
    final static String CONTRACT_STORE_KEY = "contracts";

    private String neoxpConfigFileName = DEFAULT_NEOXP_CONFIG;

    private NeoExpressTestContainer container;
    private Neow3jExpress neow3j;
    private DeployContext deployCtx;

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        ContractTest annotation = context.getTestClass().get().getAnnotation(ContractTest.class);
        if (annotation == null) {
            throw new ExtensionConfigurationException("Using the " + this.getClass().getSimpleName()
                    + " without the @" + ContractTest.class.getSimpleName() + " annotation.");
        }
        container = new NeoExpressTestContainer(annotation.blockTime());
        if (!annotation.neoxpConfig().isEmpty()) {
            neoxpConfigFileName = annotation.neoxpConfig();
        }
        container.withNeoxpConfig(neoxpConfigFileName);
        if (!annotation.batchFile().isEmpty()) {
            container.withBatchFile(annotation.batchFile());
        }
        if (!annotation.checkpoint().isEmpty()) {
            container.withCheckpoint(annotation.checkpoint());
        }
        container.start();
        neow3j = Neow3jExpress.build(new HttpService(container.getNodeUrl()));

        deployCtx = new DeployContext();
        for (Class<?> c : annotation.contracts()) {
            Method m = findCorrespondingDeployConfigMethod(c, context);
            ContractParameter deployParam = null;
            if (m != null) {
                deployParam = (ContractParameter) m.invoke(null, deployCtx);
            }
            SmartContract deployedContract = compileAndDeploy(c, deployParam, container, neow3j);
            deployCtx.addDeployedContract(c, deployedContract);
        }

        ExtensionContext.Store store = context.getStore(ExtensionContext.Namespace.GLOBAL);
        store.put(CONTAINER_STORE_KEY, container);
        store.put(NEOW3J_STORE_KEY, neow3j);
        store.put(CONTRACT_STORE_KEY, deployCtx);
    }

    private Method findCorrespondingDeployConfigMethod(Class<?> contract, ExtensionContext ctx) {
        // TODO: Check for correct signature of the method, i.e., if it returns ContractParameter.
        List<Method> methods = Arrays.stream(ctx.getTestClass().get().getMethods())
                .filter(m -> Modifier.isStatic(m.getModifiers()) &&
                        m.isAnnotationPresent(DeployConfig.class) &&
                        m.getAnnotation(DeployConfig.class).value().equals(contract))
                .collect(Collectors.toList());
        if (methods.isEmpty()) {
            return null;
        }
        if (methods.size() > 1) {
            throw new ExtensionConfigurationException("Specified more than one deployment " +
                    "configuration method for contract class " + contract.getCanonicalName());
        }
        return methods.get(0);
    }

    private SmartContract compileAndDeploy(Class<?> contractClass,
            NeoExpressTestContainer container, Neow3jExpress neow3j) throws Throwable {
        return compileAndDeploy(contractClass, null, container, neow3j);
    }

    private SmartContract compileAndDeploy(Class<?> contractClass, ContractParameter deployParam,
            NeoExpressTestContainer container, Neow3jExpress neow3j) throws IOException {

        CompilationUnit res = new Compiler().compile(contractClass.getCanonicalName());
        Account sender = getAccount("Alice");
        TransactionBuilder txBuilder = new ContractManagement(neow3j)
                .deploy(res.getNefFile(), res.getManifest(), deployParam)
                // TODO: this account needs to be generic or set by the dev.
                .signers(AccountSigner.global(sender)); // TODO: Global needed?
        Transaction tx;
        try {
            tx = txBuilder.sign();
        } catch (Throwable t) {
            throw new ExtensionConfigurationException("Error when signing deploy transaction.", t);
        }
        Hash256 txHash = tx.send().getSendRawTransaction().getHash();
        Await.waitUntilTransactionIsExecuted(txHash, neow3j);
        NeoApplicationLog log = neow3j.getApplicationLog(txHash).send().getApplicationLog();
        if (log.getExecutions().get(0).getState().equals(NeoVMStateType.FAULT)) {
            throw new ExtensionConfigurationException("Failed to deploy smart contract. NeoVM " +
                    "error message: " + log.getExecutions().get(0).getException());
        }
        Hash160 contractHash = SmartContract.calcContractHash(sender.getScriptHash(),
                res.getNefFile().getCheckSumAsInteger(), res.getManifest().getName());
        return new SmartContract(contractHash, neow3j);
    }

    @Override
    public void afterAll(ExtensionContext context) {
        container.stop();
    }

    /**
     * Gets the contract that is under test. Use the contract class to make calls to the contract.
     *
     * @return the contract under test.
     */
    public SmartContract getContractUnderTest(Class<?> contractClass) {
        return deployCtx.getDeployedContract(contractClass);
    }

    /**
     * Gets the Neow3j instance that allows for calls to the underlying neo-express instance.
     *
     * @return the Neow3j instance.
     */
    public Neow3jExpress getNeow3j() {
        return neow3j;
    }

    /**
     * Starts neo-express in the test container if it is not running yet.
     *
     * @throws Exception if running neo-express failed, or it was already running.
     */
    public void runExpress() throws Exception {
        container.runExpress();
    }

    /**
     * Stops neo-express in the test container if it is not already stopped. The container will
     * continue running, allowing you to start up neo-express again.
     *
     * @throws Exception if stopping neo-express failed, or it was already stopped.
     */
    public void stopExpress() throws Exception {
        container.stopExpress();
    }

    /**
     * Creates a new account on the neo-express instance and returns its Neo address.
     *
     * @param name The desired name of the account.
     * @return The account's address
     * @throws Exception if creating the account failed.
     */
    public String createAccount(String name) throws Exception {
        return container.createAccount(name);
    }

    /**
     * Transfers assets.
     *
     * @param amount   The amount of assets to transfer.
     * @param asset    The asset to transfer. Can be a symbol, e.g., "NEO", or the hash of a
     *                 contract.
     * @param sender   The sender. Can be a name of a wallet, e.g., "genesis", or an address.
     * @param receiver The receiver. Can be a name of a wallet, e.g., "genesis", or an address.
     * @return The transaction hash of the transfer.
     * @throws Exception if the transfer transaction cannot be created or propagated.
     */
    public Hash256 transfer(BigInteger amount, String asset, String sender, String receiver)
            throws Exception {
        return new Hash256(container.transfer(amount, asset, sender, receiver));
    }

    /**
     * Gets the account for the given name if it exists on the neo-express instance. If the
     * account is a multi-sig account, it will not have a private key available for transaction
     * signing.
     *
     * @param name The account's name
     * @return The account
     * @throws IOException                     if an error occurs reading the neo-express
     *                                         configuration.
     * @throws ExtensionConfigurationException if the account name cannot be found.
     */
    public Account getAccount(String name) throws IOException {
        InputStream s = ContractTestExtension.class.getClassLoader()
                .getResourceAsStream(neoxpConfigFileName);
        NeoExpressConfig config = ObjectMapperFactory.getObjectMapper()
                .readValue(s, NeoExpressConfig.class);

        Optional<NeoExpressConfig.Wallet.Account> acc = Stream.concat(
                        config.getConsensusNodes().stream().flatMap(n -> n.getWallet().getAccounts().stream()),
                        config.getWallets().stream().flatMap(w -> w.getAccounts().stream()))
                .filter(a -> a.label != null && a.label.equals(name)).findFirst();

        if (!acc.isPresent()) {
            throw new ExtensionConfigurationException("Account '" + name + "' not found.");
        }
        VerificationScript verifScript = new VerificationScript(
                Numeric.hexStringToByteArray(acc.get().getContract().getScript()));
        if (verifScript.isMultiSigScript()) {
            return Account.fromVerificationScript(verifScript);
        }
        return Account.fromWIF(WIF.getWIFFromPrivateKey(
                Numeric.hexStringToByteArray(acc.get().privateKey)));
    }

}
