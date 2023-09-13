package io.neow3j.compiler;

import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.ECPoint;
import io.neow3j.devpack.annotations.Instruction;
import io.neow3j.protocol.core.response.NeoApplicationLog;
import io.neow3j.protocol.core.stackitem.StackItem;
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

import java.math.BigInteger;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
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
        assertThat(log.getFirstExecution().getState(), is(NeoVMStateType.HALT));
        assertThat(log.getFirstExecution().getStack(), hasSize(1));
        assertThat(log.getFirstExecution().getFirstStackItem().getType(), is(StackItemType.ARRAY));
        assertThat(log.getFirstExecution().getFirstStackItem().getList(), hasSize(2));
        assertThat(log.getFirstExecution().getFirstStackItem().getList().get(0).getType(),
                is(StackItemType.BYTE_STRING));
        assertThat(log.getFirstExecution().getFirstStackItem().getList().get(0).getString(), is("NEO"));
        assertThat(log.getFirstExecution().getFirstStackItem().getList().get(1).getType(), is(StackItemType.INTEGER));
        assertThat(log.getFirstExecution().getFirstStackItem().getList().get(1).getInteger(),
                greaterThanOrEqualTo(BigInteger.ZERO));
    }

    @Test
    public void methodWithMultipleInstructionsIncludingASyscall() throws Throwable {
        Hash256 resp = ct.invokeFunctionAndAwaitExecution(testName);
        NeoApplicationLog log = ct.getNeow3j().getApplicationLog(resp).send().getApplicationLog();

        NeoApplicationLog.Execution exec = log.getFirstExecution();
        assertThat(exec.getState(), is(NeoVMStateType.HALT));
        assertThat(exec.getStack(), hasSize(1));
        assertThat(exec.getFirstStackItem().getType(), is(StackItemType.ARRAY));
        List<StackItem> stackArray = exec.getFirstStackItem().getList();
        assertThat(stackArray, hasSize(4));

        assertThat(stackArray.get(0).getType(), is(StackItemType.BYTE_STRING));
        assertThat(stackArray.get(0).getHexString(), is("0102"));

        assertThat(stackArray.get(1).getType(), is(StackItemType.INTEGER));
        assertThat(stackArray.get(1).getInteger(), is(new BigInteger("5195086")));

        assertThat(stackArray.get(2).getType(), is(StackItemType.ARRAY));
        List<StackItem> stackArrayEntry2AsList = stackArray.get(2).getList();
        assertThat(stackArrayEntry2AsList, hasSize(2));
        assertThat(stackArrayEntry2AsList.get(0).getString(), is("state1"));
        assertThat(stackArrayEntry2AsList.get(1).getString(), is("state2"));

        assertThat(stackArray.get(3).getType(), is(StackItemType.BYTE_STRING));
        assertThat(stackArray.get(3).getString(), is("hello"));
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

        assertThat(log.getFirstExecution().getState(), is(NeoVMStateType.HALT));
        assertThat(log.getFirstExecution().getStack(), hasSize(1));
        assertThat(log.getFirstExecution().getFirstStackItem().getType(), is(StackItemType.INTEGER));
        assertThat(log.getFirstExecution().getFirstStackItem().getHexString(), is("40")); // TriggerType Application
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

        public static int methodWithSingleSyscallInstruction() {
            return TestContract2.method();
        }

    }

    static class TestContract1 {

        @Instruction(opcode = OpCode.PUSH2)
        @Instruction(opcode = OpCode.NEWARRAY)
        @Instruction(opcode = OpCode.DUP)
        @Instruction(opcode = OpCode.PUSH0)
        @Instruction(interopService = InteropService.SYSTEM_RUNTIME_PLATFORM)
        @Instruction(opcode = OpCode.SETITEM)
        @Instruction(opcode = OpCode.DUP)
        @Instruction(opcode = OpCode.PUSH1)
        @Instruction(interopService = InteropService.SYSTEM_RUNTIME_GETRANDOM)
        @Instruction(opcode = OpCode.SETITEM)
        public TestContract1() {
        }

        // name, state array, network, 0102 -> pack those 4 into an array
        // effect: return is -> array(0102, networkNr, state array, name)
        @Instruction(interopService = InteropService.SYSTEM_RUNTIME_GETNETWORK)
        @Instruction(opcode = OpCode.PUSHDATA1, operandPrefix = {0x02}, operand = {0x01, 0x02})
        @Instruction(opcode = OpCode.PUSH4)
        @Instruction(opcode = OpCode.PACK)
        public static native ByteString method(String name, Object[] state);

    }

    static class TestContract2 {

        @Instruction(interopService = InteropService.SYSTEM_CONTRACT_CREATEMULTISIGACCOUNT)
        public TestContract2(int m, ECPoint[] pubKeys) {
        }

        @Instruction(interopService = InteropService.SYSTEM_RUNTIME_GETTRIGGER)
        public static native int method();

    }

}
