package io.neow3j.compiler;

import io.neow3j.types.Hash160;
import io.neow3j.types.Hash256;
import io.neow3j.contract.NeoToken;
import io.neow3j.contract.SmartContract;
import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.Contract;
import io.neow3j.devpack.annotations.DisplayName;
import io.neow3j.devpack.annotations.OnDeployment;
import io.neow3j.devpack.contracts.ContractManagement;
import io.neow3j.devpack.events.Event1Arg;
import io.neow3j.protocol.ObjectMapperFactory;
import io.neow3j.protocol.core.methods.response.NeoApplicationLog.Execution.Notification;
import io.neow3j.protocol.core.methods.response.NeoGetContractState;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.protocol.core.methods.response.NeoSendRawTransaction;
import io.neow3j.protocol.core.methods.response.StackItem;
import io.neow3j.transaction.Signer;
import io.neow3j.utils.Await;
import io.neow3j.utils.Numeric;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.IOException;
import java.util.List;

import static io.neow3j.TestProperties.neoTokenHash;
import static io.neow3j.types.ContractParameter.byteArray;
import static io.neow3j.types.ContractParameter.hash160;
import static io.neow3j.types.ContractParameter.string;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class ContractManagementIntegrationTest {

    @Rule
    public TestName testName = new TestName();

    @ClassRule
    public static ContractTestRule ct = new ContractTestRule(
            ContractManagementIntegrationTestContract.class.getName());

    @Test
    public void getContract() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName,
                hash160(new Hash160(neoTokenHash())));
        List<StackItem> array = response.getInvocationResult().getStack().get(0).getList();
        assertThat(array.get(0).getInteger().intValue(), is(-5)); // ID
        assertThat(array.get(1).getInteger().intValue(), is(0)); // updateCounter
        assertThat(Numeric.reverseHexString(array.get(2).getHexString()),
                is(NeoToken.SCRIPT_HASH.toString())); // contract hash
        // nef
        assertThat(array.get(3).getHexString(), not(isEmptyString()));
        // manifest
        assertThat(array.get(4).getList(), notNullValue());
    }

    @Test
    public void deployWithoutData() throws Throwable {
        CompilationUnit compUnit = new Compiler().compile(
                ContractManagementIntegrationTestContractToDeployWithoutData.class.getName());
        String manifestString = ObjectMapperFactory.getObjectMapper()
                .writeValueAsString(compUnit.getManifest());

        ct.signWithCommitteeAccount();
        // Call the method that calls the ContractManagement.deploy(...) method and pass the
        // compiled contract to it.
        Hash256 txHash = ct.invokeFunction(testName, byteArray(compUnit.getNefFile().toArray()),
                string(manifestString));
        Await.waitUntilTransactionIsExecuted(txHash, ct.getNeow3j());

        Notification notification =
                ct.getNeow3j().getApplicationLog(txHash).send().getApplicationLog()
                        .getExecutions().get(0).getNotifications().get(0);
        assertThat(notification.getEventName(), is("onDeployWithoutData"));

        Hash160 contractHash = SmartContract.getContractHash(ct.getCommittee().getScriptHash(),
                compUnit.getNefFile().getCheckSumAsInteger(), compUnit.getManifest().getName());
        NeoGetContractState result =
                ct.getNeow3j().getContractState(contractHash).send();
        assertThat(result.getContractState(), notNullValue());
    }

    @Test
    public void deployWithData() throws Throwable {
        CompilationUnit compUnit = new Compiler().compile(
                ContractManagementIntegrationTestContractToDeployWithData.class.getName());
        String manifestString = ObjectMapperFactory.getObjectMapper()
                .writeValueAsString(compUnit.getManifest());

        ct.signWithCommitteeAccount();
        // Call the method that calls the ContractManagement.deploy(...) method and pass the
        // compiled contract to it.
        Hash256 txHash = ct.invokeFunction(testName, byteArray(compUnit.getNefFile().toArray()),
                string(manifestString), string("hello, world!"));
        Await.waitUntilTransactionIsExecuted(txHash, ct.getNeow3j());

        Notification notification = ct.getNeow3j().getApplicationLog(txHash).send()
                .getApplicationLog().getExecutions().get(0).getNotifications().get(0);
        assertThat(notification.getEventName(), is("onDeployWithData"));
        assertThat(notification.getState().getList().get(0).getString(), is("hello, world!"));

        Hash160 contractHash = SmartContract.getContractHash(ct.getCommittee().getScriptHash(),
                compUnit.getNefFile().getCheckSumAsInteger(), compUnit.getManifest().getName());
        NeoGetContractState result =
                ct.getNeow3j().getContractState(contractHash).send();
        assertThat(result.getContractState(), notNullValue());
    }

    @Test
    public void updateWithoutData() throws Throwable {
        CompilationUnit compUnit = new Compiler().compile(
                ContractManagementIntegrationTestContractToUpdateWithoutData.class.getName());

        // Deploy contract
        NeoSendRawTransaction response = new io.neow3j.contract.ContractManagement(ct.getNeow3j())
                .deploy(compUnit.getNefFile(), compUnit.getManifest())
                .wallet(ct.getWallet())
                .signers(Signer.calledByEntry(ct.getCommittee().getScriptHash()))
                .sign().send();
        Await.waitUntilTransactionIsExecuted(response.getSendRawTransaction().getHash(),
                ct.getNeow3j());

        // Check zero updates have been performed
        Hash160 contractHash = SmartContract.getContractHash(ct.getCommittee().getScriptHash(),
                compUnit.getNefFile().getCheckSumAsInteger(), compUnit.getManifest().getName());
        NeoGetContractState contractState =
                ct.getNeow3j().getContractState(contractHash).send();
        assertThat(contractState.getContractState().getUpdateCounter(), is(0));

        // Compile updated version of contract
        compUnit = new Compiler().compile(
                ContractManagementIntegrationTestContractUpdatedWithoutData.class.getName());
        String manifestString = ObjectMapperFactory.getObjectMapper()
                .writeValueAsString(compUnit.getManifest());

        // Update the contract
        Hash256 txHash =
                new SmartContract(contractHash, ct.getNeow3j()).invokeFunction("updateWithoutData",
                        byteArray(compUnit.getNefFile().toArray()), string(manifestString))
                        .wallet(ct.getWallet())
                        .signers(Signer.calledByEntry(ct.getCommittee().getScriptHash()))
                        .sign()
                        .send()
                        .getSendRawTransaction()
                        .getHash();
        Await.waitUntilTransactionIsExecuted(txHash, ct.getNeow3j());

        // Check one update has been performed
        contractState = ct.getNeow3j().getContractState(contractHash).send();
        assertThat(contractState.getContractState().getUpdateCounter(), is(1));
    }

    @Test
    public void updateWithData() throws Throwable {
        CompilationUnit compUnit = new Compiler().compile(
                ContractManagementIntegrationTestContractToUpdateWithData.class.getName());

        // Deploy contract
        NeoSendRawTransaction response = new io.neow3j.contract.ContractManagement(ct.getNeow3j())
                .deploy(compUnit.getNefFile(), compUnit.getManifest())
                .wallet(ct.getWallet())
                .signers(Signer.calledByEntry(ct.getCommittee().getScriptHash()))
                .sign().send();
        Await.waitUntilTransactionIsExecuted(response.getSendRawTransaction().getHash(),
                ct.getNeow3j());

        // Check zero updates have been performed
        Hash160 contractHash = SmartContract.getContractHash(ct.getCommittee().getScriptHash(),
                compUnit.getNefFile().getCheckSumAsInteger(), compUnit.getManifest().getName());
        NeoGetContractState contractState =
                ct.getNeow3j().getContractState(contractHash).send();
        assertThat(contractState.getContractState().getUpdateCounter(), is(0));

        // Compile updated version of contract
        compUnit = new Compiler().compile(
                ContractManagementIntegrationTestContractUpdatedWithData.class.getName());
        String manifestString = ObjectMapperFactory.getObjectMapper()
                .writeValueAsString(compUnit.getManifest());

        // Update the contract
        Hash256 txHash =
                new SmartContract(contractHash, ct.getNeow3j()).invokeFunction("updateWithData",
                        byteArray(compUnit.getNefFile().toArray()), string(manifestString),
                        string("hello, world!"))
                        .wallet(ct.getWallet())
                        .signers(Signer.calledByEntry(ct.getCommittee().getScriptHash()))
                        .sign()
                        .send()
                        .getSendRawTransaction()
                        .getHash();
        Await.waitUntilTransactionIsExecuted(txHash, ct.getNeow3j());

        // Check one update has been performed
        contractState = ct.getNeow3j().getContractState(contractHash).send();
        assertThat(contractState.getContractState().getUpdateCounter(), is(1));
    }

    @Test
    public void destroy() throws Throwable {
        CompilationUnit res = new Compiler().compile(
                ContractManagementIntegrationTestContractToDestroy.class.getName());
        NeoSendRawTransaction response = new io.neow3j.contract.ContractManagement(ct.getNeow3j())
                .deploy(res.getNefFile(), res.getManifest())
                .wallet(ct.getWallet())
                .signers(Signer.calledByEntry(ct.getCommittee().getScriptHash()))
                .sign().send();
        Await.waitUntilTransactionIsExecuted(response.getSendRawTransaction().getHash(),
                ct.getNeow3j());

        Hash160 contractHash = SmartContract.getContractHash(ct.getCommittee().getScriptHash(),
                res.getNefFile().getCheckSumAsInteger(), res.getManifest().getName());
        SmartContract sc = new SmartContract(contractHash, ct.getNeow3j());
        Hash256 txHash = sc.invokeFunction("destroy")
                .wallet(ct.getWallet())
                .signers(Signer.calledByEntry(ct.getCommittee().getScriptHash()))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        Await.waitUntilTransactionIsExecuted(txHash, ct.getNeow3j());

        NeoGetContractState contractState =
                ct.getNeow3j().getContractState(contractHash).send();
        assertThat(contractState.getError().getMessage(), is("Unknown contract"));
    }

    @Test
    public void getHash() throws Throwable {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        assertThat(response.getInvocationResult().getStack().get(0).getHexString(),
                is(io.neow3j.contract.ContractManagement.SCRIPT_HASH.toString()));
    }

    static class ContractManagementIntegrationTestContract {

        public static Contract getContract(io.neow3j.devpack.Hash160 contractHash) {
            return ContractManagement.getContract(contractHash);
        }

        public static io.neow3j.devpack.Hash160 getHash() {
            return ContractManagement.getHash();
        }

        public static Contract deployWithoutData(ByteString nefFile, String manifest) {
            return ContractManagement.deploy(nefFile, manifest);
        }

        public static Contract deployWithData(ByteString nefFile, String manifest, Object data) {
            return ContractManagement.deploy(nefFile, manifest, data);
        }
    }

    static class ContractManagementIntegrationTestContractToUpdateWithData {

        public static void updateWithData(ByteString nefFile, String manifest, Object data) {
            ContractManagement.update(nefFile, manifest, data);
        }

    }

    static class ContractManagementIntegrationTestContractToUpdateWithoutData {

        public static void updateWithoutData(ByteString nefFile, String manifest) {
            ContractManagement.update(nefFile, manifest);
        }

    }

    @DisplayName(
            "ContractManagementIntegrationTest$ContractManagementIntegrationTestContractToUpdateWithData")
    static class ContractManagementIntegrationTestContractUpdatedWithData {

        static Event1Arg<Object> onUpdate;

        @OnDeployment
        public static void deploy(Object data, boolean update) {
            onUpdate.fire(data);
        }

    }

    @DisplayName(
            "ContractManagementIntegrationTest$ContractManagementIntegrationTestContractToUpdateWithoutData")
    static class ContractManagementIntegrationTestContractUpdatedWithoutData {

        static Event1Arg<Object> onUpdate;

        @OnDeployment
        public static void deploy(Object data, boolean update) {
            onUpdate.fire(data);
        }

    }

    static class ContractManagementIntegrationTestContractToDeployWithoutData {

        private static Event1Arg<String> onDeployWithoutData;

        @OnDeployment
        public static void deploy(Object data, boolean update) {
            onDeployWithoutData.fire("Deploy without data.");
        }
    }

    static class ContractManagementIntegrationTestContractToDeployWithData {

        private static Event1Arg<Object> onDeployWithData;

        @OnDeployment
        public static void deploy(Object data, boolean update) {
            onDeployWithData.fire(data);
        }
    }

    static class ContractManagementIntegrationTestContractToDestroy {

        public static void destroy() {
            ContractManagement.destroy();
        }
    }

}
