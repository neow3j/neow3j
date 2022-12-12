package io.neow3j.contract;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.Neow3jConfig;
import io.neow3j.protocol.core.response.ContractManifest;
import io.neow3j.protocol.core.response.ContractState;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.script.ScriptBuilder;
import io.neow3j.serialization.exceptions.DeserializationException;
import io.neow3j.transaction.Transaction;
import io.neow3j.transaction.WitnessScope;
import io.neow3j.types.ContractParameter;
import io.neow3j.types.Hash160;
import io.neow3j.wallet.Account;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static io.neow3j.constants.NeoConstants.MAX_MANIFEST_SIZE;
import static io.neow3j.protocol.ObjectMapperFactory.getObjectMapper;
import static io.neow3j.test.WireMockTestHelper.setUpWireMockForCall;
import static io.neow3j.test.WireMockTestHelper.setUpWireMockForInvokeFunction;
import static io.neow3j.transaction.AccountSigner.calledByEntry;
import static io.neow3j.types.ContractParameter.byteArray;
import static io.neow3j.types.ContractParameter.integer;
import static io.neow3j.types.ContractParameter.string;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ContractManagementTest {

    private static final Hash160 CONTRACTMANAGEMENT_SCRIPTHASH =
            new Hash160("fffdc93764dbaddd97c48f252a53ea4643faa3fd");

    private final static Path TESTCONTRACT_NEF_FILE = Paths.get("contracts", "TestContract.nef");
    private final static Path TESTCONTRACT_MANIFEST_FILE =
            Paths.get("contracts", "TestContract.manifest.json");

    private Neow3j neow3j;

    private Account account1;

    @RegisterExtension
    static WireMockExtension wireMockExtension = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @BeforeAll
    public void setUp() {
        // Configuring WireMock to use default host and the dynamic port set in WireMockRule.
        int port = wireMockExtension.getPort();
        WireMock.configureFor(port);
        neow3j = Neow3j.build(new HttpService("http://127.0.0.1:" + port), new Neow3jConfig().setNetworkMagic(769));
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

        Transaction tx = new ContractManagement(neow3j)
                .setMinimumDeploymentFee(new BigInteger("70000000"))
                .signers(calledByEntry(account1))
                .sign();

        assertThat(tx.getSigners(), hasSize(1));
        assertThat(tx.getSigners().get(0).getScriptHash(), is(account1.getScriptHash()));
        assertThat(tx.getSigners().get(0).getScopes(), contains(WitnessScope.CALLED_BY_ENTRY));
        assertThat(tx.getScript(), is(expectedScript));
        assertThat(tx.getWitnesses().get(0).getVerificationScript().getScript(),
                is(account1.getVerificationScript().getScript()));
    }

    @Test
    public void testGetContract() throws IOException {
        setUpWireMockForCall("getcontractstate", "contractstate.json");

        Hash160 contractHash = new Hash160("0xf61eebf573ea36593fd43aa150c055ad7906ab83");
        ContractState state = new ContractManagement(neow3j).getContract(contractHash);

        assertThat(state.getHash(), is(contractHash));
        assertThat(state.getManifest().getName(), is("neow3j"));
    }

    @Test
    public void testGetContractById() throws IOException {
        setUpWireMockForInvokeFunction("getContractById", "management_getContract.json");
        setUpWireMockForCall("getcontractstate", "contractstate.json");

        Hash160 contractHash = new Hash160("0xf61eebf573ea36593fd43aa150c055ad7906ab83");
        ContractState state = new ContractManagement(neow3j).getContractById(12);

        assertThat(state.getHash(), is(contractHash));
        assertThat(state.getId(), is(12));
        assertThat(state.getManifest().getName(), is("neow3j"));
    }

    @Test
    public void testGetContractById_nonExistent() throws IOException {
        setUpWireMockForCall("invokefunction", "management_contractstate_notexistent.json");

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> new ContractManagement(neow3j).getContractById(20));
        assertThat(thrown.getMessage(), is("Could not get the contract hash for the provided id."));
    }

    @Test
    public void testGetContractHashes() throws IOException {
        setUpWireMockForCall("invokefunction", "invokefunction_iterator_session.json");

        Iterator<ContractState.ContractIdentifiers> it = new ContractManagement(neow3j).getContractHashes();
        assertThat(it.getSessionId(), is("a7b35b13-bdfc-4ab3-a398-88a9db9da4fe"));
        assertThat(it.getIteratorId(), is("190d19ca-e935-4ad0-95c9-93b8cf6d115c"));
    }

    @Test
    public void testGetContractHashesUnwrapped() throws IOException {
        setUpWireMockForCall("invokescript", "management_hashes_unwrapped.json");

        List<ContractState.ContractIdentifiers> list = new ContractManagement(neow3j).getContractHashesUnwrapped();
        assertThat(list.size(), is(2));
        assertThat(list.get(0).getId(), is(BigInteger.ONE));
        assertThat(list.get(0).getHash(), is(new Hash160("5947de99c264d7caa81a7ece81ec98661ecc2c73")));
        ContractState.ContractIdentifiers expected = new ContractState.ContractIdentifiers(BigInteger.valueOf(2),
                new Hash160("3bbf19136b9a7f65b76e3f36b8c137f07d0fa4de"));
        assertEquals(expected, list.get(1));
    }

    @Test
    public void testHasMethod() throws IOException {
        setUpWireMockForInvokeFunction("hasMethod", "invocationresult_boolean_true.json");

        ContractManagement contractManagement = new ContractManagement(neow3j);
        assertTrue(contractManagement.hasMethod(NeoToken.SCRIPT_HASH, "symbol", 0));
    }

    @Test
    public void scriptHash() {
        assertThat(new ContractManagement(neow3j).getScriptHash(),
                is(CONTRACTMANAGEMENT_SCRIPTHASH));
    }

    @Test
    public void deployWithoutData() throws Throwable {
        setUpWireMockForCall("invokescript", "management_deploy.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");
        setUpWireMockForCall("calculatenetworkfee", "calculatenetworkfee.json");

        File nefFile = new File(getClass().getClassLoader()
                .getResource(TESTCONTRACT_NEF_FILE.toString()).toURI());
        NefFile nef = NefFile.readFromFile(nefFile);

        File manifestFile = new File(getClass().getClassLoader()
                .getResource(TESTCONTRACT_MANIFEST_FILE.toString()).toURI());
        ContractManifest manifest =
                getObjectMapper().readValue(manifestFile, ContractManifest.class);
        byte[] manifestBytes = getObjectMapper().writeValueAsBytes(manifest);

        byte[] expectedScript = new ScriptBuilder().contractCall(
                ContractManagement.SCRIPT_HASH, "deploy",
                asList(byteArray(nef.toArray()), byteArray(manifestBytes))).toArray();

        Transaction tx = new ContractManagement(neow3j)
                .deploy(nef, manifest)
                .signers(calledByEntry(account1))
                .sign();

        assertThat(tx.getScript(), is(expectedScript));
    }

    @Test
    public void deployWithData() throws Throwable {
        setUpWireMockForCall("invokescript", "management_deploy.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");
        setUpWireMockForCall("calculatenetworkfee", "calculatenetworkfee.json");

        File nefFile = new File(this.getClass().getClassLoader()
                .getResource(TESTCONTRACT_NEF_FILE.toString()).toURI());
        NefFile nef = NefFile.readFromFile(nefFile);

        File manifestFile = new File(this.getClass().getClassLoader()
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
                .signers(calledByEntry(account1))
                .sign();

        assertThat(tx.getScript(), is(expectedScript));
    }

    @Test
    public void deploy_NefNull() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> new ContractManagement(neow3j).deploy(null, null, null));
        assertThat(thrown.getMessage(), is("The NEF file cannot be null."));
    }

    @Test
    public void deploy_manifestNull() throws IOException, DeserializationException, URISyntaxException {
        File nefFile = new File(this.getClass().getClassLoader().getResource(TESTCONTRACT_NEF_FILE.toString()).toURI());
        NefFile nef = NefFile.readFromFile(nefFile);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> new ContractManagement(neow3j).deploy(nef, null, null));
        assertThat(thrown.getMessage(), is("The manifest cannot be null."));
    }

    @Test
    public void deploy_manifestTooLong() throws IOException, URISyntaxException, DeserializationException {
        File nefFile = new File(this.getClass().getClassLoader().getResource(TESTCONTRACT_NEF_FILE.toString()).toURI());
        NefFile nef = NefFile.readFromFile(nefFile);

        ContractManifest tooBigManifest = getTooBigManifest();

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> new ContractManagement(neow3j).deploy(nef, tooBigManifest));
        assertThat(thrown.getMessage(), containsString("The given contract manifest is too long."));
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
