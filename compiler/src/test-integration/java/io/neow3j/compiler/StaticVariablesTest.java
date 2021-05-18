package io.neow3j.compiler;

import io.neow3j.types.ContractParameter;
import io.neow3j.types.Hash256;
import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.Runtime;
import io.neow3j.devpack.Storage;
import io.neow3j.devpack.StorageMap;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import org.hamcrest.core.StringEndsWith;
import org.hamcrest.core.StringStartsWith;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.IOException;

import static org.junit.Assert.assertThat;

public class StaticVariablesTest {

    @Rule
    public TestName testName = new TestName();

    @ClassRule
    public static ContractTestRule ct = new ContractTestRule(
            StaticVariables.class.getName());

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

    static class StaticVariables {

        private static final StorageMap map = Storage.getStorageContext().createMap(new ByteString(
                "data"));
        private static final String platform = "The platform: " + Runtime.getPlatform();

        public static void putToStaticStorageMap(ByteString key, ByteString value) {
            map.put(key, value);
        }

        public static String stringConcatWithSyscall() {
            return platform;
        }

    }

}

