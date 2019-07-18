package io.neow3j.contract.abi.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.neow3j.contract.ContractParameter;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyCollectionOf;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class NeoContractEventTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testSerialize() throws JsonProcessingException {
        NeoContractEvent neoContractEvent = new NeoContractEvent(
                "anything",
                Arrays.asList(
                        ContractParameter.byteArray("001010101010"),
                        ContractParameter.bool(true),
                        ContractParameter.integer(123)
                )
        );
        String neoContractEventString = objectMapper.writeValueAsString(neoContractEvent);

        assertThat(neoContractEventString,
                is(
                        "{" +
                                "\"name\":\"anything\"," +
                                "\"parameters\":[" +
                                "{" +
                                "\"type\":\"ByteArray\"," +
                                "\"value\":\"001010101010\"" +
                                "}," +
                                "{" +
                                "\"type\":\"Boolean\"," +
                                "\"value\":true" +
                                "}," +
                                "{" +
                                "\"type\":\"Integer\"," +
                                "\"value\":\"123\"" +
                                "}" +
                                "]" +
                                "}"
                )
        );
    }

    @Test
    public void testSerialize_Empty() throws JsonProcessingException {
        NeoContractEvent neoContractEvent = new NeoContractEvent(
                "anything",
                Arrays.asList()
        );
        String neoContractEventString = objectMapper.writeValueAsString(neoContractEvent);

        assertThat(neoContractEventString,
                is(
                        "{" +
                                "\"name\":\"anything\"," +
                                "\"parameters\":[" +
                                "]" +
                                "}"
                )
        );
    }

    @Test
    public void testSerialize_Null() throws JsonProcessingException {
        NeoContractEvent neoContractEvent = new NeoContractEvent(
                "anything",
                null
        );
        String neoContractEventString = objectMapper.writeValueAsString(neoContractEvent);

        assertThat(neoContractEventString,
                is(
                        "{" +
                                "\"name\":\"anything\"," +
                                "\"parameters\":[" +
                                "]" +
                                "}"
                )
        );
    }

    @Test
    public void testDeserialize() throws IOException {

        String neoContractEventString = "{" +
                "\"name\":\"anything\"," +
                "\"parameters\":[" +
                "{" +
                "\"type\":\"ByteArray\"," +
                "\"value\":\"001010101010\"" +
                "}," +
                "{" +
                "\"type\":\"Boolean\"," +
                "\"value\":true" +
                "}," +
                "{" +
                "\"type\":\"Integer\"," +
                "\"value\":\"123\"" +
                "}" +
                "]" +
                "}";

        NeoContractEvent neoContractEvent = objectMapper.readValue(neoContractEventString, NeoContractEvent.class);

        assertThat(neoContractEvent.getName(), is("anything"));
        assertThat(neoContractEvent.getParameters(), not(emptyCollectionOf(ContractParameter.class)));
        assertThat(neoContractEvent.getParameters(),
                hasItems(
                        ContractParameter.integer(123),
                        ContractParameter.byteArray("001010101010"),
                        ContractParameter.bool(true),
                        ContractParameter.integer(123)
                )
        );
    }

    @Test
    public void testDeserialize_Empty() throws IOException {

        String neoContractEventString = "{" +
                "\"name\":\"anything\"," +
                "\"parameters\":[" +
                "]" +
                "}";

        NeoContractEvent neoContractEvent = objectMapper.readValue(neoContractEventString, NeoContractEvent.class);

        assertThat(neoContractEvent.getName(), is("anything"));
        assertThat(neoContractEvent.getParameters(), emptyCollectionOf(ContractParameter.class));
        assertThat(neoContractEvent.getParameters(), hasSize(0));
    }

    @Test
    public void testDeserialize_Null() throws IOException {

        String neoContractEventString = "{" +
                "\"name\":\"anything\"," +
                "\"parameters\":null" +
                "}";

        NeoContractEvent neoContractEvent = objectMapper.readValue(neoContractEventString, NeoContractEvent.class);

        assertThat(neoContractEvent.getName(), is("anything"));
        assertThat(neoContractEvent.getParameters(), emptyCollectionOf(ContractParameter.class));
        assertThat(neoContractEvent.getParameters(), hasSize(0));
    }

}
