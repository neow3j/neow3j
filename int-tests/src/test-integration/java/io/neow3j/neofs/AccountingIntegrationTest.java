package io.neow3j.neofs;

import com.google.protobuf.InvalidProtocolBufferException;
import io.neow3j.neofs.sdk.NeoFSClient;
import io.neow3j.wallet.Account;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static io.neow3j.neofs.NeoFSIntegrationTestHelper.neofsEndpoint;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Disabled // Remove for manual testing and once productive
public class AccountingIntegrationTest {

    private static final Account account = Account.create();
    private static NeoFSClient neofsClient;

    @BeforeAll
    public static void setup() throws Exception {
        neofsClient = NeoFSClient.loadAndInitialize(account, neofsEndpoint);
    }

    @AfterAll
    public static void after() {
        neofsClient.deleteClient();
    }

    @Test
    public void testBalance() throws InvalidProtocolBufferException {
        neo.fs.v2.accounting.Types.Decimal balance = neofsClient.getBalance(account.getECKeyPair().getPublicKey());
        assertThat(balance.getPrecision(), is(12));
        assertThat(balance.getValue(), is(0L));

        neo.fs.v2.accounting.Types.Decimal expected = neo.fs.v2.accounting.Types.Decimal.newBuilder()
                .setPrecision(12)
                .setValue(0)
                .build();
        assertEquals(expected, balance);
    }

}
