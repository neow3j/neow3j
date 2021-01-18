package io.neow3j.compiler;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import io.neow3j.contract.ContractParameter;
import io.neow3j.contract.NeoToken;
import io.neow3j.devpack.contracts.ManagementContract;
import io.neow3j.devpack.neo.Contract;
import io.neow3j.protocol.core.methods.response.ArrayStackItem;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.utils.Numeric;
import java.io.IOException;
import org.junit.BeforeClass;
import org.junit.Test;

public class ManagementContractIntegrationTest extends ContractTest {

    @BeforeClass
    public static void setUp() throws Throwable {
        setUp(ManagementContractIntegrationTestContract.class.getName());
    }

    @Test
    public void getContract() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(
                ContractParameter.hash160(io.neow3j.contract.NeoToken.SCRIPT_HASH));
        ArrayStackItem arrayStackItem = response.getInvocationResult().getStack().get(0).asArray();
        assertThat(arrayStackItem.get(0).asInteger().getValue().intValue(), is(-1)); // ID
        assertThat(arrayStackItem.get(1).asInteger().getValue().intValue(), is(0)); // updateCounter
        assertThat(Numeric.reverseHexString(arrayStackItem.get(2).asByteString().getAsHexString()),
                is(NeoToken.SCRIPT_HASH.toString())); // contract hash
        // script
        assertThat(arrayStackItem.get(3).asByteString().getAsHexString(), not(isEmptyString()));
        // manifest
        assertThat(arrayStackItem.get(4).asByteString().getAsHexString(), not(isEmptyString()));
    }

    @Test
    public void getHash() throws Throwable {
        NeoInvokeFunction response = callInvokeFunction();
        assertThat(response.getInvocationResult().getStack().get(0).asByteString().getAsHexString(),
                is(io.neow3j.contract.ManagementContract.SCRIPT_HASH.toString()));
    }

    static class ManagementContractIntegrationTestContract {

        public static Contract getContract(byte[] contractHash) {
            return ManagementContract.getContract(contractHash);
        }

        public static byte[] getHash() {
            return ManagementContract.getHash();
        }

    }
}
