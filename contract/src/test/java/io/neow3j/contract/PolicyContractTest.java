package io.neow3j.contract;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.neow3j.contract.ContractTestHelper.setUpWireMockForCall;
import static io.neow3j.contract.ContractTestHelper.setUpWireMockForInvokeFunction;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.neow3j.protocol.Neow3j;
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

public class PolicyContractTest {

    private static final ScriptHash POLICY_SCRIPT_HASH = PolicyContract.SCRIPT_HASH;

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
    public void testGetMaxTransactionsPerBlock() throws IOException {
        setUpWireMockForInvokeFunction("getMaxTransactionsPerBlock",
                "policy_getMaxTxPerBlock.json");

        PolicyContract policyContract = new PolicyContract(this.neow3j);
        assertThat(policyContract.getMaxTransactionsPerBlock(), is(512));
    }

    @Test
    public void testGetMaxBlockSize() throws IOException {
        setUpWireMockForInvokeFunction("getMaxBlockSize",
                "policy_getMaxBlockSize.json");

        PolicyContract policyContract = new PolicyContract(this.neow3j);
        assertThat(policyContract.getMaxBlockSize(), is(262144));
    }

    @Test
    public void testGetMaxBlockSystemFee() throws IOException {
        setUpWireMockForInvokeFunction("getMaxBlockSystemFee",
                "policy_getMaxBlockSystemFee.json");

        PolicyContract policyContract = new PolicyContract(this.neow3j);
        assertThat(policyContract.getMaxBlockSystemFee(), is(new BigInteger("900000000000")));
    }

    @Test
    public void testGetFeePerByte() throws IOException {
        setUpWireMockForInvokeFunction("getFeePerByte", "policy_getFeePerByte.json");

        PolicyContract policyContract = new PolicyContract(this.neow3j);
        assertThat(policyContract.getFeePerByte(), is(1000));
    }

    @Test
    public void testGetExecFeeFactor() throws IOException {
        setUpWireMockForInvokeFunction("getExecFeeFactor", "policy_getExecFeeFactor.json");

        PolicyContract policyContract = new PolicyContract(neow3j);
//        assertThat(policyContract.getExecFeeFactor(), is());
    }

    @Test
    public void testGetStoragePrice() throws IOException {
        setUpWireMockForInvokeFunction("getStoragePrice", "policy_getStoragePrice.json");

        PolicyContract policyContract = new PolicyContract(neow3j);
//        assertThat(policyContract.getStoragePrice(), is());
    }

    @Test
    public void testIsBlocked() throws IOException {
        setUpWireMockForInvokeFunction("isBlocked", "policy_isBlocked.json");

        PolicyContract policyContract = new PolicyContract(neow3j);
//        assertThat(policyContract.isBlocked(account1.getScriptHash()), is());
    }

    @Test
    public void testSetMaxBlockSize_producesCorrectTransaction() throws Throwable {
        setUpWireMockForCall("invokescript", "policy_setMaxBlockSize.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");

        byte[] expectedScript = new ScriptBuilder()
                .contractCall(POLICY_SCRIPT_HASH, "setMaxBlockSize",
                        Arrays.asList(ContractParameter.integer(200000)))
                .toArray();

        Wallet w = Wallet.withAccounts(account1);
        Transaction tx = new PolicyContract(neow3j)
                .setMaxBlockSize(200000)
                .wallet(w)
                .signers(Signer.calledByEntry(account1.getScriptHash()))
                .sign();

        assertThat(tx.getSigners(), hasSize(1));
        assertThat(tx.getSigners().get(0).getScriptHash(), is(account1.getScriptHash()));
        assertThat(tx.getSigners().get(0).getScopes(), contains(WitnessScope.CALLED_BY_ENTRY));
        assertThat(tx.getScript(), is(expectedScript));
        assertThat(tx.getWitnesses(), hasSize(1));
        assertThat(tx.getWitnesses().get(0).getVerificationScript().getScript(),
                is(account1.getVerificationScript().getScript()));
    }

