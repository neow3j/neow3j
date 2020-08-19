package io.neow3j.contract;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import io.neow3j.constants.InteropServiceCode;
import static io.neow3j.contract.ContractTestHelper.setUpWireMockForCall;
import static io.neow3j.contract.ContractTestHelper.setUpWireMockForInvokeFunction;
import io.neow3j.transaction.WitnessScope;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.transaction.Signer;
import io.neow3j.transaction.Transaction;
import io.neow3j.utils.Numeric;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.Wallet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class PolicyContractTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().dynamicPort());

    private Neow3j neow3j;
    private Wallet consensusWallet;
    private byte[] verificationScript;
//    AViY58NUkBaxzxZ1Pq6QjvCsWs9NE7guMH -> multisig
//    AGM8niYHxiEwShDenzKSnf499zzxbXJAmc -> default
    private static final ScriptHash CONSENSUS_MULTISIG_SCRIPT_HASH =
            new ScriptHash("1fbb6ff8b41bb08b79800c0126d882d1222cf098");
    private static final String CONSENSUS_MULTISIG_ADDRESS = "AViY58NUkBaxzxZ1Pq6QjvCsWs9NE7guMH";
//    private static final byte[] VERIFICATION_SCRIPT = ScriptBuilder.buildVerificationScript()
//            Numeric.hexStringToByteArray("110c2102c0b60c995bc092e866f15a37c176bb59b7ebacf069ba94c0ebf561cb8f956238110b41c330181e");

    @Before
    public void setUp() {
        // Configuring WireMock to use default host and the dynamic port set in WireMockRule.
        int port = this.wireMockRule.port();
        WireMock.configureFor(port);
        neow3j = Neow3j.build(new HttpService("http://127.0.0.1:" + port));

        // Configuring wallet to invoke policy contract
        ECKeyPair ecKeyPair = ECKeyPair.create(
                Numeric.hexStringToByteArray("e6e919577dd7b8e97805151c05ae07ff4f752654d6d8797597aca989c02c4cb3"));
        Account acct = new Account(ecKeyPair);
        Account multiSigAccount = Account.createMultiSigAccount(
                Arrays.asList(acct.getECKeyPair().getPublicKey()), 1);
        consensusWallet = Wallet.withAccounts(acct, multiSigAccount);
        verificationScript = multiSigAccount.getVerificationScript().getScript();
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
    public void testSetFeePerByte() throws IOException {
        byte[] expectedScript = new ScriptBuilder()
                .pushInteger(20)
                .pushInteger(1)
                .pack()
                .pushData(Numeric.hexStringToByteArray("73657446656550657242797465"))
                .pushData(Numeric.hexStringToByteArray("e9ff4ca7cc252e1dfddb26315869cd79505906ce"))
                .sysCall(InteropServiceCode.SYSTEM_CONTRACT_CALL)
                .toArray();
        setUpWireMockForCall("invokescript", "policy_setFeePerByte.json",
                Numeric.toHexStringNoPrefix(expectedScript));
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");

        Invocation inv = new PolicyContract(neow3j)
                .buildSetFeePerByteInvocation(20, consensusWallet, Signer.global(CONSENSUS_MULTISIG_SCRIPT_HASH));
        Transaction tx = inv.getTransaction();
        assertThat(tx.getSender().getScriptHash().toAddress(), is(CONSENSUS_MULTISIG_ADDRESS));
        assertThat(tx.getSystemFee(), is(4007420L));
        assertThat(tx.getNetworkFee(), is(1205450L));
        assertThat(tx.getSigners(), hasSize(1));
        assertThat(tx.getSigners().get(0).getScriptHash(), is(CONSENSUS_MULTISIG_SCRIPT_HASH));
        assertThat(tx.getSigners().get(0).getScopes(), contains(WitnessScope.GLOBAL));
        assertThat(tx.getScript(), is(expectedScript));

        assertThat(tx.getWitnesses().get(0).getVerificationScript().getScript(), is(verificationScript));
    }

    @Test
    public void testSetMaxTxPerBlock() throws IOException {
        byte[] expectedScript = new ScriptBuilder()
                .pushInteger(500)
                .pushInteger(1)
                .pack()
                .pushData(Numeric.hexStringToByteArray("7365744d61785472616e73616374696f6e73506572426c6f636b"))
                .pushData(Numeric.hexStringToByteArray("e9ff4ca7cc252e1dfddb26315869cd79505906ce"))
                .sysCall(InteropServiceCode.SYSTEM_CONTRACT_CALL)
                .toArray();
        setUpWireMockForCall("invokescript", "policy_setMaxTransactionsPerBlock.json",
                Numeric.toHexStringNoPrefix(expectedScript));
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");

        Invocation inv = new PolicyContract(neow3j)
                .buildSetMaxTxPerBlockInvocation(500, consensusWallet,
                        Signer.calledByEntry(CONSENSUS_MULTISIG_SCRIPT_HASH));
        Transaction tx = inv.getTransaction();
        assertThat(tx.getSender().getScriptHash().toAddress(), is(CONSENSUS_MULTISIG_ADDRESS));
        assertThat(tx.getSystemFee(), is(4007420L));
        assertThat(tx.getNetworkFee(), is(1219450L));
        assertThat(tx.getSigners(), hasSize(1));
        assertThat(tx.getSigners().get(0).getScriptHash(), is(CONSENSUS_MULTISIG_SCRIPT_HASH));
        assertThat(tx.getSigners().get(0).getScopes(), contains(WitnessScope.CALLED_BY_ENTRY));
        assertThat(tx.getScript(), is(expectedScript));
        assertThat(tx.getWitnesses().get(0).getVerificationScript().getScript(), is(verificationScript));
    }

    @Test
    public void testBlockAccount() throws IOException {
        byte[] expectedScript = new ScriptBuilder()
                .pushData(Numeric.hexStringToByteArray("c8172ea3b405bf8bfc57c33a8410116b843e13df"))
                .pushInteger(1)
                .pack()
                .pushData(Numeric.hexStringToByteArray("626c6f636b4163636f756e74"))
                .pushData(Numeric.hexStringToByteArray("e9ff4ca7cc252e1dfddb26315869cd79505906ce"))
                .sysCall(InteropServiceCode.SYSTEM_CONTRACT_CALL)
                .toArray();
        setUpWireMockForCall("invokescript", "policy_blockAccount.json",
                Numeric.toHexStringNoPrefix(expectedScript));
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");

        Invocation inv = new PolicyContract(neow3j)
                .buildBlockAccountInvocation(ScriptHash.fromAddress("Aa1rZbE1k8fXTwzaxxsPRtJYPwhDQjWRFZ"),
                        consensusWallet, Signer.calledByEntry(CONSENSUS_MULTISIG_SCRIPT_HASH));
        Transaction tx = inv.getTransaction();
        assertThat(tx.getSender().getScriptHash().toAddress(), is(CONSENSUS_MULTISIG_ADDRESS));
        assertThat(tx.getSystemFee(), is(4007570L));
        assertThat(tx.getNetworkFee(), is(1224450L));
        assertThat(tx.getSigners(), hasSize(1));
        assertThat(tx.getSigners().get(0).getScriptHash(), is(CONSENSUS_MULTISIG_SCRIPT_HASH));
        assertThat(tx.getSigners().get(0).getScopes(), contains(WitnessScope.CALLED_BY_ENTRY));
        assertThat(tx.getScript(), is(expectedScript));
        assertThat(tx.getWitnesses().get(0).getVerificationScript().getScript(), is(verificationScript));
    }

    @Test
    public void testUnblockAccount() throws IOException {
        byte[] expectedScript = new ScriptBuilder()
                .pushData(Numeric.hexStringToByteArray("c8172ea3b405bf8bfc57c33a8410116b843e13df"))
                .pushInteger(1)
                .pack()
                .pushData(Numeric.hexStringToByteArray("756e626c6f636b4163636f756e74"))
                .pushData(Numeric.hexStringToByteArray("e9ff4ca7cc252e1dfddb26315869cd79505906ce"))
                .sysCall(InteropServiceCode.SYSTEM_CONTRACT_CALL)
                .toArray();
        setUpWireMockForCall("invokescript", "policy_unblockAccount.json",
                Numeric.toHexStringNoPrefix(expectedScript));
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");

        Invocation inv = new PolicyContract(neow3j)
                .buildUnblockAccountInvocation(ScriptHash.fromAddress("Aa1rZbE1k8fXTwzaxxsPRtJYPwhDQjWRFZ"),
                        consensusWallet, Signer.calledByEntry(CONSENSUS_MULTISIG_SCRIPT_HASH));
        Transaction tx = inv.getTransaction();
        assertThat(tx.getSender().getScriptHash().toAddress(), is(CONSENSUS_MULTISIG_ADDRESS));
        assertThat(tx.getSystemFee(), is(4007570L));
        assertThat(tx.getNetworkFee(), is(1226450L));
        assertThat(tx.getSigners(), hasSize(1));
        assertThat(tx.getSigners().get(0).getScriptHash(), is(CONSENSUS_MULTISIG_SCRIPT_HASH));
        assertThat(tx.getSigners().get(0).getScopes(), contains(WitnessScope.CALLED_BY_ENTRY));
        assertThat(tx.getScript(), is(expectedScript));
        assertThat(tx.getWitnesses().get(0).getVerificationScript().getScript(), is(verificationScript));
    }
}
