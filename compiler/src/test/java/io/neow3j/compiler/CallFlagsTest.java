package io.neow3j.compiler;

import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.annotations.CallFlags;
import io.neow3j.devpack.contracts.ContractInterface;
import io.neow3j.script.OpCode;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.SortedMap;

import static io.neow3j.devpack.constants.CallFlags.AllowNotify;
import static io.neow3j.devpack.constants.CallFlags.ReadOnly;
import static io.neow3j.devpack.constants.CallFlags.ReadStates;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class CallFlagsTest {

    @Test
    public void testDefaultCallFlagsAll() throws IOException {
        CompilationUnit compUnit = new Compiler().compile(TestDefaultCallFlagsContract.class.getName());
        NeoMethod neoMethod = compUnit.getNeoModule().getSortedMethods().get(0);
        SortedMap<Integer, NeoInstruction> insns = neoMethod.getInstructions();

        assertThat(insns.get(3).getOpcode(), is(OpCode.LDARG0));
        assertThat(insns.get(4).getOpcode(), is(OpCode.NEWARRAY0));
        assertThat(insns.get(6).getOpcode(), is(OpCode.PUSH15)); // CallFlags All
    }

    @Test
    public void testCustomCallFlagsWithAnnotation() throws IOException {
        CompilationUnit compUnit = new Compiler().compile(CustomCallFlagsWithAnnotationContract.class.getName());
        NeoMethod neoMethod1 = compUnit.getNeoModule().getSortedMethods().get(0);
        SortedMap<Integer, NeoInstruction> insns1 = neoMethod1.getInstructions();

        assertThat(insns1.get(3).getOpcode(), is(OpCode.LDARG0));
        assertThat(insns1.get(4).getOpcode(), is(OpCode.NEWARRAY0));
        assertThat(insns1.get(5).getOpcode(), is(OpCode.SWAP));
        assertThat(insns1.get(6).getOpcode(), is(OpCode.PUSH5)); // CallFlags ReadOnly
        assertThat(insns1.get(8).getOpcode(), is(OpCode.PUSHDATA1));
        assertThat(insns1.get(8).getOperand(), is("getReadOnlyValue".getBytes()));

        NeoMethod neoMethod2 = compUnit.getNeoModule().getSortedMethods().get(1);
        SortedMap<Integer, NeoInstruction> insns2 = neoMethod2.getInstructions();

        assertThat(insns2.get(3).getOpcode(), is(OpCode.LDARG0));
        assertThat(insns2.get(4).getOpcode(), is(OpCode.NEWARRAY0));
        assertThat(insns2.get(6).getOpcode(), is(OpCode.PUSH9)); // CallFlags ReadStates and AllowNotify
        assertThat(insns2.get(8).getOpcode(), is(OpCode.PUSHDATA1));
        assertThat(insns2.get(8).getOperand(), is("allowReadAndNotifyMethod".getBytes()));
    }

    static class TestDefaultCallFlagsContract {
        public static String test(Hash160 scriptHash) {
            return new DefaultCallFlagsWrapperContract(scriptHash).getValue();
        }
    }

    static class DefaultCallFlagsWrapperContract extends ContractInterface {

        public DefaultCallFlagsWrapperContract(Hash160 contractHash) {
            super(contractHash);
        }

        public native String getValue();

    }

    static class CustomCallFlagsWithAnnotationContract {
        public static String test1(Hash160 contractHash) {
            return new CustomCallFlagsWrapperContract(contractHash).getReadOnlyValue();
        }

        public static void test2(Hash160 contractHash) {
            new CustomCallFlagsWrapperContract(contractHash).allowReadAndNotifyMethod();
        }
    }

    static class CustomCallFlagsWrapperContract extends ContractInterface {

        public CustomCallFlagsWrapperContract(Hash160 contractHash) {
            super(contractHash);
        }

        @CallFlags(ReadOnly)
        public native String getReadOnlyValue();

        @CallFlags(ReadStates | AllowNotify)
        public native void allowReadAndNotifyMethod();

    }

}
