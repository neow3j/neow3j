package io.neow3j.contract.abi.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyCollectionOf;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.neow3j.contract.ContractParameter;
import io.neow3j.model.types.ContractParameterType;
import java.io.IOException;
import java.util.Arrays;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

public class NeoContractInterfaceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testSerialize() throws JsonProcessingException {
        NeoContractInterface neoContractInterface = new NeoContractInterface(
                "anything",
                "Main",
                Arrays.asList(
                        new NeoContractFunction("anything", Arrays.asList(ContractParameter.byteArray("001010101010")), ContractParameterType.BYTE_ARRAY)
                ),
                Arrays.asList(
                        new NeoContractEvent("anything", Arrays.asList(ContractParameter.byteArray("001010101010")))
                )
        );
        String neoContractInterfaceString = objectMapper.writeValueAsString(neoContractInterface);

        assertThat(neoContractInterfaceString,
                is(
                        "{" +
                                "\"hash\":\"anything\"," +
                                "\"entrypoint\":\"Main\"," +
                                "\"functions\":[" +
                                "{" +
                                "\"name\":\"anything\"," +
                                "\"parameters\":[" +
                                "{" +
                                "\"type\":\"ByteArray\"," +
                                "\"value\":\"001010101010\"" +
                                "}" +
                                "]," +
                                "\"returntype\":\"ByteArray\"" +
                                "}" +
                                "]," +
                                "\"events\":[" +
                                "{" +
                                "\"name\":\"anything\"," +
                                "\"parameters\":[" +
                                "{" +
                                "\"type\":\"ByteArray\"," +
                                "\"value\":\"001010101010\"" +
                                "}" +
                                "]" +
                                "}" +
                                "]" +
                                "}"
                )
        );
    }

    @Test
    public void testSerialize_Empty() throws JsonProcessingException {
        NeoContractInterface neoContractInterface = new NeoContractInterface(
                "anything",
                "Main",
                Arrays.asList(),
                Arrays.asList()
        );
        String neoContractInterfaceString = objectMapper.writeValueAsString(neoContractInterface);

        assertThat(neoContractInterfaceString,
                is(
                        "{" +
                                "\"hash\":\"anything\"," +
                                "\"entrypoint\":\"Main\"," +
                                "\"functions\":[" +
                                "]," +
                                "\"events\":[" +
                                "]" +
                                "}"
                )
        );
    }

    @Test
    public void testSerialize_Null() throws JsonProcessingException {
        NeoContractInterface neoContractInterface = new NeoContractInterface(
                "anything",
                "Main",
                null,
                null
        );
        String neoContractInterfaceString = objectMapper.writeValueAsString(neoContractInterface);

        assertThat(neoContractInterfaceString,
                is(
                        "{" +
                                "\"hash\":\"anything\"," +
                                "\"entrypoint\":\"Main\"," +
                                "\"functions\":[" +
                                "]," +
                                "\"events\":[" +
                                "]" +
                                "}"
                )
        );
    }

    @Test
    public void testDeserialize() throws IOException {

        String neoContractInterfaceString = "{" +
                "\"hash\":\"anything\"," +
                "\"entrypoint\":\"Main\"," +
                "\"functions\":[" +
                "{" +
                "\"name\":\"anything\"," +
                "\"parameters\":[" +
                "{" +
                "\"type\":\"ByteArray\"," +
                "\"value\":\"001010101010\"" +
                "}" +
                "]," +
                "\"returntype\":\"ByteArray\"" +
                "}" +
                "]," +
                "\"events\":[" +
                "{" +
                "\"name\":\"anything\"," +
                "\"parameters\":[" +
                "{" +
                "\"type\":\"ByteArray\"," +
                "\"value\":\"001010101010\"" +
                "}" +
                "]" +
                "}" +
                "]" +
                "}";

        NeoContractInterface neoContractInterface = objectMapper.readValue(neoContractInterfaceString, NeoContractInterface.class);

        assertThat(neoContractInterface.getHash(), is("anything"));
        assertThat(neoContractInterface.getEntryPoint(), is("Main"));
        assertThat(neoContractInterface.getFunctions(), not(emptyCollectionOf(NeoContractFunction.class)));
        assertThat(neoContractInterface.getFunctions(),
                CoreMatchers.hasItems(
                        new NeoContractFunction("anything", Arrays.asList(ContractParameter.byteArray("001010101010")), ContractParameterType.BYTE_ARRAY)
                )
        );
        assertThat(neoContractInterface.getEvents(), not(emptyCollectionOf(NeoContractEvent.class)));
        assertThat(neoContractInterface.getEvents(),
                CoreMatchers.hasItems(
                        new NeoContractEvent("anything", Arrays.asList(ContractParameter.byteArray("001010101010")))
                )
        );
    }

    @Test
    public void testDeserialize_Empty() throws IOException {

        String neoContractInterfaceString = "{" +
                "\"hash\":\"anything\"," +
                "\"entrypoint\":\"Main\"," +
                "\"functions\":[" +
                "]," +
                "\"events\":[" +
                "]" +
                "}";

        NeoContractInterface neoContractInterface = objectMapper.readValue(neoContractInterfaceString, NeoContractInterface.class);

        assertThat(neoContractInterface.getHash(), is("anything"));
        assertThat(neoContractInterface.getEntryPoint(), is("Main"));
        assertThat(neoContractInterface.getFunctions(), emptyCollectionOf(NeoContractFunction.class));
        assertThat(neoContractInterface.getFunctions(), hasSize(0));
        assertThat(neoContractInterface.getEvents(), emptyCollectionOf(NeoContractEvent.class));
        assertThat(neoContractInterface.getEvents(), hasSize(0));
    }

    @Test
    public void testDeserialize_Null() throws IOException {

        String neoContractInterfaceString = "{" +
                "\"hash\":\"anything\"," +
                "\"entrypoint\":\"Main\"," +
                "\"functions\":null," +
                "\"events\":null" +
                "}";

        NeoContractInterface neoContractInterface = objectMapper.readValue(neoContractInterfaceString, NeoContractInterface.class);

        assertThat(neoContractInterface.getHash(), is("anything"));
        assertThat(neoContractInterface.getEntryPoint(), is("Main"));
        assertThat(neoContractInterface.getFunctions(), emptyCollectionOf(NeoContractFunction.class));
        assertThat(neoContractInterface.getFunctions(), hasSize(0));
        assertThat(neoContractInterface.getEvents(), emptyCollectionOf(NeoContractEvent.class));
        assertThat(neoContractInterface.getEvents(), hasSize(0));
    }

}
