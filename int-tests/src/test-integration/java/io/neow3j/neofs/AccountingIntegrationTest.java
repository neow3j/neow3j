package io.neow3j.neofs;

import io.neow3j.neofs.sdk.NeoFSClient;
import io.neow3j.wallet.Account;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static io.neow3j.neofs.NeoFSIntegrationTestHelper.neofsEndpoint;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Disabled // Remove for manual testing and once productive
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AccountingIntegrationTest {

    @Test
    public void testGetBalance() throws Exception {
        Account account = Account.fromWIF("KzAXTwrj1VxQA746zSSMCt9g3omSDfyKnwsayEducuHvKd1LR9mx");
        NeoFSClient neoFSClient = NeoFSClient.loadAndInitialize(account, neofsEndpoint);

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
