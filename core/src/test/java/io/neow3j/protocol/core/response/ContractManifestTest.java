package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.protocol.ObjectMapperFactory;
import io.neow3j.protocol.core.response.ContractManifest.ContractGroup;
import io.neow3j.types.Hash160;
import io.neow3j.wallet.Account;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        ContractManifest.ContractPermission p2 =
                new ContractManifest.ContractPermission("GasToken", asList("method1", "method2"));
        ContractManifest.ContractPermission p3 =
                new ContractManifest.ContractPermission("SomeToken", asList("*"));
        ContractManifest m = new ContractManifest(null, null, null, null, null, asList(p, p2, p3), null, null);
        String s = ObjectMapperFactory.getObjectMapper().writeValueAsString(m);
        String exptected = "\"permissions\":[" +
                "{\"contract\":\"NeoToken\",\"methods\":[\"method\"]}," +
                "{\"contract\":\"GasToken\",\"methods\":[\"method1\",\"method2\"]}," +
                "{\"contract\":\"SomeToken\",\"methods\":\"*\"}" +
                "]";
        assertThat(s, Matchers.containsString(exptected));
    }

    @Test
    public void testSetGroups() {
        ContractManifest manifest = new ContractManifest("TestContract", null, null, null, null, null, null, null);
        assertThat(manifest.getGroups(), hasSize(0));

        // parameters used:
        // group wif L1QfU2mHD3MvR3aqxMa7wzedePH8bpkrWmRWFQBEWcmPFrEorwjF
        // sender wif L3x3KgTGB9L5u1SZDexDWUuXbegMRwpRP8tVSzq3vg7RrDbNLPQw
        // nef check sum 2173916933L

        manifest.setGroups(asList(new ContractGroup(
                "0x025f3953adaf5155d9ee63ce40643837219286636fe28d6024c4b1d28f675a12e2",
                "uIBPwD2tYw8ESy1GXHksHD6XrzssQOJp0H0sBSJ76CnAxtf1VgZDJ45OAGXZynamiBpNS/f8Lk5aAJ2viB5XxA=="))
        );
        assertThat(manifest.getGroups(), hasSize(1));
    }

    @Test
    public void testCreateGroup() {
        ContractManifest manifest = new ContractManifest("TestContract", null, null, null, null, null, null, null);
        assertThat(manifest.getGroups(), hasSize(0));

        // parameters used:
        // group1 wif L1QfU2mHD3MvR3aqxMa7wzedePH8bpkrWmRWFQBEWcmPFrEorwjF
        // group2 wif L2v2C2RenZgZLRSFSTVK4Ngk68E8PDXVQvJ1ijTp92EasBvXk7R7
        // sender wif L3x3KgTGB9L5u1SZDexDWUuXbegMRwpRP8tVSzq3vg7RrDbNLPQw
        // nef check sum 2173916934L

        manifest.setGroups(asList(new ContractGroup(
                "0x025f3953adaf5155d9ee63ce40643837219286636fe28d6024c4b1d28f675a12e2",
                "tBscf3to/EMw/lLSM07Ko9WPeegYJds76LIcZusDXpwPbvCJUdtiLf+Cf5rF41WuDyUoC5mfOkUOrKHS1y+tWQ=="))
        );
        assertThat(manifest.getGroups(), hasSize(1));

        Hash160 deploySender = new Hash160("f3e641ce66b1276119296da872f5e97c11538bcb");
        ECKeyPair group2KeyPair =
                Account.fromWIF("L2v2C2RenZgZLRSFSTVK4Ngk68E8PDXVQvJ1ijTp92EasBvXk7R7").getECKeyPair();
        ContractGroup group2 = manifest.createGroup(group2KeyPair, deploySender, 2173916934L);
        manifest.setGroups(asList(group2));

        assertThat(manifest.getGroups(), hasSize(1));
        assertThat(manifest.getGroups().get(0).getPubKey(),
                is("03e237d84371612e3d2ce2a71b3c150ded51be3e93d34c494d1424bdae349900a9"));
        assertThat(manifest.getGroups().get(0).getSignature(),
                is("lzrUouvaXRl0IM7dhN3PaIUZ9LL9AMw7/1ZknI60BMlPXRW99l246N69F5MW3kAiXFyk0N4cte//Ajfu1ZZ2KQ=="));
    }

    @Test
    public void testContractGroupCheckPubKey() {
        String invalidPubKey = "0x03df97fb65edef80f2fc99ac4ae4efd1a30c519c07f8b3621782787f2881a9b7";
        String signature1 = "1USFvVTJEgo1MUqpm6ZEx/NOAh4eyVfit5fg7FAipIYVbPVH8railarQ7THMjKOPbtpC6SyUs4OmpSF8Khc3jA==";

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> new ContractGroup(invalidPubKey, signature1));
        assertThat(thrown.getMessage(), is("The provided value is not a valid public key: " +
                "03df97fb65edef80f2fc99ac4ae4efd1a30c519c07f8b3621782787f2881a9b7"));
    }

    @Test
    public void testContractGroupCheckBase64Signature() {
        String pubKey = "03df97fb65edef80f2fc99ac4ae4efd1a30c519c07f8b3621782787f2881a9b7b5";
        String invalidBase64 = "Neow=";

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> new ContractGroup(pubKey, invalidBase64));
        assertThat(thrown.getMessage(), is(
                "Invalid signature: Neow=. Please provide a valid signature in " +
                        "base64 format."));
    }

}
