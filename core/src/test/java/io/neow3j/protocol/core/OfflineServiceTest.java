package io.neow3j.protocol.core;

import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.Neow3jExpress;
import io.neow3j.protocol.Neow3jService;
import io.neow3j.protocol.OfflineService;
import io.neow3j.protocol.core.response.NeoGetBlock;
import io.neow3j.protocol.exceptions.OfflineServiceException;
import io.neow3j.protocol.notifications.Notification;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    // Tests with a Neow3j instance that has an OfflineService

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
    public void testNeow3jSubscribeWithOnError() throws IOException, InterruptedException {
        AtomicBoolean onErrorExecuted = new AtomicBoolean(false);
        neow3j.subscribeToNewBlocksObservable(true).subscribe(NeoGetBlock::getBlock, (e) -> onErrorExecuted.set(true));
        Thread.sleep(100);
        assertTrue(onErrorExecuted.get());
    }

    @Test
    public void testNeow3jCatchupAndSubscribeWithOnError() throws InterruptedException {
        AtomicBoolean onErrorExecuted = new AtomicBoolean(false);
        neow3j.catchUpToLatestAndSubscribeToNewBlocksObservable(BigInteger.ZERO, true)
                .subscribe(NeoGetBlock::getBlock, (e) -> onErrorExecuted.set(true));
        Thread.sleep(100);
        assertTrue(onErrorExecuted.get());
    }

    @Test
    public void testNeow3jReplayBlocksObservableSubscribe() throws InterruptedException {
        AtomicBoolean onErrorExecuted = new AtomicBoolean(false);
        neow3j.replayBlocksObservable(BigInteger.ZERO, BigInteger.ZERO, true)
                .subscribe(NeoGetBlock::getBlock, (e) -> onErrorExecuted.set(true));
        Thread.sleep(100);
        assertTrue(onErrorExecuted.get());
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
