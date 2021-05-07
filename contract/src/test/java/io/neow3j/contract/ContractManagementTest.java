package io.neow3j.contract;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.neow3j.io.exceptions.DeserializationException;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.Neow3jConfig;
import io.neow3j.protocol.core.methods.response.ContractManifest;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.transaction.Transaction;
import io.neow3j.transaction.WitnessScope;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.Wallet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.neow3j.constants.NeoConstants.MAX_MANIFEST_SIZE;
import static io.neow3j.contract.ContractParameter.byteArray;
import static io.neow3j.contract.ContractParameter.integer;
import static io.neow3j.contract.ContractParameter.string;
import static io.neow3j.contract.ContractTestHelper.setUpWireMockForCall;
import static io.neow3j.contract.ContractTestHelper.setUpWireMockForInvokeFunction;
import static io.neow3j.protocol.ObjectMapperFactory.getObjectMapper;
import static io.neow3j.transaction.Signer.calledByEntry;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ContractManagementTest {

    private static final String CONTRACTMANAGEMENT_SCRIPTHASH =
            "fffdc93764dbaddd97c48f252a53ea4643faa3fd";

    private final static Path TESTCONTRACT_NEF_FILE = Paths.get("/contracts", "TestContract.nef");
    private final static Path TESTCONTRACT_MANIFEST_FILE =
            Paths.get("/contracts", "TestContract.manifest.json");

    private Neow3j neow3j;

    private Account account1;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().dynamicPort());

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Before
    public void setUp() {
        // Configuring WireMock to use default host and the dynamic port set in WireMockRule.
        int port = wireMockRule.port();
        WireMock.configureFor(port);
        neow3j = Neow3j.build(new HttpService("http://127.0.0.1:" + port),
                new Neow3jConfig().setNetworkMagic(769));
        account1 = Account.fromWIF("L1WMhxazScMhUrdv34JqQb1HFSQmWeN2Kpc1R9JGKwL7CDNP21uR");
    }

    @Test
    public void testGetMinimumDeploymentFee() throws IOException {
        setUpWireMockForInvokeFunction("getMinimumDeploymentFee",
                "management_getMinimumDeploymentFee.json");

        ContractManagement contractManagement = new ContractManagement(neow3j);
        assertThat(contractManagement.getMinimumDeploymentFee(), is(new BigInteger("1000000000")));
    }

    @Test
    public void testSetMinimumDeploymentFee() throws Throwable {
        setUpWireMockForCall("invokescript", "management_setMinimumDeploymentFee.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");
        setUpWireMockForCall("calculatenetworkfee", "calculatenetworkfee.json");

        byte[] expectedScript = new ScriptBuilder().contractCall(ContractManagement.SCRIPT_HASH,
                "setMinimumDeploymentFee",
                singletonList(integer(new BigInteger("70000000")))).toArray();

        Wallet w = Wallet.withAccounts(account1);
        Transaction tx = new ContractManagement(neow3j)
                .setMinimumDeploymentFee(new BigInteger("70000000"))
                .wallet(w)
                .signers(calledByEntry(account1.getScriptHash()))
                .sign();

        assertThat(tx.getSigners(), hasSize(1));
        assertThat(tx.getSigners().get(0).getScriptHash(), is(account1.getScriptHash()));
        assertThat(tx.getSigners().get(0).getScopes(), contains(WitnessScope.CALLED_BY_ENTRY));
        assertThat(tx.getScript(), is(expectedScript));
        assertThat(tx.getWitnesses().get(0).getVerificationScript().getScript(),
                is(account1.getVerificationScript().getScript()));
    }

    @Test
    public void scriptHash() {
        assertThat(new ContractManagement(neow3j).getScriptHash().toString(),
                is(CONTRACTMANAGEMENT_SCRIPTHASH));
    }

    @Test
    public void deployWithoutData() throws Throwable {
        setUpWireMockForCall("invokescript", "management_deploy.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");
        setUpWireMockForCall("calculatenetworkfee", "calculatenetworkfee.json");

        Wallet w = Wallet.withAccounts(account1);
        File nefFile = new File(getClass().getResource(TESTCONTRACT_NEF_FILE.toString()).toURI());
        NefFile nef = NefFile.readFromFile(nefFile);

        File manifestFile = new File(
                getClass().getResource(TESTCONTRACT_MANIFEST_FILE.toString()).toURI());
        ContractManifest manifest =
                getObjectMapper().readValue(manifestFile, ContractManifest.class);
        byte[] manifestBytes = getObjectMapper().writeValueAsBytes(manifest);

        byte[] expectedScript = new ScriptBuilder().contractCall(
                ContractManagement.SCRIPT_HASH, "deploy",
                asList(byteArray(nef.toArray()), byteArray(manifestBytes))).toArray();

        Transaction tx = new ContractManagement(neow3j)
                .deploy(nef, manifest)
                .wallet(w)
                .signers(calledByEntry(account1.getScriptHash()))
                .sign();

        assertThat(tx.getScript(), is(expectedScript));
    }

    @Test
    public void deployWithData() throws Throwable {
        setUpWireMockForCall("invokescript", "management_deploy.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");
        setUpWireMockForCall("calculatenetworkfee", "calculatenetworkfee.json");

        Wallet w = Wallet.withAccounts(account1);
        File nefFile = new File(
                this.getClass().getResource(TESTCONTRACT_NEF_FILE.toString()).toURI());
        NefFile nef = NefFile.readFromFile(nefFile);

        File manifestFile = new File(this.getClass()
                .getResource(TESTCONTRACT_MANIFEST_FILE.toString()).toURI());
        ContractManifest manifest = getObjectMapper()
                .readValue(manifestFile, ContractManifest.class);
        byte[] manifestBytes = getObjectMapper().writeValueAsBytes(manifest);

        ContractParameter data = string("some data");

        byte[] expectedScript = new ScriptBuilder().contractCall(
                ContractManagement.SCRIPT_HASH, "deploy", asList(
                        byteArray(nef.toArray()), byteArray(manifestBytes), data))
                .toArray();

        Transaction tx = new ContractManagement(neow3j)
                .deploy(nef, manifest, data)
                .wallet(w)
                .signers(calledByEntry(account1.getScriptHash()))
                .sign();

        assertThat(tx.getScript(), is(expectedScript));
    }

    @Test
    public void deploy_NefNull() throws JsonProcessingException {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("The NEF file cannot be null.");
        new ContractManagement(neow3j)
                .deploy(null, null, null);
    }

    @Test
    public void deploy_manifestNull() throws IOException, DeserializationException,
            URISyntaxException {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("The manifest cannot be null.");
        File nefFile = new File(
                this.getClass().getResource(TESTCONTRACT_NEF_FILE.toString()).toURI());
        NefFile nef = NefFile.readFromFile(nefFile);
        new ContractManagement(neow3j)
                .deploy(nef, null, null);
    }

    @Test
    public void deploy_manifestTooLong() throws IOException, URISyntaxException,
            DeserializationException {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("The given contract manifest is too long.");

        File nefFile = new File(
                this.getClass().getResource(TESTCONTRACT_NEF_FILE.toString()).toURI());
        NefFile nef = NefFile.readFromFile(nefFile);

        ContractManifest tooBigManifest = getTooBigManifest();

        new ContractManagement(neow3j).deploy(nef, tooBigManifest);
    }

    // Creates a ContractManifest that is one byte to long for deployment
    private ContractManifest getTooBigManifest() {
        String partialName = "a"; // 1 bytes
        StringBuilder stringBuilder = new StringBuilder();
        // manifest with all null values is of size 108 bytes itself
        for (int i = 0; i < MAX_MANIFEST_SIZE - 107; i++) {
            stringBuilder.append(partialName);
        }
        String namePlaceholder = stringBuilder.toString();
        return new ContractManifest(namePlaceholder, null, null, null, null, null, null, null);
    }

}
