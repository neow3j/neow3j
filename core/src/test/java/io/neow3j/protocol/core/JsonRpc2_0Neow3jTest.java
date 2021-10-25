package io.neow3j.protocol.core;

import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.Neow3jConfig;
import io.neow3j.protocol.Neow3jService;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class JsonRpc2_0Neow3jTest {

    private ScheduledExecutorService scheduledExecutorService
            = mock(ScheduledExecutorService.class);
    private Neow3jService service = mock(Neow3jService.class);

    private Neow3j neow3j = Neow3j.build(service, new Neow3jConfig()
            .setPollingInterval(10)
            .setScheduledExecutorService(scheduledExecutorService));

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testStopExecutorOnShutdown() throws Exception {
        neow3j.shutdown();

        verify(scheduledExecutorService).shutdown();
        verify(service).close();
    }

    @Test
    public void testThrowsRuntimeExceptionIfFailedToCloseService() throws Exception {
        doThrow(new IOException("Failed to close"))
                .when(service).close();

        exceptionRule.expect(RuntimeException.class);
        exceptionRule.expectMessage("Failed to close");
        neow3j.shutdown();
    }

}
