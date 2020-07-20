package io.neow3j.contract;

import static io.neow3j.contract.ContractTestHelper.setUpWireMockForCall;
import static io.neow3j.contract.ContractTestHelper.setUpWireMockForInvokeFunction;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.transaction.Cosigner;
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
    public WireMockRule wireMockRule = new WireMockRule();

    private Neow3j neow3j;
    private Wallet consensusWallet;

    private static final ScriptHash CONSENSUS_MULTISIG_SCRIPT_HASH =
            new ScriptHash("55b842d631f43f23257a27992ac2b53169a4fe00");
    private static final String CONSENSUS_MULTISIG_ADDRESS = "AFs8hMHrS8emaPP4oyTuf5uKPuAW6HZ2DF";
    private static final byte[] VERIFICATION_SCRIPT =
            Numeric.hexStringToByteArray("110c2102c0b60c995bc092e866f15a37c176bb59b7ebacf069ba94c0ebf561cb8f956238110b41c330181e");

    @Before
    public void setUp() {
        // Configuring WireMock to use default host and port "localhost:8080".
        WireMock.configure();
        neow3j = Neow3j.build(new HttpService("http://localhost:8080"));

        // Configuring wallet to invoke policy contract
        ECKeyPair ecKeyPair = ECKeyPair.create(
                Numeric.hexStringToByteArray("e6e919577dd7b8e97805151c05ae07ff4f752654d6d8797597aca989c02c4cb3"));
        Account acct = new Account(ecKeyPair);
        consensusWallet = Wallet.withAccounts(acct);
        consensusWallet.addAccounts(Account.createMultiSigAccount(
                Arrays.asList(acct.getECKeyPair().getPublicKey()), 1));
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
        String script =
                "001411c00c0d736574466565506572427974650c149a61a46eec97b89306d7ce81f15b462091d0093241627d5b52";
        setUpWireMockForCall("invokescript", "policy_setFeePerByte.json", script);
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");

        Invocation inv = new PolicyContract(neow3j)
                .buildSetFeePerByteInvocation(20, consensusWallet, CONSENSUS_MULTISIG_SCRIPT_HASH);
        Transaction tx = inv.getTransaction();
        assertThat(tx.getSender().toAddress(), is(CONSENSUS_MULTISIG_ADDRESS));
        assertThat(tx.getSystemFee(), is(4007420L));
        assertThat(tx.getNetworkFee(), is(1227450L));
        assertThat(tx.getCosigners(), contains(Cosigner.calledByEntry(CONSENSUS_MULTISIG_SCRIPT_HASH)));
        assertThat(tx.getScript(), is(Numeric.hexStringToByteArray(script)));
        assertThat(tx.getWitnesses().get(0).getVerificationScript().getScript(), is(VERIFICATION_SCRIPT));
    }

    @Test
    public void testSetMaxTxPerBlock() throws IOException {
        String script =
                "01f40111c00c1a7365744d61785472616e73616374696f6e73506572426c6f636b0c149a61a46eec97b89306d7ce81f15b462091d0093241627d5b52";
        setUpWireMockForCall("invokescript", "policy_setMaxTransactionsPerBlock.json", script);
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");

        Invocation inv = new PolicyContract(neow3j)
                .buildSetMaxTxPerBlockInvocation(500, consensusWallet, CONSENSUS_MULTISIG_SCRIPT_HASH);
        Transaction tx = inv.getTransaction();
        assertThat(tx.getSender().toAddress(), is(CONSENSUS_MULTISIG_ADDRESS));
        assertThat(tx.getSystemFee(), is(4007420L));
        assertThat(tx.getNetworkFee(), is(1241450L));
        assertThat(tx.getCosigners(), contains(Cosigner.calledByEntry(CONSENSUS_MULTISIG_SCRIPT_HASH)));
        assertThat(tx.getScript(), is(Numeric.hexStringToByteArray(script)));
        assertThat(tx.getWitnesses().get(0).getVerificationScript().getScript(), is(VERIFICATION_SCRIPT));
    }

    @Test
    public void testBlockAccount() throws IOException {
        String script =
                "0c14c8172ea3b405bf8bfc57c33a8410116b843e13df11c00c0c626c6f636b4163636f756e740c149a61a46eec97b89306d7ce81f15b462091d0093241627d5b52";
        setUpWireMockForCall("invokescript", "policy_blockAccount.json", script);
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");

        Invocation inv = new PolicyContract(neow3j)
                .buildBlockAccountInvocation(ScriptHash.fromAddress("Aa1rZbE1k8fXTwzaxxsPRtJYPwhDQjWRFZ"),
                        consensusWallet, CONSENSUS_MULTISIG_SCRIPT_HASH);
        Transaction tx = inv.getTransaction();
        assertThat(tx.getSender().toAddress(), is(CONSENSUS_MULTISIG_ADDRESS));
        assertThat(tx.getSystemFee(), is(4007570L));
        assertThat(tx.getNetworkFee(), is(1246450L));
        assertThat(tx.getCosigners(), contains(Cosigner.calledByEntry(CONSENSUS_MULTISIG_SCRIPT_HASH)));
        assertThat(tx.getScript(), is(Numeric.hexStringToByteArray(script)));
        assertThat(tx.getWitnesses().get(0).getVerificationScript().getScript(), is(VERIFICATION_SCRIPT));
    }

    @Test
    public void testUnblockAccount() throws IOException {
        String script =
                "0c14c8172ea3b405bf8bfc57c33a8410116b843e13df11c00c0e756e626c6f636b4163636f756e740c149a61a46eec97b89306d7ce81f15b462091d0093241627d5b52";
        setUpWireMockForCall("invokescript", "policy_unblockAccount.json", script);
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");

        Invocation inv = new PolicyContract(neow3j)
                .buildUnblockAccountInvocation(ScriptHash.fromAddress("Aa1rZbE1k8fXTwzaxxsPRtJYPwhDQjWRFZ"),
                        consensusWallet, CONSENSUS_MULTISIG_SCRIPT_HASH);
        Transaction tx = inv.getTransaction();
        assertThat(tx.getSender().toAddress(), is(CONSENSUS_MULTISIG_ADDRESS));
        assertThat(tx.getSystemFee(), is(4007570L));
        assertThat(tx.getNetworkFee(), is(1248450L));
        assertThat(tx.getCosigners(), contains(Cosigner.calledByEntry(CONSENSUS_MULTISIG_SCRIPT_HASH)));
        assertThat(tx.getScript(), is(Numeric.hexStringToByteArray(script)));
        assertThat(tx.getWitnesses().get(0).getVerificationScript().getScript(), is(VERIFICATION_SCRIPT));
    }
}
