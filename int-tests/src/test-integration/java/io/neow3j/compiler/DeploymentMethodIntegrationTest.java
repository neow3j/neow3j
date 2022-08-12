package io.neow3j.compiler;

import io.neow3j.devpack.annotations.OnDeployment;
import io.neow3j.devpack.events.Event2Args;
import io.neow3j.protocol.core.response.NeoApplicationLog.Execution;
import io.neow3j.utils.Numeric;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class DeploymentMethodIntegrationTest {

    @RegisterExtension
    public static ContractTestExtension ct = new ContractTestExtension(
            DeploymentMethodIntegrationTestContract.class.getName());

    @Test
    public void deployingContractWithDeployMethod() throws Throwable {
        List<Execution> executions = ct.getNeow3j().getApplicationLog(ct.getDeployTxHash()).send()
                .getApplicationLog().getExecutions();

        assertThat(executions.get(0).getNotifications().get(0).getEventName(), is("onDeploy"));
        assertThat(executions.get(0).getNotifications().get(0).getState().getList().get(0).getString(),
                is("Deployed contract."));

        // Deploy event generated by ManagementContract
        assertThat(executions.get(0).getNotifications().get(1).getEventName(), is("Deploy"));
        String message = executions.get(0).getNotifications().get(1).getState().getList().get(0).getHexString();
        assertThat(Numeric.reverseHexString(message), is(ct.getContract().getScriptHash().toString()));
    }

    static class DeploymentMethodIntegrationTestContract {

        static Event2Args<String, Object> onDeploy;

        @OnDeployment
        public static void deploy(Object data, boolean update) {
            if (update) {
                return;
            }
            onDeploy.fire("Deployed contract.", data);
        }

    }

}
