package com.axlabs.neow3j.protocol.core;

import com.axlabs.neow3j.protocol.Neow3j;
import com.axlabs.neow3j.protocol.Neow3jService;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class JsonRpc2_0Neow3jTest {

    private ScheduledExecutorService scheduledExecutorService
            = mock(ScheduledExecutorService.class);
    private Neow3jService service = mock(Neow3jService.class);

    private Neow3j neow3j = Neow3j.build(service, 10, scheduledExecutorService);

    @Test
    public void testStopExecutorOnShutdown() throws Exception {
        neow3j.shutdown();

        verify(scheduledExecutorService).shutdown();
        verify(service).close();
    }

    @Test(expected = RuntimeException.class)
    public void testThrowsRuntimeExceptionIfFailedToCloseService() throws Exception {
        doThrow(new IOException("Failed to close"))
                .when(service).close();

        neow3j.shutdown();
    }
}