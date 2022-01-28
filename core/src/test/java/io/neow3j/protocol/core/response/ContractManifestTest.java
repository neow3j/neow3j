package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.neow3j.protocol.ObjectMapperFactory;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;

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

    @Test
    public void serializeWithWildCardPermissionMethod() throws JsonProcessingException {
        ContractManifest.ContractPermission p =
                new ContractManifest.ContractPermission("NeoToken", asList("*"));
        ContractManifest m = new ContractManifest(null, null, null, null, null, asList(p), null, null);
        String s = ObjectMapperFactory.getObjectMapper().writeValueAsString(m);
        String exptected = "\"permissions\":[{\"contract\":\"NeoToken\",\"methods\":\"*\"}]";
        assertThat(s, Matchers.containsString(exptected));
    }

    @Test
    public void serializeWithNoPermissions() throws JsonProcessingException {
        ContractManifest m =
                new ContractManifest(null, null, null, null, null, new ArrayList<>(), null, null);
        String s = ObjectMapperFactory.getObjectMapper().writeValueAsString(m);
        assertThat(s, Matchers.containsString("\"permissions\":[]"));
    }

    @Test
    public void serializeWithPermissionsWithOneMethod() throws JsonProcessingException {
        ContractManifest.ContractPermission p =
                new ContractManifest.ContractPermission("NeoToken", asList("method"));
        ContractManifest m = new ContractManifest(null, null, null, null, null, asList(p), null, null);
        String s = ObjectMapperFactory.getObjectMapper().writeValueAsString(m);
        String exptected = "\"permissions\":[{\"contract\":\"NeoToken\",\"methods\":[\"method\"]}]";
        assertThat(s, Matchers.containsString(exptected));
    }

    @Test
    public void serializeWithMultiplePermissions() throws JsonProcessingException {
        ContractManifest.ContractPermission p =
                new ContractManifest.ContractPermission("NeoToken", asList("method"));
        ContractManifest.ContractPermission p2=
                new ContractManifest.ContractPermission("GasToken", asList("method1", "method2"));
        ContractManifest.ContractPermission p3 =
                new ContractManifest.ContractPermission("SomeToken", asList("*"));
        ContractManifest m = new ContractManifest(null, null, null, null, null, asList(p,p2,p3),
                null, null);
        String s = ObjectMapperFactory.getObjectMapper().writeValueAsString(m);
        String exptected = "\"permissions\":[" +
                "{\"contract\":\"NeoToken\",\"methods\":[\"method\"]}," +
                "{\"contract\":\"GasToken\",\"methods\":[\"method1\",\"method2\"]}," +
                "{\"contract\":\"SomeToken\",\"methods\":\"*\"}" +
                "]";
        assertThat(s, Matchers.containsString(exptected));
    }

}
