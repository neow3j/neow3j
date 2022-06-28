package io.neow3j.protocol;

import io.neow3j.crypto.ECKeyPair;
import io.neow3j.protocol.Neow3jExpress;
import io.neow3j.protocol.core.response.InvocationResult;
import io.neow3j.protocol.core.response.NeoApplicationLog;
import io.neow3j.protocol.core.response.NeoSendRawTransaction;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.script.ScriptBuilder;
import io.neow3j.test.NeoExpressTestContainer;
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
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;

import static io.neow3j.test.TestProperties.gasTokenHash;
import static io.neow3j.test.TestProperties.neoTokenHash;
import static io.neow3j.types.ContractParameter.hash160;
import static io.neow3j.types.ContractParameter.integer;
import static io.neow3j.utils.Numeric.hexStringToByteArray;
import static io.neow3j.utils.Numeric.toHexStringNoPrefix;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class TransactionBuilderIntegrationTest {

    private static final String TEST_SCRIPT = toHexStringNoPrefix(new ScriptBuilder()
            .contractCall(new Hash160(neoTokenHash()), "symbol", new ArrayList<>())
            .toArray());

    protected static Neow3jExpress neow3jExpress;

    @ClassRule
    public static NeoExpressTestContainer container = new NeoExpressTestContainer()
            .withSecondsPerBlock(1)
            .withConfigFile(NeoExpressTestContainer.DEFAULT_NEOXP_CONFIG);

    @BeforeClass
    public static void setUp() {
        neow3jExpress = Neow3jExpress.build(new HttpService(container.getNodeUrl()));
    }

    @Test
    public void testAutomaticSettingOfNetworkFeeWithDifferentAccounts() throws Throwable {
        TransactionBuilder b = new TransactionBuilder(neow3jExpress)
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
        TransactionBuilder b = new TransactionBuilder(neow3jExpress)
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
        TransactionBuilder b = new TransactionBuilder(neow3jExpress)
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
        Account recipient = Account.create();
        ECKeyPair keyPair = ECKeyPair.create(hexStringToByteArray(container.getGenesisAccount().getPrivateKeys()[0]));
        Account genesis = new Account(keyPair);
        int amount = 10000;

        byte[] script = new ScriptBuilder()
                .contractCall(new Hash160(gasTokenHash()), "transfer", asList(hash160(genesis), hash160(recipient),
                        integer(amount), null))
                .toArray();

        AndCondition condition = new AndCondition(
                new CalledByEntryCondition(),
                new NotCondition(new BooleanCondition(false)),
                new NotCondition(new GroupCondition(recipient.getECKeyPair().getPublicKey())));
        WitnessRule rule = new WitnessRule(WitnessAction.ALLOW, condition);
        Signer signer = AccountSigner.none(genesis).setRules(rule);

        TransactionBuilder b = new TransactionBuilder(neow3jExpress)
                .script(script)
                .signers(signer);

        Transaction tx = b.sign();
        NeoSendRawTransaction response = tx.send();
        assertFalse(response.hasError());

        Hash256 txHash = response.getSendRawTransaction().getHash();
        Await.waitUntilTransactionIsExecuted(txHash, neow3jExpress);
        NeoApplicationLog log = neow3jExpress.getApplicationLog(txHash).send().getApplicationLog();
        assertThat(log.getExecutions().get(0).getState(), is(NeoVMStateType.HALT));
        assertTrue(log.getExecutions().get(0).getStack().get(0).getBoolean()); // The transfer should be successful.

        // Same condition, but deny the witness. The returned stack should contain false.
        rule = new WitnessRule(WitnessAction.DENY, condition);
        signer = AccountSigner.none(genesis).setRules(rule);

        b = new TransactionBuilder(neow3jExpress)
                .script(script)
                .signers(signer);

        tx = b.sign();
        response = tx.send();
        assertFalse(response.hasError());
        txHash = response.getSendRawTransaction().getHash();
        Await.waitUntilTransactionIsExecuted(txHash, neow3jExpress);
        log = neow3jExpress.getApplicationLog(txHash).send().getApplicationLog();
        assertThat(log.getExecutions().get(0).getState(), is(NeoVMStateType.HALT));
        assertFalse(log.getExecutions().get(0).getStack().get(0).getBoolean()); // The transfer should fail.
    }

    @Test
    public void testTransmissionOnFault() throws Throwable {
        Account a = Account.fromAddress(TestProperties.defaultAccountAddress());
        neow3jExpress.allowTransmissionOnFault();
        String failingScript = toHexStringNoPrefix(new ScriptBuilder()
                .contractCall(new Hash160(neoTokenHash()), "balanceOf", new ArrayList<>())
                .toArray());
        TransactionBuilder b = new TransactionBuilder(neow3jExpress)
                .script(hexStringToByteArray(failingScript))
                .signers(AccountSigner.none(a));

        InvocationResult result = b.callInvokeScript().getInvocationResult();
        assertTrue(result.hasStateFault());
        long gasConsumed = new BigInteger(result.getGasConsumed()).longValue();

        Transaction tx = b.getUnsignedTransaction();
        assertThat(tx.getSystemFee(), is(gasConsumed));
        neow3jExpress.preventTransmissionOnFault();
    }

    @Test
    public void testPreventTransmissionOnFault() throws Throwable {
        Account a = Account.fromAddress(TestProperties.defaultAccountAddress());
        assertFalse(neow3jExpress.transmissionOnFaultIsAllowed());
        String failingScript = toHexStringNoPrefix(new ScriptBuilder()
                .contractCall(new Hash160(neoTokenHash()), "balanceOf", new ArrayList<>())
                .toArray());
        TransactionBuilder b = new TransactionBuilder(neow3jExpress)
                .script(hexStringToByteArray(failingScript))
                .signers(AccountSigner.none(a));

        InvocationResult result = b.callInvokeScript().getInvocationResult();
        assertTrue(result.hasStateFault());

        TransactionConfigurationException thrown =
                assertThrows(TransactionConfigurationException.class, b::getUnsignedTransaction);
        assertThat(thrown.getMessage(), containsString("The vm exited due to the following exception: "));
    }

}
