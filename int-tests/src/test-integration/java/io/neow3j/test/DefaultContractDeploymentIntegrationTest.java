package io.neow3j.test;

import io.neow3j.devpack.Storage;
import io.neow3j.devpack.annotations.OnDeployment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ContractTest(contracts = DefaultContractDeploymentIntegrationTest.TestContract.class)
public class DefaultContractDeploymentIntegrationTest {

    @RegisterExtension
    public static final ContractTestExtension ext = new ContractTestExtension();

    @Test
    public void deploysWithBundledDefaultConfig() throws IOException {
        assertThat(ext.getDeployedContract(TestContract.class).callFunctionReturningString("getValue"), is("value"));
    }

    public static class TestContract {

        @OnDeployment
        public static void deploy(Object data, boolean update) {
            Storage.put("key", "value");
        }

        public static String getValue() {
            return Storage.getString("key");
        }

    }

}
