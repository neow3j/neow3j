package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.neow3j.protocol.ObjectMapperFactory;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertThat;

public class ContractManifestTest {

    @Test
    public void serializeWithWildCardTrust() throws JsonProcessingException {
        ContractManifest m =
                new ContractManifest(null, null, null, null, null, null, asList("*"), null);
        String s = ObjectMapperFactory.getObjectMapper().writeValueAsString(m);
        assertThat(s, Matchers.containsString("\"trusts\":\"*\""));
    }

    @Test
    public void serializeWithNoTrusts() throws JsonProcessingException {
        ContractManifest m =
                new ContractManifest(null, null, null, null, null, null, new ArrayList<>(), null);
        String s = ObjectMapperFactory.getObjectMapper().writeValueAsString(m);
        assertThat(s, Matchers.containsString("\"trusts\":[]"));
    }

    @Test
    public void serializeWithOneTrust() throws JsonProcessingException {
        ContractManifest m = new ContractManifest(null, null, null, null, null, null,
                Arrays.asList("69ecca587293047be4c59159bf8bc399985c160d"), null);
        String s = ObjectMapperFactory.getObjectMapper().writeValueAsString(m);
        assertThat(s, Matchers.containsString("\"trusts\":" +
                "[\"69ecca587293047be4c59159bf8bc399985c160d\"]"));
    }

    @Test
    public void serializeWithTwoTrust() throws JsonProcessingException {
        ContractManifest m = new ContractManifest(null, null, null, null, null, null,
                Arrays.asList("69ecca587293047be4c59159bf8bc399985c160d",
                        "69ecca587293047be4c59159bf8bc399985c160d"), null);
        String s = ObjectMapperFactory.getObjectMapper().writeValueAsString(m);
        assertThat(s, Matchers.containsString("\"trusts\":" +
                "[\"69ecca587293047be4c59159bf8bc399985c160d\"," +
                "\"69ecca587293047be4c59159bf8bc399985c160d\"]"));
    }


}