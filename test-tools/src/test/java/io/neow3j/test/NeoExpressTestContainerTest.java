package io.neow3j.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class NeoExpressTestContainerTest {

    @Test
    public void testDefaultConfigSetsRpcLimitsAtRoot() throws IOException {
        JsonNode config = new ObjectMapper().readTree(getClass().getResourceAsStream("/default.neo-express"));

        assertThat(config.path("settings").path("rpc.MaxGasInvoke").asInt(), is(100000));
        assertThat(config.path("settings").path("rpc.MaxFee").asDouble(), is(0.2));
        assertThat(config.path("consensus-nodes").get(0).path("settings").has("rpc.MaxGasInvoke"), is(false));
        assertThat(config.path("consensus-nodes").get(0).path("settings").has("rpc.MaxFee"), is(false));
    }

    @Test
    public void testAddDefaultConfigInput() {
        ArrayList<String> commands = new ArrayList<>();
        commands.add("neoxp");
        commands.add("oracle");
        commands.add("enable");
        commands.add("genesis");

        NeoExpressTestContainer.addDefaultConfigInput(commands);

        assertThat(commands, hasSize(6));
        assertThat(commands.get(4), is("--input"));
        assertThat(commands.get(5), is(NeoExpressTestContainer.NEOXP_CONFIG_DEST));
    }

    @Test
    public void testAddDefaultConfigInput_withExistingInputFlag() {
        ArrayList<String> commands = new ArrayList<>();
        commands.add("neoxp");
        commands.add("oracle");
        commands.add("enable");
        commands.add("--input");
        commands.add(NeoExpressTestContainer.NEOXP_CONFIG_DEST);
        commands.add("genesis");

        NeoExpressTestContainer.addDefaultConfigInput(commands);

        assertThat(commands, hasSize(6));
        assertThat(commands.get(0), is("neoxp"));
        assertThat(commands.get(1), is("oracle"));
        assertThat(commands.get(2), is("enable"));
        assertThat(commands.get(3), is("--input"));
        assertThat(commands.get(4), is(NeoExpressTestContainer.NEOXP_CONFIG_DEST));
        assertThat(commands.get(5), is("genesis"));
    }

    @Test
    public void testAddNeoxpCommand() {
        ArrayList<String> commands = new ArrayList<>();
        commands.add("create");

        NeoExpressTestContainer.addNeoxpCommand(commands);

        assertThat(commands, hasSize(2));
        assertThat(commands.get(0), is("neoxp"));
        assertThat(commands.get(1), is("create"));

        NeoExpressTestContainer.addNeoxpCommand(commands);

        assertThat(commands, hasSize(2));
        assertThat(commands.get(0), is("neoxp"));
        assertThat(commands.get(1), is("create"));
    }

}
