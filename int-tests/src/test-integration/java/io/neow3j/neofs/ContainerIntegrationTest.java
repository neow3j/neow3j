package io.neow3j.neofs;

import com.google.protobuf.InvalidProtocolBufferException;
import io.neow3j.neofs.sdk.NeoFSClient;
import io.neow3j.wallet.Account;
import neo.fs.v2.container.Types;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.util.List;

import static io.neow3j.neofs.NeoFSIntegrationTestHelper.createSimpleContainer;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled // Remove for manual testing and once productive
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ContainerIntegrationTest {

    @RegisterExtension
    public static NeoFSTestExtension ct = new NeoFSTestExtension();

    private NeoFSClient neoFSClient;
    private Account account;

    @BeforeAll
    public void setUp() throws Exception {
        account = Account.fromWIF("KzAXTwrj1VxQA746zSSMCt9g3omSDfyKnwsayEducuHvKd1LR9mx");
        neoFSClient = NeoFSClient.loadAndInitialize(account, ct.getNeofsEndpoint());
    }

    @Test
    public void testCreateContainer() throws InvalidProtocolBufferException {
        String containerId = neoFSClient.createContainer(createSimpleContainer(account));
        assertNotNull(containerId);
        assertFalse(containerId.isEmpty());
    }

    @Test
    public void testGetContainer() throws InvalidProtocolBufferException, InterruptedException {
        Types.Container simpleContainer = createSimpleContainer(account);
        String containerId = neoFSClient.createContainer(simpleContainer);

        // Todo: Implement class that handles waiting until request is persisted.
        Thread.sleep(1000);

        assertNotNull(containerId);
        assertFalse(containerId.isEmpty());

        Types.Container container = neoFSClient.getContainer(containerId);
        assertEquals(simpleContainer, container);
    }

    @Test
    public void testDeleteContainer() throws InvalidProtocolBufferException, InterruptedException {
        Types.Container simpleContainer = createSimpleContainer(account);
        String containerId = neoFSClient.createContainer(simpleContainer);
        Thread.sleep(1000);

        Types.Container container = neoFSClient.getContainer(containerId);
        assertEquals(simpleContainer, container);

        assertTrue(neoFSClient.deleteContainer(containerId));
    }

    @Test
    public void testListContainers() throws IOException, InterruptedException {
        Types.Container firstContainer = createSimpleContainer(account);
        String firstContainerId = neoFSClient.createContainer(firstContainer);
        Thread.sleep(2000);

        List<String> ids = neoFSClient.listContainers(account.getECKeyPair().getPublicKey());

        assertThat(ids, hasSize(greaterThanOrEqualTo(1)));
        assertThat(ids, hasItem(firstContainerId));
    }

}
