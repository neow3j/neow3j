package io.neow3j.contract;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.neow3j.crypto.Base64;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.script.ScriptBuilder;
import io.neow3j.transaction.Transaction;
import io.neow3j.transaction.TransactionBuilder;
import io.neow3j.transaction.WitnessScope;
import io.neow3j.types.Hash160;
import io.neow3j.wallet.Account;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static io.neow3j.contract.SmartContract.DEFAULT_ITERATOR_COUNT;
import static io.neow3j.script.ScriptBuilder.buildContractCallAndUnwrapIterator;
import static io.neow3j.test.WireMockTestHelper.setUpWireMockForInvokeScript;
import static io.neow3j.types.ContractParameter.hash160;
import static io.neow3j.types.ContractParameter.integer;
import static io.neow3j.test.WireMockTestHelper.setUpWireMockForCall;
import static io.neow3j.test.WireMockTestHelper.setUpWireMockForInvokeFunction;
import static io.neow3j.transaction.AccountSigner.calledByEntry;
import static io.neow3j.utils.Numeric.toHexString;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PolicyContractTest {

    private static final Hash160 POLICYCONTRACT_HASH =
            new Hash160("cc5e4edd9f5f8dba8bb65734541df7a1c081c67b");

    private PolicyContract policyContract;
    private Account account1;
    private Hash160 recipient;

    @RegisterExtension
    static WireMockExtension wireMockExtension = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @BeforeAll
    public void setUp() throws IOException {
        // Configuring WireMock to use default host and the dynamic port set in WireMockRule.
        int port = wireMockExtension.getPort();
        WireMock.configureFor(port);
        setUpWireMockForCall("getversion", "getversion.json");
        Neow3j neow3j = Neow3j.build(new HttpService("http://127.0.0.1:" + port));
        policyContract = new PolicyContract(neow3j);
        account1 = Account.fromWIF("L1WMhxazScMhUrdv34JqQb1HFSQmWeN2Kpc1R9JGKwL7CDNP21uR");
        recipient = new Hash160("969a77db482f74ce27105f760efa139223431394");
    }

    @Test
    public void testGetFeePerByte() throws IOException {
        setUpWireMockForInvokeFunction("getFeePerByte", "policy_getFeePerByte.json");
        assertThat(policyContract.getFeePerByte(), is(new BigInteger("1000")));
    }

    @Test
    public void testGetExecFeeFactor() throws IOException {
        setUpWireMockForInvokeFunction("getExecFeeFactor", "policy_getExecFeeFactor.json");
        assertThat(policyContract.getExecFeeFactor(), is(new BigInteger("30")));
    }

    @Test
    public void testGetStoragePrice() throws IOException {
        setUpWireMockForInvokeFunction("getStoragePrice", "policy_getStoragePrice.json");
        assertThat(policyContract.getStoragePrice(), is(new BigInteger("100000")));
    }

    @Test
    public void testGetMillisecondsPerBlock() throws IOException {
        setUpWireMockForInvokeFunction("getMillisecondsPerBlock", "policy_getMillisecondsPerBlock.json");
        assertThat(policyContract.getMillisecondsPerBlock(), is(new BigInteger("15000")));
    }

    @Test
    public void testGetMaxValidUntilBlockIncrement() throws IOException {
        setUpWireMockForInvokeFunction("getMaxValidUntilBlockIncrement", "policy_getMaxValidUntilBlockIncrement.json");
        assertThat(policyContract.getMaxValidUntilBlockIncrement(), is(new BigInteger("5760")));
    }

    @Test
    public void testGetMaxTraceableBlocks() throws IOException {
        setUpWireMockForInvokeFunction("getMaxTraceableBlocks", "policy_getMaxTraceableBlocks.json");
        assertThat(policyContract.getMaxTraceableBlocks(), is(new BigInteger("2102400")));
    }

    @Test
    public void testIsBlocked() throws IOException {
        setUpWireMockForInvokeFunction("isBlocked", "policy_isBlocked.json");
        assertFalse(policyContract.isBlocked(account1.getScriptHash()));
    }

    @Test
    public void testGetBlockedAccounts() throws IOException {
        setUpWireMockForInvokeFunction("getBlockedAccounts", "invokefunction_iterator_session.json");
        Iterator<Hash160> it = policyContract.getBlockedAccounts();
        assertNotNull(it.getIteratorId());
        assertNotNull(it.getSessionId());
        assertNotNull(it.getMapper());
    }

    @Test
    public void testGetBlockedAccountsUnwrapped() throws IOException {
        String scriptBase64 = Base64.encode(toHexString(buildContractCallAndUnwrapIterator(PolicyContract.SCRIPT_HASH,
                "getBlockedAccounts", asList(), DEFAULT_ITERATOR_COUNT)));
        setUpWireMockForInvokeScript(scriptBase64, "policy_getBlockedAccountsUnwrapped.json");

        List<Hash160> blockedAccounts = policyContract.getBlockedAccountsUnwrapped();
        assertThat(blockedAccounts, hasSize(1));
        assertThat(blockedAccounts.get(0), is(new Hash160("0x69ecca587293047be4c59159bf8bc399985c160d")));
    }

    @Test
    public void testSetFeePerByte_ProducesCorrectTransaction() throws Throwable {
        setUpWireMockForCall("getversion", "getversion.json");
        setUpWireMockForCall("invokescript", "policy_setFeePerByte.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");
        setUpWireMockForCall("calculatenetworkfee", "calculatenetworkfee.json");

        byte[] expectedScript = new ScriptBuilder()
                .contractCall(
                        PolicyContract.SCRIPT_HASH,
                        "setFeePerByte",
                        singletonList(integer(20)))
                .toArray();

        Transaction tx = policyContract.setFeePerByte(new BigInteger("20"))
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
    public void testSetExecFeeFactor() throws Throwable {
        setUpWireMockForCall("getversion", "getversion.json");
        setUpWireMockForCall("invokescript", "policy_setExecFeeFactor.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");
        setUpWireMockForCall("calculatenetworkfee", "calculatenetworkfee.json");

        byte[] expectedScript = new ScriptBuilder()
                .contractCall(
                        PolicyContract.SCRIPT_HASH,
                        "setExecFeeFactor",
                        singletonList(integer(10)))
                .toArray();

        Transaction tx = policyContract.setExecFeeFactor(BigInteger.TEN)
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
    public void testSetStoragePrice() throws Throwable {
        setUpWireMockForCall("getversion", "getversion.json");
        setUpWireMockForCall("invokescript", "policy_setStoragePrice.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");
        setUpWireMockForCall("calculatenetworkfee", "calculatenetworkfee.json");

        byte[] expectedScript = new ScriptBuilder()
                .contractCall(
                        PolicyContract.SCRIPT_HASH,
                        "setStoragePrice",
                        singletonList(integer(8)))
                .toArray();

        Transaction tx = policyContract.setStoragePrice(new BigInteger("8"))
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
    public void testSetMillisecondsPerBlock() throws Throwable {
        setUpWireMockForCall("getversion", "getversion.json");
        setUpWireMockForCall("invokescript", "policy_setMillisecondsPerBlock.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");
        setUpWireMockForCall("calculatenetworkfee", "calculatenetworkfee.json");

        byte[] expectedScript = new ScriptBuilder()
                .contractCall(
                        PolicyContract.SCRIPT_HASH,
                        "setMillisecondsPerBlock",
                        singletonList(integer(15000)))
                .toArray();

        Transaction tx = policyContract.setMillisecondsPerBlock(new BigInteger("15000"))
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
    public void testSetMaxValidUntilBlockIncrement() throws Throwable {
        setUpWireMockForCall("getversion", "getversion.json");
        setUpWireMockForCall("invokescript", "policy_setMaxValidUntilBlockIncrement.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");
        setUpWireMockForCall("calculatenetworkfee", "calculatenetworkfee.json");

        byte[] expectedScript = new ScriptBuilder()
                .contractCall(
                        PolicyContract.SCRIPT_HASH,
                        "setMaxValidUntilBlockIncrement",
                        singletonList(integer(5760)))
                .toArray();

        Transaction tx = policyContract.setMaxValidUntilBlockIncrement(new BigInteger("5760"))
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
    public void testSetMaxTraceableBlocks() throws Throwable {
        setUpWireMockForCall("getversion", "getversion.json");
        setUpWireMockForCall("invokescript", "policy_setMaxTraceableBlocks.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");
        setUpWireMockForCall("calculatenetworkfee", "calculatenetworkfee.json");

        byte[] expectedScript = new ScriptBuilder()
                .contractCall(
                        PolicyContract.SCRIPT_HASH,
                        "setMaxTraceableBlocks",
                        singletonList(integer(2102400)))
                .toArray();

        Transaction tx = policyContract.setMaxTraceableBlocks(new BigInteger("2102400"))
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
    public void testBlockAccount() throws Throwable {
        setUpWireMockForCall("getversion", "getversion.json");
        setUpWireMockForCall("invokescript", "policy_blockAccount.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");
        setUpWireMockForCall("calculatenetworkfee", "calculatenetworkfee.json");

        byte[] expectedScript = new ScriptBuilder()
                .contractCall(
                        PolicyContract.SCRIPT_HASH,
                        "blockAccount",
                        singletonList(hash160(recipient)))
                .toArray();

        Transaction tx = policyContract.blockAccount(recipient)
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
    public void testBlockAccount_address() throws Throwable {
        setUpWireMockForCall("getversion", "getversion.json");
        setUpWireMockForCall("invokescript", "policy_blockAccount.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");
        setUpWireMockForCall("calculatenetworkfee", "calculatenetworkfee.json");

        byte[] expectedScript = new ScriptBuilder()
                .contractCall(
                        PolicyContract.SCRIPT_HASH,
                        "blockAccount",
                        singletonList(hash160(recipient)))
                .toArray();

        Transaction tx = policyContract
                .blockAccount(recipient.toAddress())
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
    public void testUnblockAccount() throws Throwable {
        setUpWireMockForCall("getversion", "getversion.json");
        setUpWireMockForCall("invokescript", "policy_unblockAccount.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");
        setUpWireMockForCall("calculatenetworkfee", "calculatenetworkfee.json");

        byte[] expectedScript = new ScriptBuilder()
                .contractCall(
                        PolicyContract.SCRIPT_HASH,
                        "unblockAccount",
                        singletonList(hash160(recipient)))
                .toArray();

        Transaction tx = policyContract.unblockAccount(recipient)
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
    public void testUnblockAccount_address() throws Throwable {
        setUpWireMockForCall("getversion", "getversion.json");
        setUpWireMockForCall("invokescript", "policy_unblockAccount.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");
        setUpWireMockForCall("calculatenetworkfee", "calculatenetworkfee.json");

        byte[] expectedScript = new ScriptBuilder()
                .contractCall(
                        PolicyContract.SCRIPT_HASH,
                        "unblockAccount",
                        singletonList(hash160(recipient)))
                .toArray();

        Transaction tx = policyContract.unblockAccount(recipient.toAddress())
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
    public void testRecoverFund() {
        Hash160 accountToRecoverFrom = account1.getScriptHash();
        byte[] expectedScript = new ScriptBuilder()
                .contractCall(
                        PolicyContract.SCRIPT_HASH,
                        "recoverFund",
                        asList(hash160(accountToRecoverFrom), hash160(NeoToken.SCRIPT_HASH)))
                .toArray();

        TransactionBuilder b = policyContract.recoverFund(accountToRecoverFrom, NeoToken.SCRIPT_HASH);
        assertThat(b.getScript(), is(expectedScript));
    }

    @Test
    public void testScriptHash() {
        assertThat(policyContract.getScriptHash(), is(POLICYCONTRACT_HASH));
    }

}
