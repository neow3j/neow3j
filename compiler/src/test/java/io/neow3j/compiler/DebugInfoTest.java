package io.neow3j.compiler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.neow3j.compiler.DebugInfo.Event;
import io.neow3j.compiler.DebugInfo.Method;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class DebugInfoTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void deserializeAndSerializeDebugInfoFile() throws IOException, URISyntaxException {
        String debugInfoFileName = "test.debug.json";
        InputStream inStream = this.getClass().getClassLoader()
                .getResourceAsStream(debugInfoFileName);
        DebugInfo dbgnfo = objectMapper.readValue(inStream, DebugInfo.class);

        assertThat(dbgnfo.getDocuments(), hasSize(7));
        assertThat(dbgnfo.getDocuments().get(0),
                is("/templates/Template.NEP17.CSharp/NEP17.Helpers.cs"));
        assertThat(dbgnfo.getHash(), is("0xf798b2b69bfb380573787c6e177e4571643993c4"));
        assertThat(dbgnfo.getEvents(), hasSize(1));

        List<String> staticVariables = dbgnfo.getStaticVariables();
        assertThat(staticVariables, hasSize(2));
        assertThat(staticVariables.get(0), is("owner,Hash160,0"));
        assertThat(staticVariables.get(1), is("ctx,InteropInterface,1"));

        List<Method> methods = dbgnfo.getMethods();
        assertThat(methods, hasSize(10));
        Method method = methods.get(0);
        assertThat(method.getId(),
                is("System.Boolean Template.NEP17.CSharp.NEP17::ValidateAddress(System.Byte[])"));
        assertThat(method.getName(), is("Template.NEP17.CSharp.NEP17,validateAddress"));
        assertThat(method.getRange(), is("0-35"));
        assertThat(method.getParams(), hasSize(1));
        assertThat(method.getParams().get(0), is("address,ByteArray"));
        assertThat(method.getReturnType(), is("Boolean"));
        assertThat(method.getVariables(), empty());
        assertThat(method.getSequencePoints(), hasSize(1));
        assertThat(method.getSequencePoints().get(0), is("3[0]8:64-8:115"));
        method = methods.get(6);
        assertThat(method.getVariables(), hasSize(2));
        assertThat(method.getVariables().get(0), is("notification,Array"));
        assertThat(method.getVariables().get(1), is("amount,Integer"));

        List<Event> events = dbgnfo.getEvents();
        assertThat(events, hasSize(1));
        Event event = events.get(0);
        assertThat(event.getId(), is("Template.NEP17.CSharp.NEP17::OnTransfer"));
        assertThat(event.getName(), is("Template.NEP17.CSharp.NEP17,Transfer"));
        assertThat(event.getParams(), hasSize(3));
        assertThat(event.getParams().get(0), is("arg1,ByteArray"));

        File dbgInfoFile = new File(this.getClass().getClassLoader().getResource(debugInfoFileName)
                .toURI());
        String dbgInfoJsonString = new String(Files.readAllBytes(dbgInfoFile.toPath()))
                .replaceAll("\\s+", "");
        String serialized = objectMapper.writeValueAsString(dbgnfo).replaceAll("\\s+", "");
        assertThat(serialized, is(dbgInfoJsonString));
    }

}
