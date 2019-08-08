package io.neow3j.contract;

import static java.util.Optional.of;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import io.neow3j.contract.abi.model.NeoContractEvent;
import io.neow3j.contract.abi.model.NeoContractFunction;
import io.neow3j.model.types.ContractParameterType;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

public class ContractAbiLoaderTest {

    private static final String TEST1_SMARTCONTRACT_ABI_FILENAME = "/test1-smartcontract.abi.json";
    private static final String TEST2_SMARTCONTRACT_ABI_FILENAME = "/test2-smartcontract.abi.json";

    @Test
    public void abiWithoutAddress_test1() {
        Contract c1 = new ContractAbiLoader.Builder()
            .loadABIFile(getTestAbsoluteFileName(TEST1_SMARTCONTRACT_ABI_FILENAME))
            .build()
            .load();

        assertThat(c1.getContractScriptHash(),
            is(new ScriptHash("5944fc67643207920ec129d13181297fed10350c")));
        assertThat(c1.getFunctions(), hasSize(3));
        assertThat(
            c1.getFunction("Name"),
            is(of(new NeoContractFunction("Name", Collections.emptyList(),
                ContractParameterType.STRING)))
        );
        assertThat(c1.getEvents(), hasSize(0));
        assertThat(c1.getDeploymentScript(), nullValue());
        assertThat(c1.getAbi(), notNullValue());
        assertThat(
            c1.getEntryPoint().get(),
            is(
                new NeoContractFunction("Main",
                    Arrays.asList(
                        new ContractParameter("operation", ContractParameterType.STRING),
                        new ContractParameter("args", ContractParameterType.ARRAY)
                    ),
                    ContractParameterType.BYTE_ARRAY)
            )
        );
        assertThat(c1.getEntryPointParameters(), hasSize(2));
        assertThat(c1.getEntryPointReturnType().get(), is(ContractParameterType.BYTE_ARRAY));
    }

    @Test
    public void abiWithoutAddress_test2() {
        Contract c1 = new ContractAbiLoader.Builder()
            .loadABIFile(getTestAbsoluteFileName(TEST2_SMARTCONTRACT_ABI_FILENAME))
            .build()
            .load();

        assertThat(c1.getContractScriptHash(),
            is(new ScriptHash("5944fc67643207920ec129d13181297fed10350c")));
        assertThat(c1.getFunctions(), hasSize(3));
        assertThat(
            c1.getFunction("Name"),
            is(
                of(
                    new NeoContractFunction("Name", Arrays.asList(
                        new ContractParameter("nameParam1", ContractParameterType.INTEGER)
                    ), ContractParameterType.ARRAY)
                )
            )
        );
        assertThat(
            c1.getFunction("Description"),
            is(
                of(
                    new NeoContractFunction("Description", Arrays.asList(
                        new ContractParameter("descriptionParam1", ContractParameterType.STRING)
                    ), ContractParameterType.STRING)
                )
            )
        );
        assertThat(c1.getEvents(), hasSize(2));
        assertThat(
            c1.getEvent("event1"),
            is(
                of(
                    new NeoContractEvent("event1",
                        Arrays.asList(
                            new ContractParameter("event1Param1", ContractParameterType.STRING),
                            new ContractParameter("event1Param2", ContractParameterType.ARRAY)
                        )
                    )
                )
            )
        );
        assertThat(
            c1.getEvent("event2"),
            is(
                of(
                    new NeoContractEvent("event2",
                        Arrays.asList(
                            new ContractParameter("event2Param1", ContractParameterType.INTEGER),
                            new ContractParameter("event2Param2", ContractParameterType.ARRAY)
                        )
                    )
                )
            )
        );
        assertThat(c1.getDeploymentScript(), nullValue());
        assertThat(c1.getAbi(), notNullValue());
        assertThat(
            c1.getEntryPoint().get(),
            is(
                new NeoContractFunction("Main",
                    Arrays.asList(
                        new ContractParameter("operation", ContractParameterType.STRING),
                        new ContractParameter("args", ContractParameterType.ARRAY)
                    ),
                    ContractParameterType.BYTE_ARRAY)
            )
        );
        assertThat(c1.getEntryPointParameters(), hasSize(2));
        assertThat(c1.getEntryPointReturnType().get(), is(ContractParameterType.BYTE_ARRAY));
    }

    @Test(expected = IllegalStateException.class)
    public void abiWithoutEitherABIFileOrContractScriptHash() {
        new ContractAbiLoader.Builder()
            .build()
            .load();
    }

    @Test(expected = IllegalStateException.class)
    public void abiWithContractScriptHashMismatch() {
        new ContractAbiLoader.Builder()
            .contractScriptHash(new ScriptHash("0x746d6cc63dacd7b275bb3a3a06d54859661591a6"))
            .loadABIFile(getTestAbsoluteFileName(TEST1_SMARTCONTRACT_ABI_FILENAME))
            .build()
            .load();
    }

    @Test(expected = IllegalArgumentException.class)
    public void abiWithInvalidAddress() {
        new ContractAbiLoader.Builder()
            .contractScriptHash(new ScriptHash("anything"))
            .loadABIFile(getTestAbsoluteFileName(TEST1_SMARTCONTRACT_ABI_FILENAME))
            .build()
            .load();
    }

    private String getTestAbsoluteFileName(String fileName) {
        return this.getClass().getResource(fileName).getFile();
    }

}
