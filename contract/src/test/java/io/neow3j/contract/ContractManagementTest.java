package io.neow3j.contract;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.neow3j.contract.ContractParameter.byteArray;
import static io.neow3j.contract.ContractParameter.integer;
import static io.neow3j.contract.ContractParameter.string;
import static io.neow3j.contract.ContractTestHelper.setUpWireMockForCall;
import static io.neow3j.contract.ContractTestHelper.setUpWireMockForInvokeFunction;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.neow3j.model.types.ContractParameterType;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.ObjectMapperFactory;
import io.neow3j.protocol.core.methods.response.ContractManifest;
import io.neow3j.protocol.core.methods.response.NeoGetContractState;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.transaction.Signer;
import io.neow3j.transaction.Transaction;
import io.neow3j.transaction.WitnessScope;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.Wallet;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

public class ContractManagementTest {

    private static final String CONTRACTMANAGEMENT_SCRIPTHASH =
            "a501d7d7d10983673b61b7a2d3a813b36f9f0e43";

    private final static Path TESTCONTRACT_NEF_FILE = Paths.get("/contracts", "TestContract.nef");
    private final static Path TESTCONTRACT_MANIFEST_FILE =
            Paths.get("/contracts", "TestContract.manifest.json");

    private Neow3j neow3j;

    private Account account1;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().dynamicPort());

    @Before
    public void setUp() {
        // Configuring WireMock to use default host and the dynamic port set in WireMockRule.
        int port = wireMockRule.port();
        WireMock.configureFor(port);
        neow3j = Neow3j.build(new HttpService("http://127.0.0.1:" + port));
        neow3j.setNetworkMagicNumber(769);
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
                .signers(Signer.calledByEntry(account1.getScriptHash()))
                .sign();

        assertThat(tx.getSigners(), hasSize(1));
        assertThat(tx.getSigners().get(0).getScriptHash(), is(account1.getScriptHash()));
        assertThat(tx.getSigners().get(0).getScopes(), contains(WitnessScope.CALLED_BY_ENTRY));
        assertThat(tx.getScript(), is(expectedScript));
        assertThat(tx.getWitnesses().get(0).getVerificationScript().getScript(),
                is(account1.getVerificationScript().getScript()));
    }

    // TODO: 01.02.21 Guil:
    // Update the `ContractManagement` class
    @Ignore("The ContractManagement.getContract() method should be updated before.")
    @Test
    public void getContract() throws IOException {
        setUpWireMockForInvokeFunction("getContract",
                "management_getContract.json");

        ContractManagement contractManagement = new ContractManagement(neow3j);
        NeoGetContractState.ContractState state = contractManagement.getContract(
                NeoToken.SCRIPT_HASH);
        assertNotNull(state);
        assertThat(state.getId(), is(-3));
        assertThat(state.getUpdateCounter(), is(0));
        assertThat(state.getHash(), is(NeoToken.SCRIPT_HASH.toString()));

        assertThat(state.getNef().getMagic(), is(860243278L));
        assertThat(state.getNef().getCompiler(), is("neo-core-v3.0"));
        assertThat(state.getNef().getScript(), is(nullValue()));
        assertThat(state.getNef().getChecksum(), is(3921333105L));

        assertThat(state.getManifest().getName(), is(NeoToken.NAME));
        assertThat(state.getManifest().getAbi().getMethods(), hasSize(14));
        assertThat(state.getManifest().getAbi().getMethods().get(6).getName(), is("vote"));
        assertThat(state.getManifest().getAbi().getMethods().get(6).getParameters(), hasSize(2));
        assertThat(state.getManifest().getAbi().getMethods().get(6).getParameters().get(1)
                .getParamName(), is("voteTo"));
        assertThat(state.getManifest().getAbi().getMethods().get(6).getParameters().get(1)
                .getParamType(), is(ContractParameterType.BYTE_ARRAY));
        assertThat(state.getManifest().getAbi().getMethods().get(6).getOffset(), is(0));
        assertThat(state.getManifest().getAbi().getMethods().get(6).getReturnType(),
                is(ContractParameterType.BOOLEAN));
        assertFalse(state.getManifest().getAbi().getMethods().get(6).isSafe());

        assertThat(state.getManifest().getAbi().getEvents(), hasSize(1));
        assertThat(state.getManifest().getAbi().getEvents().get(0).getName(), is("Transfer"));
        assertThat(state.getManifest().getAbi().getEvents().get(0).getParameters().get(2)
                .getParamName(), is("amount"));
        assertThat(state.getManifest().getPermissions().get(0).getContract(), is("*"));
        assertThat(state.getManifest().getPermissions().get(0).getMethods(), hasSize(1));
        assertThat(state.getManifest().getPermissions().get(0).getMethods().get(0), is("*"));
        assertThat(state.getManifest().getTrusts(), hasSize(0));
        assertNull(state.getManifest().getExtra());
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
        File nefFile = new File(
                this.getClass().getResource(TESTCONTRACT_NEF_FILE.toString()).toURI());
        NefFile nef = NefFile.readFromFile(nefFile);

        File manifestFile = new File(this.getClass()
                .getResource(TESTCONTRACT_MANIFEST_FILE.toString()).toURI());
        ContractManifest manifest = ObjectMapperFactory.getObjectMapper()
                .readValue(manifestFile, ContractManifest.class);
        byte[] manifestBytes = ObjectMapperFactory.getObjectMapper().writeValueAsBytes(manifest);

        byte[] expectedScript = new ScriptBuilder().contractCall(
                ContractManagement.SCRIPT_HASH, "deploy",
                asList(byteArray(nef.toArray()), byteArray(manifestBytes))).toArray();

        Transaction tx = new ContractManagement(neow3j)
                .deploy(nef, manifest)
                .wallet(w)
                .signers(Signer.calledByEntry(account1.getScriptHash()))
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
        ContractManifest manifest = ObjectMapperFactory.getObjectMapper()
                .readValue(manifestFile, ContractManifest.class);
        byte[] manifestBytes = ObjectMapperFactory.getObjectMapper().writeValueAsBytes(manifest);

        ContractParameter data = string("some data");

        byte[] expectedScript = new ScriptBuilder().contractCall(
                ContractManagement.SCRIPT_HASH, "deploy", asList(
                        byteArray(nef.toArray()), byteArray(manifestBytes), data))
                .toArray();

        Transaction tx = new ContractManagement(neow3j)
                .deploy(nef, manifest, data)
                .wallet(w)
                .signers(Signer.calledByEntry(account1.getScriptHash()))
                .sign();

        assertThat(tx.getScript(), is(expectedScript));
    }

}
