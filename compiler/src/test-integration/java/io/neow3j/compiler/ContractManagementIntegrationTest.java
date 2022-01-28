package io.neow3j.compiler;

import io.neow3j.devpack.annotations.Permission;
import io.neow3j.test.TestProperties;
import io.neow3j.transaction.AccountSigner;
import io.neow3j.transaction.Transaction;
import io.neow3j.transaction.Witness;
import io.neow3j.types.Hash160;
import io.neow3j.types.Hash256;
import io.neow3j.contract.NeoToken;
import io.neow3j.contract.SmartContract;
import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.Contract;
import io.neow3j.devpack.Manifest;
import io.neow3j.devpack.annotations.DisplayName;
import io.neow3j.devpack.annotations.OnDeployment;
import io.neow3j.devpack.contracts.ContractManagement;
import io.neow3j.devpack.events.Event1Arg;
import io.neow3j.protocol.ObjectMapperFactory;
import io.neow3j.protocol.core.response.NeoApplicationLog.Execution.Notification;
import io.neow3j.protocol.core.response.NeoGetContractState;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import io.neow3j.protocol.core.response.NeoSendRawTransaction;
import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.utils.Await;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.IOException;
import java.util.List;

import static io.neow3j.crypto.Sign.signMessage;
import static io.neow3j.test.TestProperties.neoTokenHash;
import static io.neow3j.transaction.Witness.createMultiSigWitness;
import static io.neow3j.types.ContractParameter.byteArray;
import static io.neow3j.types.ContractParameter.hash160;
import static io.neow3j.types.ContractParameter.string;
import static io.neow3j.utils.Numeric.reverseHexString;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertTrue;

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
        assertThat(reverseHexString(array.get(2).getHexString()),
                is(NeoToken.SCRIPT_HASH.toString())); // contract hash
        // nef
        assertThat(array.get(3).getHexString(), not(isEmptyString()));
        // manifest
        assertThat(array.get(4).getList(), notNullValue());
    }

    @Test
    public void checkManifestValues() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName,
                hash160(new Hash160(neoTokenHash())));
        List<StackItem> array = response.getInvocationResult().getStack().get(0).getList();
        assertTrue(array.get(0).getBoolean());
        assertTrue(array.get(1).getBoolean());
        assertTrue(array.get(2).getBoolean());
        assertTrue(array.get(3).getBoolean());
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

        Hash160 contractHash = SmartContract.calcContractHash(ct.getCommittee().getScriptHash(),
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

        Hash160 contractHash = SmartContract.calcContractHash(ct.getCommittee().getScriptHash(),
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
        Transaction tx = new io.neow3j.contract.ContractManagement(ct.getNeow3j())
                .deploy(compUnit.getNefFile(), compUnit.getManifest())
                .signers(AccountSigner.calledByEntry(ct.getCommittee()))
                .getUnsignedTransaction();
        Witness multiSigWitness = createMultiSigWitness(
                asList(signMessage(tx.getHashData(), ct.getDefaultAccount().getECKeyPair())),
                ct.getCommittee().getVerificationScript());
        NeoSendRawTransaction response = tx.addWitness(multiSigWitness).send();
        Await.waitUntilTransactionIsExecuted(response.getSendRawTransaction().getHash(),
                ct.getNeow3j());

        // Check zero updates have been performed
        Hash160 contractHash = SmartContract.calcContractHash(ct.getCommittee().getScriptHash(),
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
        tx = new SmartContract(contractHash, ct.getNeow3j())
                .invokeFunction("updateWithoutData",
                        byteArray(compUnit.getNefFile().toArray()), string(manifestString))
                .signers(AccountSigner.calledByEntry(ct.getCommittee()))
                .getUnsignedTransaction();
        multiSigWitness = createMultiSigWitness(
                asList(signMessage(tx.getHashData(), ct.getDefaultAccount().getECKeyPair())),
                ct.getCommittee().getVerificationScript());
        Hash256 txHash = tx.addWitness(multiSigWitness).send().getSendRawTransaction().getHash();
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
        Transaction tx = new io.neow3j.contract.ContractManagement(ct.getNeow3j())
                .deploy(compUnit.getNefFile(), compUnit.getManifest())
                .signers(AccountSigner.calledByEntry(ct.getCommittee()))
                .getUnsignedTransaction();
        Witness multiSigWitness = createMultiSigWitness(
                asList(signMessage(tx.getHashData(), ct.getDefaultAccount().getECKeyPair())),
                ct.getCommittee().getVerificationScript());
        NeoSendRawTransaction response = tx.addWitness(multiSigWitness).send();
        Await.waitUntilTransactionIsExecuted(response.getSendRawTransaction().getHash(),
                ct.getNeow3j());

        // Check zero updates have been performed
        Hash160 contractHash = SmartContract.calcContractHash(ct.getCommittee().getScriptHash(),
                compUnit.getNefFile().getCheckSumAsInteger(), compUnit.getManifest().getName());
        NeoGetContractState contractState = ct.getNeow3j().getContractState(contractHash).send();
        assertThat(contractState.getContractState().getUpdateCounter(), is(0));

        // Compile updated version of contract
        compUnit = new Compiler().compile(
                ContractManagementIntegrationTestContractUpdatedWithData.class.getName());
        String manifestString = ObjectMapperFactory.getObjectMapper()
                .writeValueAsString(compUnit.getManifest());

        // Update the contract
        tx = new SmartContract(contractHash, ct.getNeow3j())
                .invokeFunction("updateWithData",
                        byteArray(compUnit.getNefFile().toArray()), string(manifestString),
                        string("hello, world!"))
                .signers(AccountSigner.calledByEntry(ct.getCommittee()))
                .getUnsignedTransaction();
        multiSigWitness = createMultiSigWitness(
                asList(signMessage(tx.getHashData(), ct.getDefaultAccount().getECKeyPair())),
                ct.getCommittee().getVerificationScript());
        Hash256 txHash = tx.addWitness(multiSigWitness).send().getSendRawTransaction().getHash();
        Await.waitUntilTransactionIsExecuted(txHash, ct.getNeow3j());

        // Check one update has been performed
        contractState = ct.getNeow3j().getContractState(contractHash).send();
        assertThat(contractState.getContractState().getUpdateCounter(), is(1));
    }

    @Test
    public void destroy() throws Throwable {
        CompilationUnit res = new Compiler().compile(
                ContractManagementIntegrationTestContractToDestroy.class.getName());
        Transaction tx = new io.neow3j.contract.ContractManagement(ct.getNeow3j())
                .deploy(res.getNefFile(), res.getManifest())
                .signers(AccountSigner.calledByEntry(ct.getCommittee()))
                .getUnsignedTransaction();
        Witness multiSigWitness = createMultiSigWitness(
                asList(signMessage(tx.getHashData(), ct.getDefaultAccount().getECKeyPair())),
                ct.getCommittee().getVerificationScript());
        Hash256 txHash = tx.addWitness(multiSigWitness).send().getSendRawTransaction().getHash();
        Await.waitUntilTransactionIsExecuted(txHash, ct.getNeow3j());

        Hash160 contractHash = SmartContract.calcContractHash(ct.getCommittee().getScriptHash(),
                res.getNefFile().getCheckSumAsInteger(), res.getManifest().getName());
        SmartContract sc = new SmartContract(contractHash, ct.getNeow3j());
        tx = sc.invokeFunction("destroy")
                .signers(AccountSigner.calledByEntry(ct.getCommittee()))
                .getUnsignedTransaction();
        multiSigWitness = createMultiSigWitness(
                asList(signMessage(tx.getHashData(), ct.getDefaultAccount().getECKeyPair())),
                ct.getCommittee().getVerificationScript());
        txHash = tx.addWitness(multiSigWitness).send().getSendRawTransaction().getHash();
        Await.waitUntilTransactionIsExecuted(txHash, ct.getNeow3j());

        NeoGetContractState contractState = ct.getNeow3j().getContractState(contractHash).send();
        assertThat(contractState.getError().getMessage(), is("Unknown contract"));
    }

    @Test
    public void getHash() throws Throwable {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        assertThat(response.getInvocationResult().getStack().get(0).getHexString(),
                is(reverseHexString(TestProperties.contractManagementHash())));
    }

    @Permission(contract = "0xfffdc93764dbaddd97c48f252a53ea4643faa3fd")
    static class ContractManagementIntegrationTestContract {

        public static Contract getContract(io.neow3j.devpack.Hash160 contractHash) {
            return ContractManagement.getContract(contractHash);
        }

        public static boolean[] checkManifestValues(io.neow3j.devpack.Hash160 contractHash) {
            Manifest manifest = ContractManagement.getContract(contractHash).manifest;
            boolean[] objs = new boolean[4];
            objs[0] = manifest.name == "NeoToken";
            objs[1] = manifest.abi.methods.get(0).name == "balanceOf";
            objs[2] = manifest.abi.events.get(0).name == "Transfer";
            objs[3] = manifest.supportedStandards.get(0) == "NEP-17";
            return objs;
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

    @Permission(contract = "0xfffdc93764dbaddd97c48f252a53ea4643faa3fd")
    static class ContractManagementIntegrationTestContractToUpdateWithData {

        public static void updateWithData(ByteString nefFile, String manifest, Object data) {
            ContractManagement.update(nefFile, manifest, data);
        }
    }

    @Permission(contract = "0xfffdc93764dbaddd97c48f252a53ea4643faa3fd")
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

    @Permission(contract = "0xfffdc93764dbaddd97c48f252a53ea4643faa3fd")
    static class ContractManagementIntegrationTestContractToDestroy {

        public static void destroy() {
            ContractManagement.destroy();
        }
    }

}
