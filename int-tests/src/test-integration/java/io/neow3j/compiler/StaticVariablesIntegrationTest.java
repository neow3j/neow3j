package io.neow3j.compiler;

import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.Runtime;
import io.neow3j.devpack.Storage;
import io.neow3j.devpack.StorageMap;
import io.neow3j.devpack.annotations.Struct;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import io.neow3j.types.ContractParameter;
import io.neow3j.types.Hash256;
import org.hamcrest.core.StringEndsWith;
import org.hamcrest.core.StringStartsWith;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;

public class StaticVariablesIntegrationTest {

    private String testName;

    @RegisterExtension
    public static ContractTestExtension ct = new ContractTestExtension(
            StaticVariablesIntegrationTestContract.class.getName());

    @BeforeEach
    void init(TestInfo testInfo) {
        testName = testInfo.getTestMethod().get().getName();
    }

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
        assertThat(response.getInvocationResult().getStack().get(0).getString(), StringEndsWith.endsWith("NEO"));
    }

    @Test
    public void callStaticFinalVariableFromNonContractClass() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        assertThat(response.getInvocationResult().getStack().get(0).getString(),
                StringStartsWith.startsWith("Hello, world!"));
    }

    static class StaticVariablesIntegrationTestContract {

        private static final StorageMap map = new StorageMap(Storage.getStorageContext(), new ByteString("data"));
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

    @Struct
    static class NonContractClass {

        public static final String var = "Hello, world!";

    }

}
