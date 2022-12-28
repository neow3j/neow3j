package io.neow3j.neofs;

import com.google.protobuf.InvalidProtocolBufferException;
import io.neow3j.neofs.sdk.NeoFSClient;
import io.neow3j.neofs.sdk.exceptions.UnexpectedResponseTypeException;
import io.neow3j.wallet.Account;
import neo.fs.v2.tombstone.Types;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static io.neow3j.neofs.NeoFSIntegrationTestHelper.createSimpleContainer;
import static io.neow3j.neofs.NeoFSIntegrationTestHelper.neofsEndpoint;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Disabled // Remove for manual testing and once productive
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ObjectIntegrationTest {

    private static final String RESOURCE_DIR = "neofs";

    private Account account;
    private NeoFSClient neofsClient;
    private String containerId;

    @BeforeAll
    public void setup() throws Exception {
        account = Account.fromWIF("KzAXTwrj1VxQA746zSSMCt9g3omSDfyKnwsayEducuHvKd1LR9mx");
        neofsClient = NeoFSClient.loadAndInitialize(account, neofsEndpoint);
        containerId = neofsClient.createContainer(createSimpleContainer(account));
        Thread.sleep(2000); // Wait for container creation to persist.
    }

    @Test
    public void testCreateAndReadObject() throws InterruptedException {
        String testText = "Neow3j is awesome!";
        String objectId = neofsClient.createObject(containerId, testText.getBytes(UTF_8), account);
        Thread.sleep(2000); // Wait for object creation to persist.
        byte[] readBytes = neofsClient.readObject(containerId, objectId, account);
        assertThat(new String(readBytes), is(testText));
    }

    @Test
    public void testCreateAndReadObjectFromFile() throws URISyntaxException, IOException, InterruptedException {
        String filename = "neow3j-neo3.png";
        String neow3jImage = Paths.get(RESOURCE_DIR, filename).toString();
        File neow3jFile = new File(getClass().getClassLoader().getResource(neow3jImage).toURI());

        byte[] expected = Files.readAllBytes(neow3jFile.toPath());

        String objectId = neofsClient.createObject(containerId, expected, account);
        Thread.sleep(2000); // Wait for object creation to persist.

        byte[] readBytes = neofsClient.readObject(containerId, objectId, account);
        assertArrayEquals(readBytes, expected);
    }

    @Test
    public void testCreateAndDeleteObject() throws InterruptedException, InvalidProtocolBufferException {
        String testText = "Neow3j is awesome!";

        String objectId = neofsClient.createObject(containerId, testText.getBytes(UTF_8), account);
        Thread.sleep(2000); // Wait for object creation to persist.
        byte[] readBytes = neofsClient.readObject(containerId, objectId, account);
        assertThat(new String(readBytes), is(testText));

        String tombstoneId = neofsClient.deleteObject(containerId, objectId, account);
        byte[] tombstoneBytes = neofsClient.readObject(containerId, tombstoneId, account);
        Types.Tombstone tombstone = Types.Tombstone.parseFrom(tombstoneBytes);
        assertNotNull(tombstone);

        UnexpectedResponseTypeException thrown = assertThrows(UnexpectedResponseTypeException.class,
                () -> neofsClient.readObject(containerId, objectId, account));
        assertThat(thrown.getMessage(), containsString("*apistatus.ObjectAlreadyRemoved"));
    }

}
