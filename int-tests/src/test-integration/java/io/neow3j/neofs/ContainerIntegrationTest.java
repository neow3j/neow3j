package io.neow3j.neofs;

import com.google.protobuf.InvalidProtocolBufferException;
import io.neow3j.neofs.sdk.Await;
import io.neow3j.neofs.sdk.NeoFSClient;
import io.neow3j.wallet.Account;
import neo.fs.v2.container.Types;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static io.neow3j.neofs.NeoFSIntegrationTestHelper.createSimpleContainer;
import static io.neow3j.neofs.NeoFSIntegrationTestHelper.neofsEndpoint;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled // Remove for manual testing and once productive
public class ContainerIntegrationTest {

    private static final Account account = Account.create();
    private static NeoFSClient neofsClient;

    @BeforeAll
    public static void setUp() throws Exception {
        neofsClient = NeoFSClient.loadAndInitialize(account, neofsEndpoint);
    }

    @AfterAll
    public static void after() {
        neofsClient.deleteClient();
    }

    @Test
    public void testCreateContainer() throws InvalidProtocolBufferException {
        String containerId = neofsClient.createContainer(createSimpleContainer(account));
        Await.waitUntilContainerPersists(neofsClient, containerId);

        assertNotNull(containerId);
        assertFalse(containerId.isEmpty());

        assertTrue(neofsClient.deleteContainer(containerId));
    }

    @Test
    public void testGetContainer() throws InvalidProtocolBufferException {
        Types.Container simpleContainer = createSimpleContainer(account);
        String containerId = neofsClient.createContainer(simpleContainer);

        Await.waitUntilContainerPersists(neofsClient, containerId);

        assertNotNull(containerId);
        assertFalse(containerId.isEmpty());

        Types.Container container = neofsClient.getContainer(containerId);
        assertEquals(simpleContainer, container);

        assertTrue(neofsClient.deleteContainer(containerId));
    }

    @Test
    public void testDeleteContainer() throws InvalidProtocolBufferException {
        Types.Container simpleContainer = createSimpleContainer(account);
        String containerId = neofsClient.createContainer(simpleContainer);
        Await.waitUntilContainerPersists(neofsClient, containerId);

        Types.Container container = neofsClient.getContainer(containerId);
        assertEquals(simpleContainer, container);

        assertTrue(neofsClient.deleteContainer(containerId));
    }

    @Test
    public void testListContainers() throws IOException {
        ArrayList<String> containerIdsCreated = new ArrayList<>();
        int nrContainers = 10;
        for (int i = 0; i < nrContainers; i++) {
            Types.Container container = createSimpleContainer(account);
            String containerId = neofsClient.createContainer(container);
            Await.waitUntilContainerPersists(neofsClient, containerId);
            containerIdsCreated.add(containerId);
        }

        List<String> listOfContainerIds = neofsClient.listContainers(account.getECKeyPair().getPublicKey());

        assertThat(listOfContainerIds, hasSize(greaterThanOrEqualTo(1)));
        assertThat(listOfContainerIds, containsInAnyOrder(containerIdsCreated.toArray()));
        for (String containerId : containerIdsCreated) {
            assertTrue(neofsClient.deleteContainer(containerId));
        }
    }

}
