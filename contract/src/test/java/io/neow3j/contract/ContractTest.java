package io.neow3j.contract;

import io.neow3j.contract.abi.model.NeoContractEvent;
import io.neow3j.contract.abi.model.NeoContractFunction;
import io.neow3j.contract.abi.model.NeoContractInterface;
import io.neow3j.model.types.ContractParameterType;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static java.util.Optional.empty;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class ContractTest {

    @Test
    public void testNewContract_TwoParam() {
        ContractDeploymentScript ds = mock(ContractDeploymentScript.class);
        when(ds.getContractScriptHash())
                .thenReturn(new ScriptHash("746d6cc63dacd7b275bb3a3a06d54859661591a6"));
        NeoContractInterface abi = mock(NeoContractInterface.class);

        Contract contract = new Contract(ds, abi);

        assertThat(contract.getDeploymentScript(), is(ds));
        assertThat(contract.getAbi(), is(abi));
        assertThat(
                contract.getContractScriptHash(),
                is(new ScriptHash("746d6cc63dacd7b275bb3a3a06d54859661591a6"))
        );
    }

    @Test
    public void testNewContract_ScriptHash() {
        ScriptHash sh = mock(ScriptHash.class);

        Contract contract = new Contract(sh);

        assertThat(contract.getDeploymentScript(), nullValue());
        assertThat(contract.getAbi(), nullValue());
        assertThat(
                contract.getContractScriptHash(),
                is(sh)
        );
    }

    @Test
    public void testNewContract_ScriptHash_and_ABI() {
        ScriptHash sh = mock(ScriptHash.class);
        NeoContractInterface abi = mock(NeoContractInterface.class);

        Contract contract = new Contract(sh, abi);

        assertThat(contract.getDeploymentScript(), nullValue());
        assertThat(contract.getAbi(), is(abi));
        assertThat(
                contract.getContractScriptHash(),
                is(sh)
        );
    }

    @Test
    public void testGetEntryPoint() {
        NeoContractInterface mockABI = spy(NeoContractInterface.class);

        when(mockABI.getEntryPoint()).thenReturn("Main");
        when(mockABI.getFunctions()).thenReturn(
                Arrays.asList(
                        new NeoContractFunction("Main", null, null),
                        new NeoContractFunction("AnythingElse", null, null)
                )
        );

        Contract contract = new Contract(new ContractDeploymentScript(), mockABI);

        assertThat(contract.getEntryPoint().isPresent(), is(true));

        assertThat(contract.getEntryPoint().get(),
                is(new NeoContractFunction("Main",
                        Collections.emptyList(), null)));
        assertThat(contract.getFunction("Main").get(),
                is(new NeoContractFunction("Main",
                        Collections.emptyList(), null)));

        when(mockABI.getFunctions()).thenReturn(
                Arrays.asList(
                        new NeoContractFunction("AnythingElse", null, null)
                )
        );

        assertThat(contract.getEntryPoint().isPresent(), is(false));

    }

    @Test
    public void testGetEntryPointParameters() {
        NeoContractInterface mockABI = spy(NeoContractInterface.class);

        when(mockABI.getEntryPoint()).thenReturn("Main");
        when(mockABI.getFunctions()).thenReturn(
                Arrays.asList(
                        new NeoContractFunction(
                                "Main",
                                Arrays.asList(
                                        new ContractParameter("param1", ContractParameterType.STRING),
                                        new ContractParameter("param2", ContractParameterType.ARRAY)
                                ),
                                null
                        )
                )
        );

        Contract contract = new Contract(new ContractDeploymentScript(), mockABI);

        assertThat(
                contract.getEntryPointParameters(),
                hasItems(
                        new ContractParameter("param1", ContractParameterType.STRING),
                        new ContractParameter("param2", ContractParameterType.ARRAY)
                )
        );

        when(mockABI.getFunctions()).thenReturn(
                Arrays.asList(
                        new NeoContractFunction(
                                "Main",
                                Arrays.asList(),
                                null
                        )
                )
        );

        assertThat(
                contract.getEntryPointParameters(),
                hasSize(0)
        );

    }

    @Test
    public void testGetEntryPointReturnType() {
        NeoContractInterface mockABI = spy(NeoContractInterface.class);

        when(mockABI.getEntryPoint()).thenReturn("Main");
        when(mockABI.getFunctions()).thenReturn(
                Arrays.asList(
                        new NeoContractFunction(
                                "Main",
                                null,
                                ContractParameterType.BYTE_ARRAY
                        )
                )
        );

        Contract contract = new Contract(new ContractDeploymentScript(), mockABI);

        assertThat(
                contract.getEntryPointReturnType().get(),
                is(ContractParameterType.BYTE_ARRAY)
        );

        when(mockABI.getFunctions()).thenReturn(
                Arrays.asList(
                        new NeoContractFunction(
                                "Main",
                                null,
                                null
                        )
                )
        );

        assertThat(
                contract.getEntryPointReturnType(),
                is(empty())
        );

    }

    @Test
    public void testGetFunctions() {
        NeoContractInterface mockABI = spy(NeoContractInterface.class);

        NeoContractFunction f1 = new NeoContractFunction(
                "Main",
                Arrays.asList(
                        new ContractParameter("param1", ContractParameterType.INTEGER),
                        new ContractParameter("param2", ContractParameterType.STRING)
                ),
                ContractParameterType.BYTE_ARRAY
        );

        NeoContractFunction f2 = new NeoContractFunction(
                "Function1",
                Arrays.asList(
                        new ContractParameter("param1", ContractParameterType.INTEGER),
                        new ContractParameter("param2", ContractParameterType.STRING)
                ),
                ContractParameterType.BYTE_ARRAY
        );

        when(mockABI.getFunctions()).thenReturn(
                Arrays.asList(f1, f2)
        );

        Contract contract = new Contract(new ContractDeploymentScript(), mockABI);

        assertThat(
                contract.getFunctions(),
                hasItems(f1, f2)
        );

        assertThat(
                contract.getFunctionReturnType("Main").get(),
                is(ContractParameterType.BYTE_ARRAY)
        );

        assertThat(
                contract.getFunctionReturnType("Function1").get(),
                is(ContractParameterType.BYTE_ARRAY)
        );

        assertThat(
                contract.getFunction("Main").get(),
                is(f1)
        );

        assertThat(
                contract.getFunction("Function1").get(),
                is(f2)
        );

        when(mockABI.getFunctions()).thenReturn(Arrays.asList());

        assertThat(
                contract.getFunctions(),
                hasSize(0)
        );
    }

    @Test
    public void testGetEvents() {
        NeoContractInterface mockABI = spy(NeoContractInterface.class);

        NeoContractEvent e1 = new NeoContractEvent(
                "Event1",
                Arrays.asList(
                        new ContractParameter("param1", ContractParameterType.INTEGER),
                        new ContractParameter("param2", ContractParameterType.STRING)
                )
        );

        NeoContractEvent e2 = new NeoContractEvent(
                "Event2",
                Arrays.asList(
                        new ContractParameter("param1", ContractParameterType.INTEGER),
                        new ContractParameter("param2", ContractParameterType.STRING)
                )
        );

        when(mockABI.getEvents()).thenReturn(Arrays.asList(e1, e2));

        Contract contract = new Contract(new ContractDeploymentScript(), mockABI);

        assertThat(
                contract.getEvents(),
                hasItems(e1, e2)
        );

        assertThat(
                contract.getEventParameters("Event1"),
                hasItems(
                        new ContractParameter("param1", ContractParameterType.INTEGER),
                        new ContractParameter("param2", ContractParameterType.STRING)
                )
        );

        assertThat(
                contract.getEventParameters("Event2"),
                hasItems(
                        new ContractParameter("param1", ContractParameterType.INTEGER),
                        new ContractParameter("param2", ContractParameterType.STRING)
                )
        );

        assertThat(
                contract.getEvent("Event1").get(),
                is(e1)
        );

        assertThat(
                contract.getEvent("Event2").get(),
                is(e2)
        );

        when(mockABI.getEvents()).thenReturn(Arrays.asList());

        when(mockABI.getEvents()).thenReturn(Arrays.asList());

        assertThat(
                contract.getEvents(),
                hasSize(0)
        );

    }

    @Test(expected = IllegalStateException.class)
    public void testGetEventParameters_NoAbiSet() {
        ScriptHash mockHash = mock(ScriptHash.class);
        Contract contract = new Contract(mockHash);
        contract.getEventParameters("anyEvent");
    }

}
