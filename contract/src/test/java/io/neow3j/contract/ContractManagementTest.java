package io.neow3j.contract;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.neow3j.contract.ContractTestHelper.setUpWireMockForCall;
import static io.neow3j.contract.ContractTestHelper.setUpWireMockForInvokeFunction;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.neow3j.model.types.ContractParameterType;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.methods.response.NeoGetContractState;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.transaction.Signer;
import io.neow3j.transaction.Transaction;
import io.neow3j.transaction.WitnessScope;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.Wallet;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class ContractManagementTest {

    private static final String CONTRACTMANAGEMENT_SCRIPTHASH =
            "a501d7d7d10983673b61b7a2d3a813b36f9f0e43";

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
                Arrays.asList(ContractParameter.integer(new BigInteger("70000000")))).toArray();

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

    @Test
    public void getContract() throws IOException {
        setUpWireMockForInvokeFunction("getContract",
                "management_getContract.json");

        ContractManagement contractManagement = new ContractManagement(neow3j);
        NeoGetContractState.ContractState state = contractManagement.getContract(
                NeoToken.SCRIPT_HASH);
        assertNotNull(state);
        assertThat(state.getId(), is(-1));
        assertThat(state.getUpdateCounter(), is(0));
        assertThat(state.getHash(), is(NeoToken.SCRIPT_HASH.toString()));
        assertThat(state.getScript(), is("0c084e656f546f6b656e411af77b67"));
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
}
