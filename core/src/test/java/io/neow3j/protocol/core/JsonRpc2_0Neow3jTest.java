package io.neow3j.protocol.core;

import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.Neow3jConfig;
import io.neow3j.protocol.Neow3jService;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThrows;
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

    @Test
    public void testStopExecutorOnShutdown() throws Exception {
        neow3j.shutdown();

        verify(scheduledExecutorService).shutdown();
        verify(service).close();
    }

    @Test
    public void testThrowsRuntimeExceptionIfFailedToCloseService() throws IOException {
        doThrow(new IOException()).when(service).close();

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> neow3j.shutdown());
        assertThat(thrown.getMessage(), is("Failed to close neow3j service"));
    }

}
