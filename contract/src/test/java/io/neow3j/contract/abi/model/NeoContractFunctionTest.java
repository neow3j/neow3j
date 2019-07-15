package io.neow3j.contract.abi.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.neow3j.model.ContractParameter;
import io.neow3j.model.types.ContractParameterType;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyCollectionOf;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class NeoContractFunctionTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testSerialize() throws JsonProcessingException {
        NeoContractFunction neoContractFunction = new NeoContractFunction(
                "anything",
                Arrays.asList(
                        new ContractParameter(ContractParameterType.BYTE_ARRAY, "001010101010")
                ),
                ContractParameterType.BYTE_ARRAY
        );
        String neoContractFunctionString = objectMapper.writeValueAsString(neoContractFunction);

        assertThat(neoContractFunctionString,
                is(
                        "{" +
                                "\"name\":\"anything\"," +
                                "\"parameters\":[" +
                                "{" +
                                "\"type\":\"ByteArray\"," +
                                "\"value\":\"001010101010\"" +
                                "}" +
                                "]," +
                                "\"returntype\":\"ByteArray\"" +
                                "}"
                )
        );
    }

    @Test
    public void testSerialize_Empty() throws JsonProcessingException {
        NeoContractFunction neoContractFunction = new NeoContractFunction(
                "anything",
                Arrays.asList(),
                ContractParameterType.BYTE_ARRAY
        );
        String neoContractFunctionString = objectMapper.writeValueAsString(neoContractFunction);

        assertThat(neoContractFunctionString,
                is(
                        "{" +
                                "\"name\":\"anything\"," +
                                "\"parameters\":[" +
                                "]," +
                                "\"returntype\":\"ByteArray\"" +
                                "}"
                )
        );
    }

    @Test
    public void testSerialize_Null() throws JsonProcessingException {
        NeoContractFunction neoContractFunction = new NeoContractFunction(
                "anything",
                null,
                ContractParameterType.BYTE_ARRAY
        );
        String neoContractFunctionString = objectMapper.writeValueAsString(neoContractFunction);

        assertThat(neoContractFunctionString,
                is(
                        "{" +
                                "\"name\":\"anything\"," +
                                "\"parameters\":[" +
                                "]," +
                                "\"returntype\":\"ByteArray\"" +
                                "}"
                )
        );
    }

    @Test
    public void testDeserialize() throws IOException {

        String neoContractFunctionString = "{" +
                "\"name\":\"anything\"," +
                "\"parameters\":[" +
                "{" +
                "\"type\":\"ByteArray\"," +
                "\"value\":\"001010101010\"" +
                "}" +
                "]," +
                "\"returntype\":\"ByteArray\"" +
                "}";

        NeoContractFunction neoContractFunction = objectMapper.readValue(neoContractFunctionString, NeoContractFunction.class);

        assertThat(neoContractFunction.getName(), is("anything"));
        assertThat(neoContractFunction.getParameters(), not(emptyCollectionOf(ContractParameter.class)));
        assertThat(neoContractFunction.getParameters(),
                hasItems(
                        new ContractParameter(ContractParameterType.BYTE_ARRAY, "001010101010")
                )
        );
        assertThat(neoContractFunction.getReturnType(), is(ContractParameterType.BYTE_ARRAY));
    }

    @Test
    public void testDeserialize_Empty() throws IOException {

        String neoContractFunctionString = "{" +
                "\"name\":\"anything\"," +
                "\"parameters\":[" +
                "]," +
                "\"returntype\":\"ByteArray\"" +
                "}";

        NeoContractFunction neoContractFunction = objectMapper.readValue(neoContractFunctionString, NeoContractFunction.class);

        assertThat(neoContractFunction.getName(), is("anything"));
        assertThat(neoContractFunction.getParameters(), emptyCollectionOf(ContractParameter.class));
        assertThat(neoContractFunction.getParameters(), hasSize(0));
        assertThat(neoContractFunction.getReturnType(), is(ContractParameterType.BYTE_ARRAY));
    }

    @Test
    public void testDeserialize_Null() throws IOException {

        String neoContractFunctionString = "{" +
                "\"name\":\"anything\"," +
                "\"parameters\":null," +
                "\"returntype\":\"ByteArray\"" +
                "}";

        NeoContractFunction neoContractFunction = objectMapper.readValue(neoContractFunctionString, NeoContractFunction.class);

        assertThat(neoContractFunction.getName(), is("anything"));
        assertThat(neoContractFunction.getParameters(), emptyCollectionOf(ContractParameter.class));
        assertThat(neoContractFunction.getParameters(), hasSize(0));
        assertThat(neoContractFunction.getReturnType(), is(ContractParameterType.BYTE_ARRAY));
    }

}
