package io.neow3j.neofs;

import io.neow3j.neofs.sdk.NeoFSClient;
import io.neow3j.wallet.Account;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static io.neow3j.neofs.NeoFSIntegrationTestHelper.neofsEndpoint;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AccountingIntegrationTest {

    private NeoFSClient neoFSClient;
    private Account account;

    @BeforeAll
    public void setUp() throws Throwable {
        account = Account.fromWIF("KzAXTwrj1VxQA746zSSMCt9g3omSDfyKnwsayEducuHvKd1LR9mx");
        neoFSClient = NeoFSClient.loadAndInitialize(account, neofsEndpoint);
    }

    @Test
    public void testGetBalance() throws Exception {
        neo.fs.v2.accounting.Types.Decimal balance = neoFSClient.getBalance(account.getECKeyPair().getPublicKey());
        assertThat(balance.getPrecision(), is(12));
        assertThat(balance.getValue(), is(0L));
        neo.fs.v2.accounting.Types.Decimal expected = neo.fs.v2.accounting.Types.Decimal.newBuilder()
                .setPrecision(12)
                .setValue(0)
                .build();
        assertEquals(expected, balance);
    }

}
