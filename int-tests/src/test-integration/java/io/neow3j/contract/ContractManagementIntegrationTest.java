package io.neow3j.contract;

import io.neow3j.crypto.Base64;
import io.neow3j.helper.NeoNameServiceTestHelper;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.response.ContractManifest;
import io.neow3j.protocol.core.response.ContractState;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.test.NeoTestContainer;
import io.neow3j.transaction.Transaction;
import io.neow3j.transaction.Witness;
import io.neow3j.types.Hash160;
import io.neow3j.types.Hash256;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static io.neow3j.contract.IntegrationTestHelper.COMMITTEE_ACCOUNT;
import static io.neow3j.contract.IntegrationTestHelper.DEFAULT_ACCOUNT;
import static io.neow3j.contract.IntegrationTestHelper.GAS_HASH;
import static io.neow3j.contract.IntegrationTestHelper.NEO_HASH;
import static io.neow3j.contract.SmartContract.calcContractHash;
import static io.neow3j.crypto.Sign.signMessage;
import static io.neow3j.protocol.ObjectMapperFactory.getObjectMapper;
import static io.neow3j.transaction.AccountSigner.calledByEntry;
import static io.neow3j.transaction.Witness.createMultiSigWitness;
import static io.neow3j.utils.Await.waitUntilBlockCountIsGreaterThanZero;
import static io.neow3j.utils.Await.waitUntilTransactionIsExecuted;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
public class ContractManagementIntegrationTest {

    private static final String RESOURCE_DIR = "contract";
    private final static Path TESTCONTRACT_NEF_FILE = Paths.get(RESOURCE_DIR, "contracts", "TestContract.nef");
    private final static Path TESTCONTRACT_MANIFEST_FILE = Paths.get(RESOURCE_DIR, "contracts",
            "TestContract.manifest.json");

    private static Neow3j neow3j;
    private static ContractManagement contractManagement;

    @Container
    public static NeoTestContainer neoTestContainer = new NeoTestContainer();

    @BeforeAll
    public static void setUp() throws Throwable {
        neow3j = Neow3j.build(new HttpService(neoTestContainer.getNodeUrl()));
        waitUntilBlockCountIsGreaterThanZero(neow3j);
        contractManagement = new ContractManagement(neow3j);
        NeoNameServiceTestHelper.deployNNS(neow3j, COMMITTEE_ACCOUNT, DEFAULT_ACCOUNT);
    }

    @Test
    public void testGetAndSetMinimumDeploymentFee() throws Throwable {
        BigInteger initialDeploymentFee = new BigInteger("1000000000");
        BigInteger minimumDeploymentFee = contractManagement.getMinimumDeploymentFee();

        assertThat(minimumDeploymentFee, is(initialDeploymentFee));

        BigInteger newDeploymentFee = new BigInteger("2000000000");

        Transaction tx = contractManagement
                .setMinimumDeploymentFee(newDeploymentFee)
                .signers(calledByEntry(COMMITTEE_ACCOUNT))
                .getUnsignedTransaction();

        Witness multiSigWitness = createMultiSigWitness(
                asList(signMessage(tx.getHashData(), DEFAULT_ACCOUNT.getECKeyPair())),
                COMMITTEE_ACCOUNT.getVerificationScript());

        Hash256 txHash = tx.addWitness(multiSigWitness)
                .send()
                .getSendRawTransaction()
                .getHash();

        waitUntilTransactionIsExecuted(txHash, neow3j);

        minimumDeploymentFee = contractManagement.getMinimumDeploymentFee();
        assertThat(minimumDeploymentFee, is(newDeploymentFee));
    }

    @Test
    public void testGetContract() throws IOException {
        ContractState contract = contractManagement.getContract(NeoToken.SCRIPT_HASH);
        assertThat(contract.getManifest().getName(), is(NeoToken.NAME));
    }

    @Test
    public void testGetContractById() throws IOException {
        ContractState contract = contractManagement.getContractById(1);
        assertThat(contract.getManifest().getName(), is("NameService"));
    }

    @Test
    public void testGetContractHashes() throws IOException {
        Iterator<ContractState.ContractIdentifiers> it = contractManagement.getContractHashes();
        List<ContractState.ContractIdentifiers> identifiers = it.traverse(2);

        assertThat(identifiers.size(), is(1));
        assertThat(identifiers.get(0).getHash(), is(neow3j.getNNSResolver()));
        assertThat(identifiers.get(0).getId(), is(BigInteger.ONE));
    }

    @Test
    public void testGetContractHashesUnwrapped() throws IOException {
        List<ContractState.ContractIdentifiers> list = contractManagement.getContractHashesUnwrapped();

        assertThat(list.size(), is(1));
        ContractState.ContractIdentifiers expected = new ContractState.ContractIdentifiers(BigInteger.ONE,
                neow3j.getNNSResolver());

        assertThat(list.get(0), is(expected));
        assertThat(list.get(0).getId(), is(BigInteger.ONE));
        assertThat(list.get(0).getHash(), is(neow3j.getNNSResolver()));
    }

    @Test
    public void testDeploy() throws Throwable {
        File nefFile = new File(getClass().getClassLoader()
                .getResource(TESTCONTRACT_NEF_FILE.toString()).toURI());
        NefFile nef = NefFile.readFromFile(nefFile);

        File manifestFile = new File(getClass().getClassLoader()
                .getResource(TESTCONTRACT_MANIFEST_FILE.toString()).toURI());
        ContractManifest manifest = getObjectMapper()
                .readValue(manifestFile, ContractManifest.class);

        Transaction tx = contractManagement.deploy(nef, manifest)
                .signers(calledByEntry(COMMITTEE_ACCOUNT))
                .getUnsignedTransaction();
        Witness multiSigWitness = createMultiSigWitness(
                asList(signMessage(tx.getHashData(), DEFAULT_ACCOUNT.getECKeyPair())),
                COMMITTEE_ACCOUNT.getVerificationScript());
        Hash256 txHash = tx.addWitness(multiSigWitness).send().getSendRawTransaction().getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);

        Hash160 contractHash = calcContractHash(
                COMMITTEE_ACCOUNT.getScriptHash(), nef.getCheckSumAsInteger(), manifest.getName());
        ContractState contractState =
                neow3j.getContractState(contractHash).send().getContractState();
        assertThat(contractState.getManifest(), is(manifest));
        assertThat(contractState.getNef().getScript(), is(Base64.encode(nef.getScript())));
    }

    @Test
    public void testHasMethod() throws IOException {
        assertTrue(contractManagement.hasMethod(GAS_HASH, "transfer", 4));
        assertTrue(contractManagement.hasMethod(NEO_HASH, "getAccountState", 1));
        assertFalse(contractManagement.hasMethod(NEO_HASH, "mint", 3));
    }

}
