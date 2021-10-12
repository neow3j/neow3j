package io.neow3j.protocol;

import io.neow3j.protocol.http.HttpService;
import io.neow3j.script.ScriptBuilder;
import io.neow3j.test.NeoExpressTestContainer;
import io.neow3j.test.TestProperties;
import io.neow3j.transaction.AccountSigner;
import io.neow3j.transaction.Transaction;
import io.neow3j.transaction.TransactionBuilder;
import io.neow3j.types.Hash160;
import io.neow3j.utils.Numeric;
import io.neow3j.wallet.Account;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.ArrayList;

import static io.neow3j.utils.Numeric.toHexStringNoPrefix;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class TransactionBuilderIntegrationTest {

    private static final String TEST_SCRIPT = toHexStringNoPrefix(new ScriptBuilder()
            .contractCall(new Hash160(TestProperties.neoTokenHash()), "symbol", new ArrayList<>())
            .toArray());

    protected static Neow3jExpress neow3jExpress;

    @ClassRule
    public static NeoExpressTestContainer container = new NeoExpressTestContainer(1)
            .withNeoxpConfig(NeoExpressTestContainer.DEFAULT_NEOXP_CONFIG_SRC);

    @BeforeClass
    public static void setUp() {
        neow3jExpress = Neow3jExpress.build(new HttpService(container.getNodeUrl()));
    }

    @Test
    public void testAutomaticSettingOfNetworkFeeWithDifferentAccounts() throws Throwable {
        TransactionBuilder b = new TransactionBuilder(neow3jExpress)
                .script(Numeric.hexStringToByteArray(TEST_SCRIPT));

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


}
