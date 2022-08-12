package io.neow3j.protocol;

import io.neow3j.contract.GasToken;
import io.neow3j.protocol.core.response.InvocationResult;
import io.neow3j.protocol.core.response.NeoApplicationLog;
import io.neow3j.protocol.core.response.NeoSendRawTransaction;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.script.ScriptBuilder;
import io.neow3j.test.NeoTestContainer;
import io.neow3j.test.TestProperties;
import io.neow3j.transaction.AccountSigner;
import io.neow3j.transaction.Signer;
import io.neow3j.transaction.Transaction;
import io.neow3j.transaction.TransactionBuilder;
import io.neow3j.transaction.exceptions.TransactionConfigurationException;
import io.neow3j.transaction.witnessrule.AndCondition;
import io.neow3j.transaction.witnessrule.BooleanCondition;
import io.neow3j.transaction.witnessrule.CalledByEntryCondition;
import io.neow3j.transaction.witnessrule.GroupCondition;
import io.neow3j.transaction.witnessrule.NotCondition;
import io.neow3j.transaction.witnessrule.WitnessAction;
import io.neow3j.transaction.witnessrule.WitnessRule;
import io.neow3j.types.Hash160;
import io.neow3j.types.Hash256;
import io.neow3j.types.NeoVMStateType;
import io.neow3j.utils.Await;
import io.neow3j.wallet.Account;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigInteger;
import java.util.ArrayList;

import static io.neow3j.test.TestProperties.neoTokenHash;
import static io.neow3j.utils.Numeric.hexStringToByteArray;
import static io.neow3j.utils.Numeric.toHexStringNoPrefix;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
public class TransactionBuilderIntegrationTest {

    private static final String TEST_SCRIPT = toHexStringNoPrefix(new ScriptBuilder()
            .contractCall(new Hash160(neoTokenHash()), "symbol", new ArrayList<>())
            .toArray());

    protected static Neow3j neow3j;

    @Container
    public static NeoTestContainer container = new NeoTestContainer();

    @BeforeAll
    public static void setUp() {
        neow3j = Neow3j.build(new HttpService(container.getNodeUrl()));
        Await.waitUntilBlockCountIsGreaterThanZero(neow3j);
    }

    @Test
    public void testAutomaticSettingOfNetworkFeeWithDifferentAccounts() throws Throwable {
        TransactionBuilder b = new TransactionBuilder(neow3j)
                .script(hexStringToByteArray(TEST_SCRIPT));

        // single-sig account from address, no verification script
        Account a = Account.fromAddress(TestProperties.defaultAccountAddress());
        Transaction tx = b.signers(AccountSigner.calledByEntry(a)).getUnsignedTransaction();
        assertThat(tx.getNetworkFee(), is(1178520L));

        // multi-sig account from address with one participant, no verification script
        a = Account.createMultiSigAccount(TestProperties.committeeAccountAddress(), 1, 1);
        tx = b.signers(AccountSigner.calledByEntry(a)).getUnsignedTransaction();
        assertThat(tx.getNetworkFee(), is(1180580L));

        // multi-sig account from address with seven participants, no verification script
        a = Account.createMultiSigAccount(TestProperties.committeeAccountAddress(), 5, 7);
        tx = b.signers(AccountSigner.calledByEntry(a)).getUnsignedTransaction();
        assertThat(tx.getNetworkFee(), is(7557220L));
    }

    @Test
    public void testAdditionalNetworkFee() throws Throwable {
        Account a = Account.fromAddress(TestProperties.defaultAccountAddress());
        TransactionBuilder b = new TransactionBuilder(neow3j)
                .script(hexStringToByteArray(TEST_SCRIPT))
                .signers(AccountSigner.calledByEntry(a));

        Transaction tx = b.getUnsignedTransaction();
        long baseNetworkFee = tx.getNetworkFee();

        b.additionalNetworkFee(5555L);
        tx = b.getUnsignedTransaction();
        assertThat(tx.getNetworkFee(), is(baseNetworkFee + 5555L));
    }

