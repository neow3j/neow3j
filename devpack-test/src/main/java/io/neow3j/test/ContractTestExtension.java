package io.neow3j.test;

import io.neow3j.compiler.CompilationUnit;
import io.neow3j.compiler.Compiler;
import io.neow3j.contract.ContractManagement;
import io.neow3j.contract.SmartContract;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.response.NeoApplicationLog;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.script.VerificationScript;
import io.neow3j.transaction.AccountSigner;
import io.neow3j.transaction.Transaction;
import io.neow3j.types.Hash160;
import io.neow3j.types.Hash256;
import io.neow3j.types.NeoVMStateType;
import io.neow3j.utils.Await;
import io.neow3j.wallet.Account;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static io.neow3j.utils.Numeric.hexStringToByteArray;
import static java.lang.String.format;

public class ContractTestExtension implements BeforeAllCallback, AfterAllCallback {

    // Extension Context Store
    final static String CHAIN_STORE_KEY = "testChain";
    final static String NEOW3J_STORE_KEY = "neow3j";
    final static String DEPLOY_CTX_STORE_KEY = "contracts";

    private Neow3j neow3j;
    private DeployContext deployCtx = new DeployContext();
    private TestBlockchain chain;

    public ContractTestExtension() {
        chain = new NeoExpressTestContainer();
    }

    public ContractTestExtension(TestBlockchain chain) {
        this.chain = chain;
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        ContractTest annotation = context.getTestClass().get().getAnnotation(ContractTest.class);
        if (annotation == null) {
            throw new ExtensionConfigurationException("Using the " + this.getClass().getSimpleName()
                    + " without the @" + ContractTest.class.getSimpleName() + " annotation.");
        }
        if (annotation.blockTime() != 0) {
            chain.withSecondsPerBlock(annotation.blockTime());
        }
        if (!annotation.configFile().isEmpty()) {
            chain.withConfigFile(annotation.configFile());
        }
        if (!annotation.batchFile().isEmpty()) {
            chain.withBatchFile(annotation.batchFile());
        }
        if (!annotation.checkpoint().isEmpty()) {
            chain.withCheckpoint(annotation.checkpoint());
        }
        chain.start();
        neow3j = Neow3j.build(new HttpService(chain.getNodeUrl()));

        for (Class<?> c : annotation.contracts()) {
            Method m = findCorrespondingDeployConfigMethod(c, context);
            DeployConfiguration config = new DeployConfiguration();
            if (m != null) {
                if (m.getParameterCount() == 1) {
                    m.invoke(null, config);
                } else if (m.getParameterCount() == 2) {
                    m.invoke(null, config, deployCtx);
                }
            }
            SmartContract deployedContract = null;
            try {
                deployedContract = compileAndDeploy(c, config, neow3j);
            } catch (Throwable t) {
                throw new Exception(t);
            }
            deployCtx.addDeployedContract(c, deployedContract);
        }

        ExtensionContext.Store store = context.getStore(ExtensionContext.Namespace.GLOBAL);
        store.put(CHAIN_STORE_KEY, chain);
        store.put(NEOW3J_STORE_KEY, neow3j);
        store.put(DEPLOY_CTX_STORE_KEY, deployCtx);
    }

    private Method findCorrespondingDeployConfigMethod(Class<?> contract, ExtensionContext ctx) {
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
        Method method = methods.get(0);
        if (!method.getReturnType().equals(void.class)) {
            throw new ExtensionConfigurationException("Methods annotated with " +
                    DeployConfig.class.getSimpleName() + " must return void.");
        }

        boolean hasOneDeployConfigParam = method.getParameterCount() == 1 &&
                method.getParameterTypes()[0].equals(DeployConfiguration.class);
        boolean hasDeployConfigAndContextParams = method.getParameterCount() == 2
                && method.getParameterTypes()[0].equals(DeployConfiguration.class)
                && method.getParameterTypes()[1].equals(DeployContext.class);
        if (!hasOneDeployConfigParam && !hasDeployConfigAndContextParams) {
            throw new ExtensionConfigurationException(format("Methods annotated with '%s' must " +
                            "have a '%s' as the first parameter, optionally followed by a '%s' " +
                            "parameter.",
                    DeployConfig.class.getSimpleName(),
                    DeployConfiguration.class.getSimpleName(),
                    DeployContext.class.getSimpleName()));
        }
        return method;
    }

