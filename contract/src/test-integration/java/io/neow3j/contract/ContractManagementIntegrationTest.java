package io.neow3j.contract;

import io.neow3j.crypto.Base64;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.response.ContractManifest;
import io.neow3j.protocol.core.response.ContractState;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.test.NeoTestContainer;
import io.neow3j.types.Hash160;
import io.neow3j.types.Hash256;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.File;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.neow3j.contract.IntegrationTestHelper.COMMITTEE_ACCOUNT;
import static io.neow3j.contract.IntegrationTestHelper.COMMITTEE_WALLET;
import static io.neow3j.contract.SmartContract.calcContractHash;
import static io.neow3j.protocol.ObjectMapperFactory.getObjectMapper;
import static io.neow3j.transaction.AccountSigner.calledByEntry;
import static io.neow3j.utils.Await.waitUntilBlockCountIsGreaterThanZero;
import static io.neow3j.utils.Await.waitUntilTransactionIsExecuted;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ContractManagementIntegrationTest {

    private static Neow3j neow3j;
    private static ContractManagement contractManagement;

    private final static Path TESTCONTRACT_NEF_FILE = Paths.get("contracts", "TestContract.nef");
    private final static Path TESTCONTRACT_MANIFEST_FILE =
            Paths.get("contracts", "TestContract.manifest.json");

    @ClassRule
    public static NeoTestContainer neoTestContainer = new NeoTestContainer();

    @BeforeClass
    public static void setUp() {
        neow3j = Neow3j.build(new HttpService(neoTestContainer.getNodeUrl()));
        waitUntilBlockCountIsGreaterThanZero(neow3j);
        contractManagement = new ContractManagement(neow3j);
    }

    @Test
    public void testGetAndSetMinimumDeploymentFee() throws Throwable {
        BigInteger initialDeploymentFee = new BigInteger("1000000000");
        BigInteger minimumDeploymentFee = contractManagement.getMinimumDeploymentFee();

        assertThat(minimumDeploymentFee, is(initialDeploymentFee));

        BigInteger newDeploymentFee = new BigInteger("2000000000");
        Hash256 txHash = contractManagement
                .setMinimumDeploymentFee(newDeploymentFee)
                .wallet(COMMITTEE_WALLET)
                .signers(calledByEntry(COMMITTEE_ACCOUNT.getScriptHash()))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);

        minimumDeploymentFee = contractManagement.getMinimumDeploymentFee();
        assertThat(minimumDeploymentFee, is(newDeploymentFee));
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

        Hash256 txHash = contractManagement.deploy(nef, manifest)
                .wallet(COMMITTEE_WALLET)
                .signers(calledByEntry(COMMITTEE_ACCOUNT.getScriptHash()))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);
        Hash160 contractHash = calcContractHash(
                COMMITTEE_ACCOUNT.getScriptHash(), nef.getCheckSumAsInteger(), manifest.getName());
        ContractState contractState =
                neow3j.getContractState(contractHash).send().getContractState();
        assertThat(contractState.getManifest(), is(manifest));
        assertThat(contractState.getNef().getScript(), is(Base64.encode(nef.getScript())));
    }

}
