package io.neow3j.neofs;

import io.neow3j.neofs.sdk.NeoFSClient;
import io.neow3j.wallet.Account;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static io.neow3j.neofs.NeoFSIntegrationTestHelper.neofsEndpoint;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled // Remove for manual testing and once productive
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ClientIntegrationTest {

    @Test
    public void testDeleteClient() throws Exception {
        Account account = Account.fromWIF("KzAXTwrj1VxQA746zSSMCt9g3omSDfyKnwsayEducuHvKd1LR9mx");
        NeoFSClient neofsClient = NeoFSClient.loadAndInitialize(account, neofsEndpoint);

        boolean deleted = neofsClient.deleteClient();
        assertTrue(deleted);
    }

}