    @Test
    public void testAdditionalSystemFee() throws Throwable {
        Account a = Account.fromAddress(TestProperties.defaultAccountAddress());
        TransactionBuilder b = new TransactionBuilder(neow3j)
                .script(hexStringToByteArray(TEST_SCRIPT))
                .signers(AccountSigner.calledByEntry(a));

        Transaction tx = b.getUnsignedTransaction();
        long baseSystemFee = tx.getSystemFee();

        b.additionalSystemFee(42000L);
        tx = b.getUnsignedTransaction();
        assertThat(tx.getSystemFee(), is(baseSystemFee + 42000L));
    }

    @Test
    public void testSignerWithWitnessRules() throws Throwable {
        Account genesis = Account.fromWIF(TestProperties.defaultAccountWIF());
        Account recipient = Account.create();
        BigInteger amount = new BigInteger("10000");

        AndCondition condition = new AndCondition(
                new CalledByEntryCondition(),
                new NotCondition(new BooleanCondition(false)),
                new NotCondition(new GroupCondition(recipient.getECKeyPair().getPublicKey())));
        WitnessRule rule = new WitnessRule(WitnessAction.ALLOW, condition);
        Signer signer = AccountSigner.none(genesis).setRules(rule);

        GasToken gas = new GasToken(neow3j);
        TransactionBuilder b = gas.transfer(genesis.getScriptHash(), recipient.getScriptHash(), amount).signers(signer);
        NeoSendRawTransaction response = b.sign().send();
        assertFalse(response.hasError());

        Hash256 txHash = response.getSendRawTransaction().getHash();
        Await.waitUntilTransactionIsExecuted(txHash, neow3j);
        NeoApplicationLog log = neow3j.getApplicationLog(txHash).send().getApplicationLog();
        assertThat(log.getExecutions().get(0).getState(), is(NeoVMStateType.HALT));
        assertTrue(log.getExecutions().get(0).getStack().get(0).getBoolean()); // The transfer should be successful.

        // Same condition, but deny the witness. The returned stack should contain false.
        rule = new WitnessRule(WitnessAction.DENY, condition);
        signer = AccountSigner.none(genesis).setRules(rule);

        b = gas.transfer(genesis.getScriptHash(), recipient.getScriptHash(), amount).signers(signer);
        response = b.sign().send();
        assertFalse(response.hasError());
        txHash = response.getSendRawTransaction().getHash();
        Await.waitUntilTransactionIsExecuted(txHash, neow3j);
        log = neow3j.getApplicationLog(txHash).send().getApplicationLog();
        assertThat(log.getExecutions().get(0).getState(), is(NeoVMStateType.HALT));
        assertFalse(log.getExecutions().get(0).getStack().get(0).getBoolean()); // The transfer should fail.
    }

    @Test
    public void testTransmissionOnFault() throws Throwable {
        Account a = Account.fromAddress(TestProperties.defaultAccountAddress());
        neow3j.allowTransmissionOnFault();
        String failingScript = toHexStringNoPrefix(new ScriptBuilder()
                .contractCall(new Hash160(neoTokenHash()), "balanceOf", new ArrayList<>())
                .toArray());
        TransactionBuilder b = new TransactionBuilder(neow3j)
                .script(hexStringToByteArray(failingScript))
                .signers(AccountSigner.none(a));

        InvocationResult result = b.callInvokeScript().getInvocationResult();
        assertTrue(result.hasStateFault());
        long gasConsumed = new BigInteger(result.getGasConsumed()).longValue();

        Transaction tx = b.getUnsignedTransaction();
        assertThat(tx.getSystemFee(), is(gasConsumed));
        neow3j.preventTransmissionOnFault();
    }

    @Test
    public void testPreventTransmissionOnFault() throws Throwable {
        Account a = Account.fromAddress(TestProperties.defaultAccountAddress());
        assertFalse(neow3j.transmissionOnFaultIsAllowed());
        String failingScript = toHexStringNoPrefix(new ScriptBuilder()
                .contractCall(new Hash160(neoTokenHash()), "balanceOf", new ArrayList<>())
                .toArray());
        TransactionBuilder b = new TransactionBuilder(neow3j)
                .script(hexStringToByteArray(failingScript))
                .signers(AccountSigner.none(a));

        InvocationResult result = b.callInvokeScript().getInvocationResult();
        assertTrue(result.hasStateFault());

        TransactionConfigurationException thrown =
                assertThrows(TransactionConfigurationException.class, b::getUnsignedTransaction);
        assertThat(thrown.getMessage(), containsString("The vm exited due to the following exception: "));
    }

}
