package io.neow3j.compiler;

import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.Runtime;
import io.neow3j.devpack.Storage;
import io.neow3j.devpack.StorageMap;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import io.neow3j.types.ContractParameter;
import io.neow3j.types.Hash256;
import org.hamcrest.core.StringEndsWith;
import org.hamcrest.core.StringStartsWith;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.IOException;

import static org.junit.Assert.assertThat;

public class StaticVariablesIntegrationTest {

    @Rule
    public TestName testName = new TestName();

    @ClassRule
    public static ContractTestRule ct = new ContractTestRule(
            StaticVariablesIntegrationTestContract.class.getName());

    @Test
    public void putToStaticStorageMap() throws Throwable {
        Hash256 txHash = ct.invokeFunctionAndAwaitExecution(testName,
                ContractParameter.string("key"),
                ContractParameter.string("value"));

        ct.assertVMExitedWithHalt(txHash);
        ct.assertStorageContains("data" + "key", "value");
    }

    @Test
    public void stringConcatWithSyscall() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        assertThat(response.getInvocationResult().getStack().get(0).getString(),
                StringStartsWith.startsWith("The platform: "));
        assertThat(response.getInvocationResult().getStack().get(0).getString(),
                StringEndsWith.endsWith("NEO"));
    }

    @Test
    public void callStaticFinalVariableFromNonContractClass() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        assertThat(response.getInvocationResult().getStack().get(0).getString(),
                StringStartsWith.startsWith("Hello, world!"));
    }

    static class StaticVariablesIntegrationTestContract {

        private static final StorageMap map =
                new StorageMap(Storage.getStorageContext(), new ByteString("data"));
        private static final String platform = "The platform: " + Runtime.getPlatform();

        public static void putToStaticStorageMap(ByteString key, ByteString value) {
            map.put(key, value);
        }

        public static String stringConcatWithSyscall() {
            return platform;
        }

        public static String callStaticFinalVariableFromNonContractClass() {
            return NonContractClass.var;
        }

    }

    static class NonContractClass {

        public static final String var = "Hello, world!";
    }

}

