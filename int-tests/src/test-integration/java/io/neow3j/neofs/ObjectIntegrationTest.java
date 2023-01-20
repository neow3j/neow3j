package io.neow3j.neofs;

import com.google.protobuf.InvalidProtocolBufferException;
import io.neow3j.neofs.sdk.Await;
import io.neow3j.neofs.sdk.NeoFSClient;
import io.neow3j.neofs.sdk.exceptions.UnexpectedResponseTypeException;
import io.neow3j.wallet.Account;
import neo.fs.v2.tombstone.Types;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static io.neow3j.neofs.NeoFSIntegrationTestHelper.createSimpleContainer;
import static io.neow3j.neofs.NeoFSIntegrationTestHelper.neofsEndpoint;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Disabled // Remove for manual testing and once productive
public class ObjectIntegrationTest {

    private static final String RESOURCE_DIR = "neofs";

    private static final Account account = Account.create();
    private static NeoFSClient neofsClient;
    private static String containerId;

    @BeforeAll
    public static void setup() throws Exception {
        neofsClient = NeoFSClient.loadAndInitialize(account, neofsEndpoint);
        containerId = neofsClient.createContainer(createSimpleContainer(account));
        Await.waitUntilContainerPersists(neofsClient, containerId);
    }

    @AfterAll
    public static void after() {
        neofsClient.deleteContainer(containerId);
        neofsClient.deleteClient();
    }

    @Test
    public void testCreateAndReadObject() {
        String testText = "Neow3j is awesome!";
        String objectId = neofsClient.createObject(containerId, testText.getBytes(UTF_8), account);
        Await.waitUntilObjectPersists(neofsClient, containerId, objectId, account);
        byte[] readBytes = neofsClient.readObject(containerId, objectId, account);
        assertThat(new String(readBytes), is(testText));
    }

    @Test
    public void testCreateAndReadObjectFromFile() throws URISyntaxException, IOException {
        String filename = "neow3j-neo3.png";
        String neow3jImage = Paths.get(RESOURCE_DIR, filename).toString();
        File neow3jFile = new File(getClass().getClassLoader().getResource(neow3jImage).toURI());

        byte[] expected = Files.readAllBytes(neow3jFile.toPath());

        String objectId = neofsClient.createObject(containerId, expected, account);
        Await.waitUntilObjectPersists(neofsClient, containerId, objectId, account);

        byte[] readBytes = neofsClient.readObject(containerId, objectId, account);
        assertArrayEquals(readBytes, expected);
    }

    @Test
    public void testCreateAndDeleteObject() throws InvalidProtocolBufferException {
        String testText = "Neow3j is awesome!";
        String objectId = neofsClient.createObject(containerId, testText.getBytes(UTF_8), account);
        Await.waitUntilObjectPersists(neofsClient, containerId, objectId, account);
        byte[] readBytes = neofsClient.readObject(containerId, objectId, account);
        assertThat(new String(readBytes), is(testText));

        String tombstoneId = neofsClient.deleteObject(containerId, objectId, account);
        Await.waitUntilObjectPersists(neofsClient, containerId, tombstoneId, account);
        byte[] tombstoneBytes = neofsClient.readObject(containerId, tombstoneId, account);
        Types.Tombstone tombstone = Types.Tombstone.parseFrom(tombstoneBytes);
        assertNotNull(tombstone);

        UnexpectedResponseTypeException thrown = assertThrows(UnexpectedResponseTypeException.class,
                () -> neofsClient.readObject(containerId, objectId, account));
        assertThat(thrown.getMessage(), containsString("object already removed"));
    }

}
