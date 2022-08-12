package io.neow3j.compiler;

import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.ECPoint;
import io.neow3j.devpack.annotations.Instruction;
import io.neow3j.protocol.core.response.NeoApplicationLog;
import io.neow3j.protocol.core.response.Notification;
import io.neow3j.script.InteropService;
import io.neow3j.script.OpCode;
import io.neow3j.types.ContractParameter;
import io.neow3j.types.Hash256;
import io.neow3j.types.NeoVMStateType;
import io.neow3j.types.StackItemType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class InstructionAnnotationsIntegrationTest {

    private String testName;

    @RegisterExtension
    public static ContractTestExtension ct = new ContractTestExtension(
            InstructionAnnotationsIntegrationTestContract.class.getName());

    @BeforeEach
    void init(TestInfo testInfo) {
        testName = testInfo.getTestMethod().get().getName();
    }

    @Test
    public void constructorWithMultipleInstructionsIncludingASyscall() throws Throwable {
        Hash256 resp = ct.invokeFunctionAndAwaitExecution(testName);
        NeoApplicationLog log = ct.getNeow3j().getApplicationLog(resp).send().getApplicationLog();
        assertThat(log.getExecutions().get(0).getState(), is(NeoVMStateType.HALT));
        List<Notification> notifications = log.getExecutions().get(0).getNotifications();
        assertThat(notifications.get(0).getEventName(), is("event"));
        assertThat(notifications.get(0).getState().getType(), is(StackItemType.ARRAY));
        assertThat(notifications.get(0).getState().getType(), is(StackItemType.ARRAY));
        assertThat(notifications.get(0).getState().getList().size(), is(0));
        assertThat(log.getExecutions().get(0).getStack().get(0).getType(), is(StackItemType.ARRAY));
        assertThat(log.getExecutions().get(0).getStack().get(0).getList().size(), is(0));
    }

    @Test
    public void methodWithMultipleInstructionsIncludingASyscall() throws Throwable {
        Hash256 resp = ct.invokeFunctionAndAwaitExecution(testName);
        NeoApplicationLog log = ct.getNeow3j().getApplicationLog(resp).send().getApplicationLog();
        assertThat(log.getExecutions().get(0).getState(), is(NeoVMStateType.HALT));
        List<Notification> notifications = log.getExecutions().get(0).getNotifications();
        assertThat(notifications.get(0).getEventName(), is("hello"));
        assertThat(notifications.get(0).getState().getType(), is(StackItemType.ARRAY));
        assertThat(notifications.get(0).getState().getList().get(0).getString(), is("state1"));
        assertThat(notifications.get(0).getState().getList().get(1).getString(), is("state2"));
    }

    @Test
    public void constructorWithSingleSyscallInstruction() throws Throwable {
        Hash256 resp = ct.invokeFunctionAndAwaitExecution(testName, ContractParameter.publicKey(
                "033a4d051b04b7fc0230d2b1aaedfd5a84be279a5361a7358db665ad7857787f1b"));
        NeoApplicationLog log = ct.getNeow3j().getApplicationLog(resp).send().getApplicationLog();
        assertThat(log.getExecutions().get(0).getState(), is(NeoVMStateType.HALT));
        assertThat(log.getExecutions().get(0).getStack().get(0).getHexString(),
                is("7f65d434362708b255f0e06856bdcb5ce99d8505"));
    }

    @Test
    public void methodWithSingleSyscallInstruction() throws Throwable {
        Hash256 resp = ct.invokeFunctionAndAwaitExecution(testName);
        NeoApplicationLog log = ct.getNeow3j().getApplicationLog(resp).send().getApplicationLog();
        assertThat(log.getExecutions().get(0).getState(), is(NeoVMStateType.HALT));
        List<Notification> notifications = log.getExecutions().get(0).getNotifications();
        assertThat(notifications.get(0).getEventName(), is("hello"));
        assertThat(notifications.get(0).getState().getType(), is(StackItemType.ARRAY));
        assertThat(notifications.get(0).getState().getList().get(0).getString(), is("state1"));
        assertThat(notifications.get(0).getState().getList().get(1).getString(), is("state2"));
    }

    static class InstructionAnnotationsIntegrationTestContract {

        public static TestContract1 constructorWithMultipleInstructionsIncludingASyscall() {
            return new TestContract1();
        }

        public static ByteString methodWithMultipleInstructionsIncludingASyscall() {
            return TestContract1.method("hello", new String[]{"state1", "state2"});
        }

        // Given the instructions on the TestContract2 constructor, this method does not actually
        // return an intance of TestContract2 but a Hash160.
        public static TestContract2 constructorWithSingleSyscallInstruction(ECPoint point) {
            return new TestContract2(1, new ECPoint[]{point});
        }

        public static void methodWithSingleSyscallInstruction() {
            TestContract2.method("hello", new String[]{"state1", "state2"});
        }

    }

    static class TestContract1 {

        @Instruction(opcode = OpCode.NEWARRAY0)
        @Instruction(opcode = OpCode.PUSHDATA1, operandPrefix = {0x05},
                operand = {0x65, 0x76, 0x65, 0x6e, 0x74})
        @Instruction(interopService = InteropService.SYSTEM_RUNTIME_NOTIFY)
        @Instruction(opcode = OpCode.NEWARRAY0)
        public TestContract1() {
        }

        @Instruction(opcode = OpCode.SWAP)
        @Instruction(interopService = InteropService.SYSTEM_RUNTIME_NOTIFY)
        @Instruction(opcode = OpCode.PUSHDATA1, operandPrefix = {0x02}, operand = {0x01, 0x02})
        public static native ByteString method(String eventName, Object[] state);

    }

    static class TestContract2 {

        @Instruction(interopService = InteropService.SYSTEM_CONTRACT_CREATEMULTISIGACCOUNT)
        public TestContract2(int m, ECPoint[] pubKeys) {
        }

        @Instruction(interopService = InteropService.SYSTEM_RUNTIME_NOTIFY)
        public static native void method(String eventName, Object[] state);

    }

}
