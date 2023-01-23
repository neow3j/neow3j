package io.neow3j.neofs;

import com.google.protobuf.InvalidProtocolBufferException;
import io.neow3j.neofs.lib.NeoFSLib;
import io.neow3j.neofs.sdk.NeoFSClient;
import io.neow3j.wallet.Account;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Random;

import static io.neow3j.neofs.NeoFSIntegrationTestHelper.neofsEndpoint;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled // Remove for manual testing and once productive
public class ClientIntegrationTest {

    private static final Account account = Account.create();
    private static NeoFSLib neofsLib;
    private static NeoFSClient neofsClient;

    @BeforeAll
    public static void setup() throws Exception {
        neofsLib = new NeoFSLib();
        neofsClient = NeoFSClient.initialize(neofsLib, account, neofsEndpoint);
    }

    @AfterAll
    public static void after() {
        neofsClient.deleteClient();
    }

    @Test
    public void createAndDeleteMultipleClients() throws InvalidProtocolBufferException {
        ArrayList<String> clientIds = new ArrayList<>();
        int nrClients = 10;
        for (int i = 0; i < nrClients; i++) {
            String clientId = neofsClient.createClient(neofsLib, account, neofsEndpoint);
            clientIds.add(clientId);
        }

        int randomIndex = new Random().nextInt(nrClients);
        String usingClientId = clientIds.get(randomIndex);
        NeoFSClient neofsClient = new NeoFSClient(neofsLib, usingClientId);
        assertNotNull(neofsClient.getNetworkInfo());
        for (String clientId : clientIds) {
            neofsClient = new NeoFSClient(neofsLib, clientId);
            assertTrue(neofsClient.deleteClient());
        }
    }

}
