package io.neow3j.compiler;

import static org.junit.Assert.assertThat;

import io.neow3j.contract.ContractParameter;
import io.neow3j.contract.Hash256;
import io.neow3j.devpack.Runtime;
import io.neow3j.devpack.Storage;
import io.neow3j.devpack.StorageMap;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import java.io.IOException;
import org.hamcrest.core.StringEndsWith;
import org.hamcrest.core.StringStartsWith;
import org.junit.BeforeClass;
import org.junit.Test;

public class StaticVariablesTest extends ContractTest {

    @BeforeClass
    public static void setUp() throws Throwable {
        setUp(StaticVariables.class.getName());
    }

    @Test
    public void putToStaticStorageMap() throws Throwable {
        Hash256 txHash = invokeFunctionAndAwaitExecution(
                ContractParameter.string("key"),
                ContractParameter.string("value"));

        assertVMExitedWithHalt(txHash);
        assertStorageContains("data" + "key", "value");
    }

    @Test
    public void stringConcatWithSyscall() throws IOException {
        NeoInvokeFunction response = callInvokeFunction();
        assertThat(response.getInvocationResult().getStack().get(0).getString(),
                StringStartsWith.startsWith("The platform: "));
        assertThat(response.getInvocationResult().getStack().get(0).getString(),
                StringEndsWith.endsWith("NEO"));
    }

    static class StaticVariables {

        private static final StorageMap map = Storage.getStorageContext().createMap("data");
        private static final String platform = "The platform: " + Runtime.getPlatform() ;

        public static void putToStaticStorageMap(String key, String value) {
            map.put(key, value);
        }

        public static String stringConcatWithSyscall() {
            return platform;
        }

    }

}

