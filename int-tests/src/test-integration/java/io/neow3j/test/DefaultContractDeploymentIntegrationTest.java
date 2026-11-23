package io.neow3j.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ContractTest(contracts = DefaultDeploymentContract.class)
public class DefaultContractDeploymentIntegrationTest {

    @RegisterExtension
    public static final ContractTestExtension ext = new ContractTestExtension();

    @Test
    public void deploysWithBundledDefaultConfig() throws IOException {
        assertThat(ext.getDeployedContract(DefaultDeploymentContract.class).callFunctionReturningString("getValue"), is("value"));
    }

}