    private SmartContract compileAndDeploy(Class<?> contractClass, DeployConfiguration conf,
            Neow3j neow3j) throws Throwable {

        CompilationUnit res;
        if (conf.getSubstitutions().isEmpty()) {
            res = new Compiler().compile(contractClass.getCanonicalName());
        } else {
            res = new Compiler().compile(contractClass.getCanonicalName(), conf.getSubstitutions());
        }
        TestBlockchain.GenesisAccount genAcc = chain.getGenesisAccount();
        Account genesisAccount = Account.fromVerificationScript(new VerificationScript(
                hexStringToByteArray(genAcc.getVerificationScript())));
        Account[] signerAccounts = genAcc.getPrivateKeys().stream()
                .map(k -> new Account(ECKeyPair.create(hexStringToByteArray(k))))
                .toArray(Account[]::new);
        Transaction tx = new ContractManagement(neow3j)
                .deploy(res.getNefFile(), res.getManifest(), conf.getDeployParam())
                .signers(AccountSigner.calledByEntry(genesisAccount))
                .getUnsignedTransaction()
                .addMultiSigWitness(genesisAccount.getVerificationScript(), signerAccounts);

        Hash256 txHash = tx.send().getSendRawTransaction().getHash();
        Await.waitUntilTransactionIsExecuted(txHash, neow3j);
        NeoApplicationLog log = neow3j.getApplicationLog(txHash).send().getApplicationLog();
        if (log.getExecutions().get(0).getState().equals(NeoVMStateType.FAULT)) {
            throw new ExtensionConfigurationException("Failed to deploy smart contract. NeoVM " +
                    "error message: " + log.getExecutions().get(0).getException());
        }
        Hash160 contractHash = SmartContract.calcContractHash(genesisAccount.getScriptHash(),
                res.getNefFile().getCheckSumAsInteger(), res.getManifest().getName());
        deployCtx.addDeployTxHash(contractClass, txHash);
        return new SmartContract(contractHash, neow3j);
    }

    @Override
    public void afterAll(ExtensionContext context) {
        chain.stop();
    }

    /**
     * Gets an instance of {@code SmartContract} for the given contract under test. It can be
     * used as a handle to interact with the contract.
     *
     * @param contractClass The contract to get the {@code SmartContract} instance for.
     * @return the {@code SmartContract} instance.
     */
    public SmartContract getDeployedContract(Class<?> contractClass) {
        return deployCtx.getDeployedContract(contractClass);
    }

    /**
     * Gets the hash of the transaction in which the given contract was deployed.
     *
     * @param contractClass The class of the deployed contract.
     * @return the transaction hash.
     */
    public Hash256 getDeployTxHash(Class<?> contractClass) {
        return deployCtx.getDeployTxHash(contractClass);
    }

    /**
     * Gets the Neow3j instance that allows for calls to the underlying blockchain instance.
     *
     * @return the Neow3j instance.
     */
    public Neow3j getNeow3j() {
        return neow3j;
    }

    /**
     * Resumes the blockchain if it was stopped before.
     *
     * @throws Exception if resuming the blockchain failed.
     */
    public void resume() throws Exception {
        chain.resume();
    }

    /**
     * Halts the blockchain, i.e., stops block production.
     *
     * @throws Exception if halting the blockchain failed.
     */
    public void halt() throws Exception {
        chain.halt();
    }

    /**
     * Creates a new account and returns its Neo address.
     *
     * @return The account's address
     * @throws Exception if creating the account failed.
     */
    public Account createAccount() throws Exception {
        return new Account(ECKeyPair.create(
                hexStringToByteArray(chain.getAccount(chain.createAccount()))));
    }

    /**
     * Fast-forwards the blockchain state by {@code n} blocks. I.e., mints {@code n} empty blocks.
     * Can be used on a running or stopped node.
     *
     * @param n The number of blocks to mint.
     * @throws Exception if minting new blocks failed.
     */
    public void fastForward(int n) throws Exception {
        chain.fastForward(n);
    }

    /**
     * Gets the account for the given address if it exists on the blockchain.
     *
     * @param address The account's address.
     * @return The account.
     */
    public Account getAccount(String address) throws Exception {
        return new Account(ECKeyPair.create(hexStringToByteArray(
                chain.getAccount(address))));
    }

    /**
     * If the underlying test blockchain implementation has control over the genesis account, it
     * will be returned with all signer accounts.
     *
     * @return The genesis account's verification script and private keys.
     */
    public GenesisAccount getGenesisAccount() {
        try {
            TestBlockchain.GenesisAccount genAcc = chain.getGenesisAccount();
            Account multiSigAccount = Account.fromVerificationScript(
                    new VerificationScript(hexStringToByteArray(genAcc.getVerificationScript())));
            List<Account> signerAccounts = genAcc.getPrivateKeys().stream()
                    .map(k -> new Account(ECKeyPair.create(hexStringToByteArray(k))))
                    .collect(Collectors.toList());
            return new GenesisAccount(multiSigAccount, signerAccounts);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static class GenesisAccount {

        private Account multiSigAccount;
        private List<Account> signerAccounts;

        public GenesisAccount(Account multiSigAccount, List<Account> signerAccounts) {
            this.multiSigAccount = multiSigAccount;
            this.signerAccounts = signerAccounts;
        }

        public Account getMultiSigAccount() {
            return multiSigAccount;
        }

        public List<Account> getSignerAccounts() {
            return signerAccounts;
        }
    }

}
