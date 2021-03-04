package io.neow3j.compiler;

import static io.neow3j.contract.ContractParameter.byteArray;
import static io.neow3j.contract.ContractParameter.string;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import io.neow3j.contract.ContractParameter;
import io.neow3j.contract.Hash160;
import io.neow3j.contract.NeoToken;
import io.neow3j.contract.SmartContract;
import io.neow3j.devpack.annotations.DisplayName;
import io.neow3j.devpack.annotations.OnDeployment;
import io.neow3j.devpack.contracts.ContractManagement;
import io.neow3j.devpack.events.Event1Arg;
import io.neow3j.devpack.Contract;
import io.neow3j.protocol.ObjectMapperFactory;
import io.neow3j.protocol.core.methods.response.ArrayStackItem;
import io.neow3j.protocol.core.methods.response.NeoApplicationLog.Execution.Notification;
import io.neow3j.protocol.core.methods.response.NeoGetContractState;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.protocol.core.methods.response.NeoSendRawTransaction;
import io.neow3j.transaction.Signer;
import io.neow3j.utils.Await;
import io.neow3j.utils.Numeric;
import java.io.IOException;
import org.junit.BeforeClass;
import org.junit.Test;

public class ContractManagementIntegrationTest extends ContractTest {

    @BeforeClass
    public static void setUp() throws Throwable {
        setUp(ContractManagementIntegrationTestContract.class.getName());
    }