    @Test
    public void testSetMaxTxPerBlock_ProducesCorrectTransaction() throws Throwable {
        setUpWireMockForCall("invokescript", "policy_setMaxTransactionsPerBlock.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");

        byte[] expectedScript = new ScriptBuilder().contractCall(POLICY_SCRIPT_HASH,
                "setMaxTransactionsPerBlock", Arrays.asList(ContractParameter.integer(500))).toArray();

        Wallet w = Wallet.withAccounts(account1);
        Transaction tx = new PolicyContract(neow3j)
                .setMaxTransactionsPerBlock(500)
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
    public void testSetMaxBlockSystemFee() throws Throwable {
        setUpWireMockForCall("invokescript", "policy_setMaxBlockSystemFee.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");

        byte[] expectedScript = new ScriptBuilder().contractCall(POLICY_SCRIPT_HASH,
                "setMaxBlockSystemFee",
                Arrays.asList(ContractParameter.integer(new BigInteger("880000000000")))).toArray();

        Wallet w = Wallet.withAccounts(account1);
        Transaction tx = new PolicyContract(neow3j)
                .setMaxBlockSystemFee(new BigInteger("880000000000"))
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
    public void testSetFeePerByte_ProducesCorrectTransaction() throws Throwable {
        setUpWireMockForCall("invokescript", "policy_setFeePerByte.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");

        byte[] expectedScript = new ScriptBuilder().contractCall(POLICY_SCRIPT_HASH,
                "setFeePerByte", Arrays.asList(ContractParameter.integer(20))).toArray();

        Wallet w = Wallet.withAccounts(account1);
        Transaction tx = new PolicyContract(neow3j)
                .setFeePerByte(20)
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
    public void testSetExecFeeFactor() throws Throwable {
        setUpWireMockForCall("invokescript", "policy_setExecFeeFactor.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");

        byte[] expectedScript = new ScriptBuilder().contractCall(POLICY_SCRIPT_HASH,
                "setExecFeeFactor", Arrays.asList(ContractParameter.integer(10))).toArray();

        Wallet w = Wallet.withAccounts(account1);
        Transaction tx = new PolicyContract(neow3j)
                .setExecFeeFactor(10)
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
    public void testSetStoragePrice() throws Throwable {
        setUpWireMockForCall("invokescript", "policy_setStoragePrice.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");

        byte[] expectedScript = new ScriptBuilder().contractCall(POLICY_SCRIPT_HASH,
                "setExecFeeFactor", Arrays.asList(ContractParameter.integer(8))).toArray();

        Wallet w = Wallet.withAccounts(account1);
        Transaction tx = new PolicyContract(neow3j)
                .setStoragePrice(8)
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
    public void testBlockAccount() throws Throwable {
        setUpWireMockForCall("invokescript", "policy_blockAccount.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");

        byte[] expectedScript = new ScriptBuilder().contractCall(POLICY_SCRIPT_HASH, "blockAccount",
                Arrays.asList(ContractParameter.hash160(recipient))).toArray();

        Wallet w = Wallet.withAccounts(account1);
        Transaction tx = new PolicyContract(neow3j)
                .blockAccount(recipient)
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
    public void testBlockAccount_address() throws Throwable {
        setUpWireMockForCall("invokescript", "policy_blockAccount.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");

        byte[] expectedScript = new ScriptBuilder().contractCall(POLICY_SCRIPT_HASH, "blockAccount",
                Arrays.asList(ContractParameter.hash160(recipient))).toArray();

        Wallet w = Wallet.withAccounts(account1);
        Transaction tx = new PolicyContract(neow3j)
                .blockAccount(recipient.toAddress())
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
    public void testUnblockAccount() throws Throwable {
        setUpWireMockForCall("invokescript", "policy_unblockAccount.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");

        byte[] expectedScript = new ScriptBuilder().contractCall(POLICY_SCRIPT_HASH,
                "unblockAccount", Arrays.asList(ContractParameter.hash160(recipient))).toArray();

        Wallet w = Wallet.withAccounts(account1);
        Transaction tx = new PolicyContract(neow3j)
                .unblockAccount(recipient)
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
    public void testUnblockAccount_address() throws Throwable {
        setUpWireMockForCall("invokescript", "policy_unblockAccount.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");

        byte[] expectedScript = new ScriptBuilder().contractCall(POLICY_SCRIPT_HASH,
                "unblockAccount", Arrays.asList(ContractParameter.hash160(recipient))).toArray();

        Wallet w = Wallet.withAccounts(account1);
        Transaction tx = new PolicyContract(neow3j)
                .unblockAccount(recipient.toAddress())
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
}
