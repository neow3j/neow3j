package io.neow3j.contract;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.Neow3jConfig;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.script.ScriptBuilder;
import io.neow3j.transaction.Transaction;
import io.neow3j.transaction.WitnessScope;
import io.neow3j.types.Hash160;
import io.neow3j.wallet.Account;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.math.BigInteger;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static io.neow3j.types.ContractParameter.hash160;
import static io.neow3j.types.ContractParameter.integer;
import static io.neow3j.test.WireMockTestHelper.setUpWireMockForCall;
import static io.neow3j.test.WireMockTestHelper.setUpWireMockForInvokeFunction;
import static io.neow3j.transaction.AccountSigner.calledByEntry;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;

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
    public void setUp() {
        // Configuring WireMock to use default host and the dynamic port set in WireMockRule.
        int port = wireMockExtension.getPort();
        WireMock.configureFor(port);
        Neow3j neow3j = Neow3j.build(new HttpService("http://127.0.0.1:" + port),
                new Neow3jConfig().setNetworkMagic(769));
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
    public void testIsBlocked() throws IOException {
        setUpWireMockForInvokeFunction("isBlocked", "policy_isBlocked.json");
        assertFalse(policyContract.isBlocked(account1.getScriptHash()));
    }

    @Test
    public void testSetFeePerByte_ProducesCorrectTransaction() throws Throwable {
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
    public void testBlockAccount() throws Throwable {
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
    public void testScriptHash() {
        assertThat(policyContract.getScriptHash(), is(POLICYCONTRACT_HASH));
    }

}
