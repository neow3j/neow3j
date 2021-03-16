package io.neow3j.compiler;

import static io.neow3j.TestProperties.neoTokenHash;
import static io.neow3j.contract.ContractParameter.byteArray;
import static io.neow3j.contract.ContractParameter.hash160;
import static io.neow3j.contract.ContractParameter.string;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import io.neow3j.compiler.utils.ContractCompilationTestRule;
import io.neow3j.contract.Hash160;
import io.neow3j.contract.Hash256;
import io.neow3j.contract.NeoToken;
import io.neow3j.contract.SmartContract;
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
import java.io.IOException;
import java.util.List;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

public class ContractManagementIntegrationTest extends ContractTest {

    @ClassRule
    public static TestRule chain = RuleChain
            .outerRule(privateNetContainer)
            .around(
                    new ContractCompilationTestRule(
                            ContractManagementIntegrationTestContract.class.getName(),
                            privateNetContainer
                    )
            );

    @Test
    public void getContract() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(hash160(new Hash160(neoTokenHash())));
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
        CompilationUnit compUnit = new Compiler().compileClass(
                ContractManagementIntegrationTestContractToDeployWithoutData.class.getName());
        String manifestString = ObjectMapperFactory.getObjectMapper()
                .writeValueAsString(compUnit.getManifest());

        signAsCommittee();
        // Call the method that calls the ContractManagement.deploy(...) method and pass the
        // compiled contract to it.
        Hash256 txHash = invokeFunction(byteArray(compUnit.getNefFile().toArray()),
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
        Hash256 txHash = invokeFunction(byteArray(compUnit.getNefFile().toArray()),
                string(manifestString), string("hello, world!"));
        Await.waitUntilTransactionIsExecuted(txHash, neow3j);

        Notification notification = neow3j.getApplicationLog(txHash).send().getApplicationLog()
                .getExecutions().get(0).getNotifications().get(0);
        assertThat(notification.getEventName(), is("onDeployWithData"));
        assertThat(notification.getState().getList().get(0).getString(), is("hello, world!"));

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
        Hash256 txHash = new SmartContract(contractHash, neow3j).invokeFunction("updateWithoutData",
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
        Hash256 txHash = new SmartContract(contractHash, neow3j).invokeFunction("updateWithData",
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
        Hash256 txHash = sc.invokeFunction("destroy")
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
