package io.neow3j.contract;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.neow3j.contract.ContractTestHelper.setUpWireMockForCall;
import static io.neow3j.contract.ContractTestHelper.setUpWireMockForInvokeFunction;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.transaction.Transaction;
import io.neow3j.transaction.WitnessScope;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.Wallet;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class PolicyContractTest {

    private static final ScriptHash POLICY_SCRIPT_HASH = PolicyContract.SCRIPT_HASH;
    private static final String SET_FEE_PER_BYTE = "setFeePerByte";
    private static final String SET_MAX_TX_PER_BLOCK = "setMaxTransactionsPerBlock";
    private static final String BLOCK_ACCOUNT = "blockAccount";
    private static final String UNBLOCK_ACCOUNT = "unblockAccount";

    private Neow3j neow3j;

    private Account account1;
    private ScriptHash recipient;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().dynamicPort());

    @Before
    public void setUp() {
        // Configuring WireMock to use default host and the dynamic port set in WireMockRule.
        int port = this.wireMockRule.port();
        WireMock.configureFor(port);
        neow3j = Neow3j.build(new HttpService("http://127.0.0.1:" + port));

        account1 = Account.fromWIF("L1WMhxazScMhUrdv34JqQb1HFSQmWeN2Kpc1R9JGKwL7CDNP21uR");
        recipient = new ScriptHash("969a77db482f74ce27105f760efa139223431394");
    }

    @Test
    public void getMaxTransactionsPerBlock() throws IOException {
        setUpWireMockForInvokeFunction("getMaxTransactionsPerBlock",
                "policy_getMaxTxPerBlock.json");
        PolicyContract policyContract = new PolicyContract(this.neow3j);
        assertThat(policyContract.getMaxTransactionsPerBlock(), is(512));
    }

    @Test
    public void getFeePerByte() throws IOException {
        setUpWireMockForInvokeFunction("getFeePerByte", "policy_getFeePerByte.json");

        PolicyContract policyContract = new PolicyContract(this.neow3j);
        assertThat(policyContract.getFeePerByte(), is(1000));
    }

    @Test
    public void getBlockedAccounts_emptyList() throws IOException {
        setUpWireMockForInvokeFunction("getBlockedAccounts",
                "policy_getBlockedAccounts_empty.json");

        PolicyContract policyContract = new PolicyContract(this.neow3j);
        assertNotNull(policyContract.getBlockedAccounts());
        assertThat(policyContract.getBlockedAccounts().size(), is(0));
    }

    @Test
    public void getBlockedAccounts() throws IOException {
        setUpWireMockForInvokeFunction("getBlockedAccounts",
                "policy_getBlockedAccounts.json");

        PolicyContract policyContract = new PolicyContract(this.neow3j);
        List<ScriptHash> blockedAccounts = policyContract.getBlockedAccounts();

        assertNotNull(blockedAccounts);
        assertThat(blockedAccounts.size(), is(2));
        assertThat(blockedAccounts, contains(
                ScriptHash.fromAddress("Aa1rZbE1k8fXTwzaxxsPRtJYPwhDQjWRFZ"),
                ScriptHash.fromAddress("ATpVyfpFwE2SzNGSvXDNrtRyfVLajhn7yN")));
    }

    @Test
    public void settingFeePerByteProducesCorrectTransaction() throws Throwable {
        setUpWireMockForCall("invokescript", "policy_setFeePerByte.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");

        byte[] expectedScript = new ScriptBuilder().contractCall(POLICY_SCRIPT_HASH,
                SET_FEE_PER_BYTE, Arrays.asList(ContractParameter.integer(20))).toArray();

        Wallet w = Wallet.withAccounts(account1);
        Transaction tx = new PolicyContract(neow3j)
                .buildSetFeePerByteInvocation(20, w, account1.getScriptHash())
                .sign();

        assertThat(tx.getSigners(), hasSize(1));
        assertThat(tx.getSigners().get(0).getScriptHash(), is(account1.getScriptHash()));
        assertThat(tx.getSigners().get(0).getScopes(), contains(WitnessScope.CALLED_BY_ENTRY));
        assertThat(tx.getScript(), is(expectedScript));
        assertThat(tx.getWitnesses().get(0).getVerificationScript().getScript(),
                is(account1.getVerificationScript().getScript()));
    }

    @Test
    public void setMaxTxPerBlockProducesCorrectTransaction() throws Throwable {
        setUpWireMockForCall("invokescript", "policy_setMaxTransactionsPerBlock.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");

        byte[] expectedScript = new ScriptBuilder().contractCall(POLICY_SCRIPT_HASH,
                SET_MAX_TX_PER_BLOCK, Arrays.asList(ContractParameter.integer(500))).toArray();

        Wallet w = Wallet.withAccounts(account1);
        Transaction tx = new PolicyContract(neow3j)
                .buildSetMaxTxPerBlockInvocation(500, w, account1.getScriptHash())
                .sign();

        assertThat(tx.getSigners(), hasSize(1));
        assertThat(tx.getSigners().get(0).getScriptHash(), is(account1.getScriptHash()));
        assertThat(tx.getSigners().get(0).getScopes(), contains(WitnessScope.CALLED_BY_ENTRY));
        assertThat(tx.getScript(), is(expectedScript));
        assertThat(tx.getWitnesses().get(0).getVerificationScript().getScript(),
                is(account1.getVerificationScript().getScript()));
    }

    @Test
    public void testBlockAccount() throws Throwable {
        setUpWireMockForCall("invokescript", "policy_blockAccount.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");

        byte[] expectedScript = new ScriptBuilder().contractCall(POLICY_SCRIPT_HASH, BLOCK_ACCOUNT,
                Arrays.asList(ContractParameter.hash160(recipient))).toArray();

        Wallet w = Wallet.withAccounts(account1);
        Transaction tx = new PolicyContract(neow3j)
                .buildBlockAccountInvocation(recipient, w, account1.getScriptHash())
                .sign();

        assertThat(tx.getSigners(), hasSize(1));
        assertThat(tx.getSigners().get(0).getScriptHash(), is(account1.getScriptHash()));
        assertThat(tx.getSigners().get(0).getScopes(), contains(WitnessScope.CALLED_BY_ENTRY));
        assertThat(tx.getScript(), is(expectedScript));
        assertThat(tx.getWitnesses().get(0).getVerificationScript().getScript(),
                is(account1.getVerificationScript().getScript()));
    }

    @Test
    public void testUnblockAccount() throws Throwable {
        setUpWireMockForCall("invokescript", "policy_unblockAccount.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");

        byte[] expectedScript = new ScriptBuilder().contractCall(POLICY_SCRIPT_HASH,
                UNBLOCK_ACCOUNT, Arrays.asList(ContractParameter.hash160(recipient))).toArray();

        Wallet w = Wallet.withAccounts(account1);
        Transaction tx = new PolicyContract(neow3j)
                .buildUnblockAccountInvocation(recipient, w, account1.getScriptHash())
                .sign();

        assertThat(tx.getSigners(), hasSize(1));
        assertThat(tx.getSigners().get(0).getScriptHash(), is(account1.getScriptHash()));
        assertThat(tx.getSigners().get(0).getScopes(), contains(WitnessScope.CALLED_BY_ENTRY));
        assertThat(tx.getScript(), is(expectedScript));
        assertThat(tx.getWitnesses().get(0).getVerificationScript().getScript(),
                is(account1.getVerificationScript().getScript()));
    }
}