    @Test
    public void getContract() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(
                ContractParameter.hash160(io.neow3j.contract.NeoToken.SCRIPT_HASH));
        ArrayStackItem arrayStackItem = response.getInvocationResult().getStack().get(0).asArray();
        assertThat(arrayStackItem.get(0).asInteger().getValue().intValue(), is(-3)); // ID
        assertThat(arrayStackItem.get(1).asInteger().getValue().intValue(), is(0)); // updateCounter
        assertThat(Numeric.reverseHexString(arrayStackItem.get(2).asByteString().getAsHexString()),
                is(NeoToken.SCRIPT_HASH.toString())); // contract hash
        // nef
        assertThat(arrayStackItem.get(3).asByteString().getAsHexString(), not(isEmptyString()));
        // manifest
        assertThat(arrayStackItem.get(4).asStruct().getValue(), notNullValue());
    }

    @Test
    public void deployWithoutData() throws Throwable {
        CompilationUnit compUnit = new Compiler().compileClass(
                ContractManagementIntegrationTestContractToDeployWithoutData.class.getName());
        String manifestString = ObjectMapperFactory.getObjectMapper()
                .writeValueAsString(compUnit.getManifest());

        signAsCommittee();
        // Call the method that calls the ContractManagement.deploy(...) method and pass the
        // compiled contract to it.
        String txHash = invokeFunction(byteArray(compUnit.getNefFile().toArray()),
                string(manifestString));
        Await.waitUntilTransactionIsExecuted(txHash, neow3j);

        Notification notification = neow3j.getApplicationLog(txHash).send().getApplicationLog()
                .getExecutions().get(0).getNotifications().get(0);
        assertThat(notification.getEventName(), is("onDeployWithoutData"));

        Hash160 contractHash = SmartContract.getContractHash(committee.getScriptHash(),
                compUnit.getNefFile().getCheckSumAsInteger(), compUnit.getManifest().getName());
        NeoGetContractState result = neow3j.getContractState(contractHash.toString()).send();
        assertThat(result.getContractState(), notNullValue());
    }

    @Test
    public void deployWithData() throws Throwable {
        CompilationUnit compUnit = new Compiler().compileClass(
                ContractManagementIntegrationTestContractToDeployWithData.class.getName());
        String manifestString = ObjectMapperFactory.getObjectMapper()
                .writeValueAsString(compUnit.getManifest());

        signAsCommittee();
        // Call the method that calls the ContractManagement.deploy(...) method and pass the
        // compiled contract to it.
        String txHash = invokeFunction(byteArray(compUnit.getNefFile().toArray()),
                string(manifestString), string("hello, world!"));
        Await.waitUntilTransactionIsExecuted(txHash, neow3j);

        Notification notification = neow3j.getApplicationLog(txHash).send().getApplicationLog()
                .getExecutions().get(0).getNotifications().get(0);
        assertThat(notification.getEventName(), is("onDeployWithData"));
        assertThat(notification.getState().asArray().get(0).asByteString().getAsString(),
                is("hello, world!"));

        Hash160 contractHash = SmartContract.getContractHash(committee.getScriptHash(),
                compUnit.getNefFile().getCheckSumAsInteger(), compUnit.getManifest().getName());
        NeoGetContractState result = neow3j.getContractState(contractHash.toString()).send();
        assertThat(result.getContractState(), notNullValue());
    }

    @Test
    public void updateWithoutData() throws Throwable {
        CompilationUnit compUnit = new Compiler().compileClass(
                ContractManagementIntegrationTestContractToUpdateWithoutData.class.getName());

        // Deploy contract
        NeoSendRawTransaction response = new io.neow3j.contract.ContractManagement(neow3j)
                .deploy(compUnit.getNefFile(), compUnit.getManifest())
                .wallet(wallet)
                .signers(Signer.calledByEntry(committee.getScriptHash()))
                .sign().send();
        Await.waitUntilTransactionIsExecuted(response.getSendRawTransaction().getHash(), neow3j);

        // Check zero updates have been performed
        Hash160 contractHash = SmartContract.getContractHash(committee.getScriptHash(),
                compUnit.getNefFile().getCheckSumAsInteger(), compUnit.getManifest().getName());
        NeoGetContractState contractState = neow3j.getContractState(contractHash.toString()).send();
        assertThat(contractState.getContractState().getUpdateCounter(), is(0));

        // Compile updated version of contract
        compUnit = new Compiler().compileClass(
                ContractManagementIntegrationTestContractUpdatedWithoutData.class.getName());
        String manifestString = ObjectMapperFactory.getObjectMapper()
                .writeValueAsString(compUnit.getManifest());

        // Update the contract
        String txHash = new SmartContract(contractHash, neow3j).invokeFunction("updateWithoutData",
                byteArray(compUnit.getNefFile().toArray()), string(manifestString))
                .wallet(wallet)
                .signers(Signer.calledByEntry(committee.getScriptHash()))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        Await.waitUntilTransactionIsExecuted(txHash, neow3j);

        // Check one update has been performed
        contractState = neow3j.getContractState(contractHash.toString()).send();
        assertThat(contractState.getContractState().getUpdateCounter(), is(1));
    }

    @Test
    public void updateWithData() throws Throwable {
        CompilationUnit compUnit = new Compiler().compileClass(
                ContractManagementIntegrationTestContractToUpdateWithData.class.getName());

        // Deploy contract
        NeoSendRawTransaction response = new io.neow3j.contract.ContractManagement(neow3j)
                .deploy(compUnit.getNefFile(), compUnit.getManifest())
                .wallet(wallet)
                .signers(Signer.calledByEntry(committee.getScriptHash()))
                .sign().send();
        Await.waitUntilTransactionIsExecuted(response.getSendRawTransaction().getHash(), neow3j);

        // Check zero updates have been performed
        Hash160 contractHash = SmartContract.getContractHash(committee.getScriptHash(),
                compUnit.getNefFile().getCheckSumAsInteger(), compUnit.getManifest().getName());
        NeoGetContractState contractState = neow3j.getContractState(contractHash.toString()).send();
        assertThat(contractState.getContractState().getUpdateCounter(), is(0));

        // Compile updated version of contract
        compUnit = new Compiler().compileClass(
                ContractManagementIntegrationTestContractUpdatedWithData.class.getName());
        String manifestString = ObjectMapperFactory.getObjectMapper()
                .writeValueAsString(compUnit.getManifest());

        // Update the contract
        String txHash = new SmartContract(contractHash, neow3j).invokeFunction("updateWithData",
                byteArray(compUnit.getNefFile().toArray()), string(manifestString),
                string("hello, world!"))
                .wallet(wallet)
                .signers(Signer.calledByEntry(committee.getScriptHash()))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        Await.waitUntilTransactionIsExecuted(txHash, neow3j);

        // Check one update has been performed
        contractState = neow3j.getContractState(contractHash.toString()).send();
        assertThat(contractState.getContractState().getUpdateCounter(), is(1));
    }

    @Test
    public void destroy() throws Throwable {
        CompilationUnit res = new Compiler().compileClass(
                ContractManagementIntegrationTestContractToDestroy.class.getName());
        NeoSendRawTransaction response = new io.neow3j.contract.ContractManagement(neow3j)
                .deploy(res.getNefFile(), res.getManifest())
                .wallet(wallet)
                .signers(Signer.calledByEntry(committee.getScriptHash()))
                .sign().send();
        Await.waitUntilTransactionIsExecuted(response.getSendRawTransaction().getHash(), neow3j);

        Hash160 contractHash = SmartContract.getContractHash(committee.getScriptHash(),
                res.getNefFile().getCheckSumAsInteger(), res.getManifest().getName());
        SmartContract sc = new SmartContract(contractHash, neow3j);
        String txHash = sc.invokeFunction("destroy")
                .wallet(wallet)
                .signers(Signer.calledByEntry(committee.getScriptHash()))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        Await.waitUntilTransactionIsExecuted(txHash, neow3j);

        NeoGetContractState contractState = neow3j.getContractState(contractHash.toString()).send();
        assertThat(contractState.getError().getMessage(), is("Unknown contract"));
    }

    @Test
    public void getHash() throws Throwable {
        NeoInvokeFunction response = callInvokeFunction();
        assertThat(response.getInvocationResult().getStack().get(0).asByteString().getAsHexString(),
                is(io.neow3j.contract.ContractManagement.SCRIPT_HASH.toString()));
    }

    static class ContractManagementIntegrationTestContract {

        public static Contract getContract(io.neow3j.devpack.Hash160 contractHash) {
            return ContractManagement.getContract(contractHash);
        }

        public static io.neow3j.devpack.Hash160 getHash() {
            return ContractManagement.getHash();
        }

        public static Contract deployWithoutData(byte[] nefFile, String manifest) {
            return ContractManagement.deploy(nefFile, manifest);
        }

        public static Contract deployWithData(byte[] nefFile, String manifest, Object data) {
            return ContractManagement.deploy(nefFile, manifest, data);
        }
    }

    static class ContractManagementIntegrationTestContractToUpdateWithData {

        public static void updateWithData(byte[] nefFile, String manifest, Object data) {
            ContractManagement.update(nefFile, manifest, data);
        }

    }

    static class ContractManagementIntegrationTestContractToUpdateWithoutData {

        public static void updateWithoutData(byte[] nefFile, String manifest) {
            ContractManagement.update(nefFile, manifest);
        }

    }

    @DisplayName("ContractManagementIntegrationTest$ContractManagementIntegrationTestContractToUpdateWithData")
    static class ContractManagementIntegrationTestContractUpdatedWithData {

        static Event1Arg<Object> onUpdate;

        @OnDeployment
        public static void deploy(Object data, boolean update) {
            onUpdate.notify(data);
        }

    }

    @DisplayName(
            "ContractManagementIntegrationTest$ContractManagementIntegrationTestContractToUpdateWithoutData")
    static class ContractManagementIntegrationTestContractUpdatedWithoutData {

        static Event1Arg<Object> onUpdate;

        @OnDeployment
        public static void deploy(Object data, boolean update) {
            onUpdate.notify(data);
        }

    }

    static class ContractManagementIntegrationTestContractToDeployWithoutData {

        private static Event1Arg<String> onDeployWithoutData;

        @OnDeployment
        public static void deploy(Object data, boolean update) {
            onDeployWithoutData.notify("Deploy without data.");
        }
    }

    static class ContractManagementIntegrationTestContractToDeployWithData {

        private static Event1Arg<Object> onDeployWithData;

        @OnDeployment
        public static void deploy(Object data, boolean update) {
            onDeployWithData.notify(data);
        }
    }

    static class ContractManagementIntegrationTestContractToDestroy {

        public static void destroy() {
            ContractManagement.destroy();
        }
    }
}
