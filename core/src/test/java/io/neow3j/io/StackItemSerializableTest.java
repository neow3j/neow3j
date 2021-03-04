package io.neow3j.io;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.neow3j.io.exceptions.DeserializationException;
import io.neow3j.protocol.core.methods.response.ContractManifest.ContractABI;
import io.neow3j.protocol.core.methods.response.ContractManifest.ContractGroup;
import io.neow3j.protocol.core.methods.response.StackItem;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import org.junit.Ignore;
import org.junit.Test;

public class StackItemSerializableTest {


    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Ignore
    @Test
    public void testContractGroup() throws IOException, DeserializationException {
        String jsonStr = getContentAsString("ContractGroup_stackitem.json");
        StackItem rawItem = OBJECT_MAPPER.readValue(jsonStr, StackItem.class);

        ContractGroup cg = StackItemSerializableInterface.from(rawItem, ContractGroup.class);
        assertThat(cg.getPubKey(), is("test"));
        assertThat(cg.getSignature(), is("test"));
    }

    @Ignore
    @Test
    public void testContractABI() throws IOException, DeserializationException {
        String jsonStr = getContentAsString("ContractABI_stackitem.json");
        StackItem rawItem = OBJECT_MAPPER.readValue(jsonStr, StackItem.class);

        //ContractABI ca = StackItemSerializableInterface.from(rawItem, ContractABI.class);
        //assertThat(ca.getPubKey(), is("test"));
        //assertThat(cg.getSignature(), is("test"));
    }

    @Ignore
    @Test
    public void testContractParameter() throws IOException, DeserializationException {
        String jsonStr = getContentAsString("ContractParameter_stackitem.json");
        StackItem rawItem = OBJECT_MAPPER.readValue(jsonStr, StackItem.class);

        //ContractParameter cp = StackItemSerializableInterface.from(rawItem, ContractParameter.class);

    }

    protected static String getContentAsString(String fileName) {
        InputStream stream = StackItemSerializableTest.class.getResourceAsStream("/stackitem/" + fileName);
        BufferedReader in = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8));
        return in.lines().collect(Collectors.joining());
    }

}
