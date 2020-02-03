package io.neow3j.contract.abi.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.neow3j.contract.ContractParameter;
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
import static org.junit.Assert.assertEquals;

public class NeoContractMethodTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testSerialize() throws JsonProcessingException {
        NeoContractMethod neoContractMethod = new NeoContractMethod(
                "anything",
                Arrays.asList(
                        ContractParameter.byteArray("001010101010")
                ),
                ContractParameterType.BYTE_ARRAY
        );
        String neoContractMethodString = objectMapper.writeValueAsString(neoContractMethod);

        assertThat(neoContractMethodString,
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
        NeoContractMethod neoContractMethod = new NeoContractMethod(
                "anything",
                Arrays.asList(),
                ContractParameterType.BYTE_ARRAY
        );
        String neoContractMethodString = objectMapper.writeValueAsString(neoContractMethod);

        assertThat(neoContractMethodString,
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
        NeoContractMethod neoContractMethod = new NeoContractMethod(
                "anything",
                null,
                ContractParameterType.BYTE_ARRAY
        );
        String neoContractMethodString = objectMapper.writeValueAsString(neoContractMethod);

        assertThat(neoContractMethodString,
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

        String neoContractMethodString = "{" +
                "\"name\":\"anything\"," +
                "\"parameters\":[" +
                "{" +
                "\"type\":\"ByteArray\"," +
                "\"value\":\"001010101010\"" +
                "}" +
                "]," +
                "\"returntype\":\"ByteArray\"" +
                "}";

        NeoContractMethod neoContractMethod = objectMapper.readValue(neoContractMethodString, NeoContractMethod.class);

        assertThat(neoContractMethod.getName(), is("anything"));
        assertThat(neoContractMethod.getParameters(), not(emptyCollectionOf(ContractParameter.class)));
        assertThat(neoContractMethod.getParameters(),
                hasItems(
                        ContractParameter.byteArray("001010101010")
                )
        );
        assertThat(neoContractMethod.getReturnType(), is(ContractParameterType.BYTE_ARRAY));
    }

    @Test
    public void testDeserialize_Empty() throws IOException {

        String neoContractMethodString = "{" +
                "\"name\":\"anything\"," +
                "\"parameters\":[" +
                "]," +
                "\"returntype\":\"ByteArray\"" +
                "}";

        NeoContractMethod neoContractMethod = objectMapper.readValue(neoContractMethodString, NeoContractMethod.class);

        assertThat(neoContractMethod.getName(), is("anything"));
        assertThat(neoContractMethod.getParameters(), emptyCollectionOf(ContractParameter.class));
        assertThat(neoContractMethod.getParameters(), hasSize(0));
        assertThat(neoContractMethod.getReturnType(), is(ContractParameterType.BYTE_ARRAY));
    }

    @Test
    public void testDeserialize_Null() throws IOException {

        String neoContractMethodString = "{" +
                "\"name\":\"anything\"," +
                "\"parameters\":null," +
                "\"returntype\":\"ByteArray\"" +
                "}";

        NeoContractMethod neoContractMethod = objectMapper.readValue(neoContractMethodString, NeoContractMethod.class);

        assertThat(neoContractMethod.getName(), is("anything"));
        assertThat(neoContractMethod.getParameters(), emptyCollectionOf(ContractParameter.class));
        assertThat(neoContractMethod.getParameters(), hasSize(0));
        assertThat(neoContractMethod.getReturnType(), is(ContractParameterType.BYTE_ARRAY));
    }

}
