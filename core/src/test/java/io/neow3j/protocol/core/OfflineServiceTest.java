package io.neow3j.protocol.core;

import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.Neow3jExpress;
import io.neow3j.protocol.Neow3jService;
import io.neow3j.protocol.OfflineService;
import io.neow3j.protocol.core.response.NeoGetBlock;
import io.neow3j.protocol.exceptions.OfflineServiceException;
import io.neow3j.protocol.notifications.Notification;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OfflineServiceTest {

    private final String EXPECTED_OFFLINESERVICE_EXCEPTION_MESSAGE = "Invalid neow3j service for this function. You " +
            "cannot interact with a Neo node using an OfflineService instance.";

    private final Neow3j neow3j = Neow3j.build();
    private final Neow3jService service = OfflineService.newInstance();

    @Test
    public void testSend() {
        OfflineServiceException thrown = assertThrows(OfflineServiceException.class,
                () -> service.send(new Request(), NeoGetBlock.class));
        assertThat(thrown.getMessage(), containsString(EXPECTED_OFFLINESERVICE_EXCEPTION_MESSAGE));
    }

    @Test
    public void testSendAsync() {
        OfflineServiceException thrown = assertThrows(OfflineServiceException.class,
                () -> service.sendAsync(new Request(), NeoGetBlock.class));
        assertThat(thrown.getMessage(), containsString(EXPECTED_OFFLINESERVICE_EXCEPTION_MESSAGE));
    }

    @Test
    public void testSubscribeWithOnError() {
        OfflineServiceException thrown = assertThrows(OfflineServiceException.class,
                () -> service.subscribe(new Request(), "", Notification.class));
        assertThat(thrown.getMessage(), containsString(EXPECTED_OFFLINESERVICE_EXCEPTION_MESSAGE));
    }

    @Test
    public void testClose() {
        OfflineServiceException thrown = assertThrows(OfflineServiceException.class, service::close);
        assertThat(thrown.getMessage(), containsString(EXPECTED_OFFLINESERVICE_EXCEPTION_MESSAGE));
    }

    // Tests with a Neow3j instance that has an OfflineService. Neow3j's catch-up and observable methods will all call
    // the send method once they are subscribed to (which is already covered in this test class), thus, they do not
    // need to be tested in the scope of this class.

    @Test
    public void testNeow3jSend() {
        OfflineServiceException thrown = assertThrows(OfflineServiceException.class,
                () -> neow3j.getBestBlockHash().send());
        assertThat(thrown.getMessage(), containsString(EXPECTED_OFFLINESERVICE_EXCEPTION_MESSAGE));
    }

    @Test
    public void testNeow3jSendAsync() {
        OfflineServiceException thrown = assertThrows(OfflineServiceException.class,
                () -> neow3j.getBestBlockHash().sendAsync());
        assertThat(thrown.getMessage(), containsString(EXPECTED_OFFLINESERVICE_EXCEPTION_MESSAGE));
    }

    @Test
    public void testNeow3jShutdown() {
        OfflineServiceException thrown = assertThrows(OfflineServiceException.class, neow3j::shutdown);
        assertThat(thrown.getMessage(), containsString(EXPECTED_OFFLINESERVICE_EXCEPTION_MESSAGE));
    }

    @Test
    public void testExpressSend() {
        Neow3jExpress neow3jExpress = Neow3jExpress.build();
        OfflineServiceException thrown = assertThrows(OfflineServiceException.class, neow3jExpress::shutdown);
        assertThat(thrown.getMessage(), containsString(EXPECTED_OFFLINESERVICE_EXCEPTION_MESSAGE));
    }

}
