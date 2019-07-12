package io.neow3j.contract;

import io.neow3j.model.types.ContractParameter;
import io.neow3j.model.types.ContractParameterType;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.methods.response.NeoInvoke;
import io.neow3j.protocol.core.methods.response.NeoSendRawTransaction;
import io.neow3j.protocol.exceptions.ErrorResponseException;
import io.neow3j.protocol.http.HttpService;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

public class ContractTest {

    private Neow3j neow3j;

    @Before
    public void setup() {
        this.neow3j = Neow3j.build(new HttpService("http://localhost:30333"));
    }

    @Ignore
    @Test
    public void abi() {
        Contract c1 = new ContractAbiLoader.Builder()
                .address("anything")
                .loadABIFile("file.abi")
                .build()
                .load();

        // TODO: 2019-07-03 Guil: to be implemented
    }

    @Test
    public void deploy() {
        Contract c1 = new ContractDeployment.Builder(this.neow3j)
                .loadAVMFile("file.avm")
                .needsStorage()
                .needsDynamicInvoke()
                .isPayable()
                .parameters(
                        Arrays.asList(
                                ContractParameterType.STRING,
                                ContractParameterType.ARRAY
                        )
                )
                .returnType(ContractParameterType.ARRAY)
                .name("ContractName")
                .version("1.0.0")
                .author("Author")
                .email("email@email.com")
                .description("ContractDescription")
                .build()
                .deploy();

        Contract c2 = new ContractDeployment.Builder(this.neow3j)
                .loadAVMFile("file.avm")
                .needsStorage()
                .needsDynamicInvoke()
                .isPayable()
                .parameter(ContractParameterType.STRING)
                .parameter(ContractParameterType.ARRAY)
                .returnType(ContractParameterType.BYTE_ARRAY)
                .name("ContractName")
                .version("1.0.0")
                .author("Author")
                .email("email@email.com")
                .description("ContractDescription")
                .build()
                .deploy();

        // TODO: 2019-07-03 Guil: to be implemented
    }

    @Test
    public void invoke() throws IOException, ErrorResponseException {

        Contract contract = new ContractDeployment.Builder(this.neow3j)
                .loadAVMFile("file.avm")
                .needsStorage()
                .needsDynamicInvoke()
                .isPayable()
                .parameter(ContractParameterType.STRING)
                .parameter(ContractParameterType.ARRAY)
                .returnType(ContractParameterType.BYTE_ARRAY)
                .name("ContractName")
                .version("1.0.0")
                .author("Author")
                .email("email@email.com")
                .description("ContractDescription")
                .build()
                .deploy();

//        NeoSendRawTransaction response = new ContractInvocation.Builder(this.neow3j)
//                .contract(contract)
//                .parameter(ContractParameterType.STRING, "valueString1")
//                .parameter(
//                        ContractParameterType.ARRAY,
//                        Arrays.asList(
//                                new ContractParameter(ContractParameterType.STRING, "valueString2"),
//                                new ContractParameter(ContractParameterType.INTEGER, 123)
//                        )
//                )
//                .build()
//                // TODO: 2019-07-03 Guil:
//                // Maybe the "invoke" can even check if the Contract class has the "correct" parameters
//                // that the user wants to call -- like a validation check.
//                .invoke();

        // TODO: 2019-07-03 Guil: to be implemented

    }

}
