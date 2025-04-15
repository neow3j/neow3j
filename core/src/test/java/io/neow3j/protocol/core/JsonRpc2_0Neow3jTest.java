package io.neow3j.protocol.core;

import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.Neow3jService;
import io.neow3j.protocol.core.response.NeoGetVersion;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;

import static io.neow3j.protocol.Neow3jConfig.defaultNeow3jConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JsonRpc2_0Neow3jTest {

    private final ScheduledExecutorService scheduledExecutorService = mock(ScheduledExecutorService.class);
    private final Neow3jService service = mock(Neow3jService.class);

    private Neow3j neow3j;

    @BeforeAll
    public void setUp() throws IOException {
        Mockito.when(service.send(Mockito.any(), Mockito.eq(NeoGetVersion.class)))
                .thenReturn(getDummyNeoGetVersionResponse());

        neow3j = Neow3j.build(service, defaultNeow3jConfig()
                .setPollingInterval(10)
                .setScheduledExecutorService(scheduledExecutorService));
    }

    @Test
    public void testStopExecutorOnShutdown() throws Exception {
        neow3j.shutdown();

        verify(scheduledExecutorService).shutdown();
        verify(service).close();
    }

    @Test
    public void testThrowsRuntimeExceptionIfFailedToCloseService() throws IOException {
        doThrow(new IOException()).when(service).close();

        RuntimeException thrown = assertThrows(RuntimeException.class, neow3j::shutdown);
        assertThat(thrown.getMessage(), is("Failed to close neow3j service"));
    }

    private NeoGetVersion getDummyNeoGetVersionResponse() {
        NeoGetVersion.NeoVersion.Protocol protocol = new NeoGetVersion.NeoVersion.Protocol();
        protocol.setNetwork(768L);
        protocol.setMilliSecondsPerBlock(1000L);

        NeoGetVersion.NeoVersion version = new NeoGetVersion.NeoVersion();
        version.setProtocol(protocol);

        NeoGetVersion neoGetVersion = new NeoGetVersion();
        neoGetVersion.setResult(version);
        return neoGetVersion;
    }

}
