package io.neow3j.compiler;

import static org.junit.Assert.fail;

import io.neow3j.contract.NeoToken;
import io.neow3j.devpack.StringLiteralHelper;
import io.neow3j.devpack.annotations.DisplayName;
import io.neow3j.devpack.annotations.Features;
import io.neow3j.devpack.annotations.OnDeployment;
import io.neow3j.devpack.annotations.OnVerification;
import io.neow3j.devpack.events.Event1Arg;
import io.neow3j.devpack.neo.Runtime;
import io.neow3j.protocol.core.methods.response.NeoApplicationLog.Notification;
import io.neow3j.protocol.core.methods.response.NeoSendRawTransaction;
import io.neow3j.transaction.Signer;
import io.neow3j.transaction.Transaction;
import io.neow3j.transaction.Witness;
import java.math.BigDecimal;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;

public class DeploymentMethodIntegrationTest extends ContractTest {

    @BeforeClass
    public static void setUp() throws Throwable {
        setUp(DeploymentMethodIntegrationTestContract.class.getName());
    }

    @Test
    public void callVerifyWithContractOwner() throws Throwable {
        NeoSendRawTransaction response = contract.deploy()
                .wallet(wallet)
                .signers(Signer.calledByEntry(committeeMember.getScriptHash()))
                .sign().send();
        List<Notification> notifications = neow3j.getApplicationLog(
                response.getSendRawTransaction().getHash()).send()
                .getApplicationLog().getNotifications();
        // TODO: Check if the application log contains the `deploy` event. It might be the one
        //  that is triggered on update, because this is the second time the contract gets deployed.
        fail();
    }

    static class DeploymentMethodIntegrationTestContract {

        @DisplayName("deploy")
        public static Event1Arg<String> onDeployment;

        @OnDeployment
        public static void deploy(boolean update) {
            if (update) {
                onDeployment.notify("This is an update...");
            }
            onDeployment.notify("Deploying...");
        }

    }
}
