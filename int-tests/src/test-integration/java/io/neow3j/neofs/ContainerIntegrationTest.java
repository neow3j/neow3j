package io.neow3j.neofs;

import com.google.protobuf.InvalidProtocolBufferException;
import io.neow3j.neofs.sdk.BasicACL;
import io.neow3j.neofs.sdk.NeoFSClient;
import io.neow3j.neofs.sdk.NeoFSHelper;
import io.neow3j.neofs.sdk.exceptions.NeoFSLibraryError;
import io.neow3j.neofs.sdk.exceptions.UnexpectedResponseTypeException;
import io.neow3j.wallet.Account;
import neo.fs.v2.container.Types;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Date;

import static io.neow3j.neofs.NeoFSIntegrationTestHelper.neofsEndpoint;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ContainerIntegrationTest {

    private NeoFSClient neoFSClient;
    private Account account;

    @BeforeAll
    public void setUp() throws Throwable {
        account = Account.fromWIF("KzAXTwrj1VxQA746zSSMCt9g3omSDfyKnwsayEducuHvKd1LR9mx");
        neoFSClient = NeoFSClient.loadAndInitialize(account, neofsEndpoint);
    }

    private Types.Container createSimpleContainer(Account ownerAccount) {
        return Types.Container.newBuilder()
                .setVersion(NeoFSHelper.createVersion())
                .setNonce(NeoFSHelper.createNonce())
                .setOwnerId(NeoFSHelper.createOwnerId(ownerAccount))
                .setBasicAcl(BasicACL.PUBLIC_BASIC_NAME.value())
                .setPlacementPolicy(neo.fs.v2.netmap.Types.PlacementPolicy.newBuilder()
                        .setContainerBackupFactor(0)
                        .addReplicas(neo.fs.v2.netmap.Types.Replica.newBuilder()
                                .setCount(0)
                                .build())
                        .build())
                .addAttributes(Types.Container.Attribute.newBuilder()
                        .setKey("CreatedAt")
                        .setValue(new Date().toString())
                        .build())
                .build();
    }

    @Test
    public void testCreateContainer() throws Exception {
        String containerId = neoFSClient.createContainer(createSimpleContainer(account));
        assertNotNull(containerId);
        assertFalse(containerId.isEmpty());
    }

    @Test
    public void testGetContainer() throws Exception {
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
    public void testDeleteContainer() throws NeoFSLibraryError, InvalidProtocolBufferException,
            UnexpectedResponseTypeException, InterruptedException {

        Types.Container simpleContainer = createSimpleContainer(account);
        String containerId = neoFSClient.createContainer(simpleContainer);
        Thread.sleep(1000);

        Types.Container container = neoFSClient.getContainer(containerId);
        assertEquals(simpleContainer, container);

        assertTrue(neoFSClient.deleteContainer(containerId));
    }

}
