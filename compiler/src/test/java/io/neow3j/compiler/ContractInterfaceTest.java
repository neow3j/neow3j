package io.neow3j.compiler;

import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.contracts.ContractInterface;
import io.neow3j.devpack.contracts.FungibleToken;
import io.neow3j.script.OpCode;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.SortedMap;

import static io.neow3j.script.InteropService.SYSTEM_CONTRACT_CALL;
import static io.neow3j.utils.Numeric.hexStringToByteArray;
import static io.neow3j.utils.Numeric.reverseHexString;
import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ContractInterfaceTest {

    @Test
    public void testContractInterfaceContractCall() throws IOException {
        CompilationUnit compUnit = new Compiler().compile(TestContractInterfaceContractCallContract.class.getName());
        NeoMethod neoMethod = compUnit.getNeoModule().getSortedMethods().get(0);
        SortedMap<Integer, NeoInstruction> insns = neoMethod.getInstructions();

        // Comments about state of the stack: top -> bottom
        assertThat(insns.get(3).getOpcode(), is(OpCode.LDARG0)); // hash
        assertThat(insns.get(4).getOpcode(), is(OpCode.LDARG1)); // value, hash

        assertThat(insns.get(5).getOpcode(), is(OpCode.PUSH1)); // 1, value, hash
        assertThat(insns.get(6).getOpcode(), is(OpCode.NEWARRAY)); // array1, value, hash
        assertThat(insns.get(7).getOpcode(), is(OpCode.DUP)); // array1, array1, value, hash

        assertThat(insns.get(8).getOpcode(), is(OpCode.ROT)); // value, array1, array1, hash
        assertThat(insns.get(9).getOpcode(), is(OpCode.PUSH0)); // 0, value, array1, array1, hash
        assertThat(insns.get(10).getOpcode(), is(OpCode.SWAP)); // value, 0, array1, array1, hash
        assertThat(insns.get(11).getOpcode(), is(OpCode.SETITEM)); // array1 (with index 0 set), hash
        assertThat(insns.get(12).getOpcode(), is(OpCode.SWAP)); // hash, array1

        assertThat(insns.get(13).getOpcode(), is(OpCode.PUSH15)); // 15 (call flags), hash, array1
        assertThat(insns.get(14).getOpcode(), is(OpCode.SWAP)); // hash, 15, array1

        assertThat(insns.get(15).getOpcode(), is(OpCode.PUSHDATA1));
        assertThat(insns.get(15).getOperand(), is("getSomeValue".getBytes())); // methodname, hash, 15, array1

        assertThat(insns.get(29).getOpcode(), is(OpCode.SWAP)); // hash, methodname, 15, array1
        assertThat(insns.get(30).getOpcode(), is(OpCode.SYSCALL));
        assertThat(insns.get(30).getOperand(),
                is(hexStringToByteArray(SYSTEM_CONTRACT_CALL.getHash()))); // method return value
        assertThat(insns.get(35).getOpcode(), is(OpCode.RET)); // returns the value remaining on the stack
    }

    @Test
    public void testToManyParams() {
        CompilerException thrown = assertThrows(CompilerException.class,
                () -> new Compiler().compile(TestToManyParamsContract.class.getName()));
        assertThat(thrown.getMessage(),
                is(format("Contract interface classes can only be initialized with a %s type or a constant %s.",
                        Hash160.class.getSimpleName(), String.class.getSimpleName())));
    }

    @Test
    public void testInvalidParamType() {
        CompilerException thrown = assertThrows(CompilerException.class,
                () -> new Compiler().compile(TestInvalidParamTypeContract.class.getName()));
        assertThat(thrown.getMessage(),
                is(format("Contract interface classes can only be initialized with a %s type or a constant %s.",
                        Hash160.class.getSimpleName(), String.class.getSimpleName())));
    }

    @Test
    public void testConstantStringParam() throws IOException {
        CompilationUnit compUnit = new Compiler().compile(TestConstantStringParamContract.class.getName());
        NeoMethod neoMethod = compUnit.getNeoModule().getSortedMethods().get(0);
        SortedMap<Integer, NeoInstruction> insns = neoMethod.getInstructions();

        // Comments about state of the stack: top -> bottom
        assertThat(insns.get(0).getOpcode(), is(OpCode.PUSHDATA1));
        assertThat(insns.get(0).getOperand(),
                is(hexStringToByteArray(reverseHexString("ef4073a0f2b305a38ec4050e4d3d28bc40ea63f5")))); // hash
        assertThat(insns.get(22).getOpcode(), is(OpCode.NEWARRAY0)); // array, hash
        assertThat(insns.get(23).getOpcode(), is(OpCode.SWAP)); // hash, array
        assertThat(insns.get(24).getOpcode(), is(OpCode.PUSH15)); // callflags, hash, array
        assertThat(insns.get(25).getOpcode(), is(OpCode.SWAP)); // hash, callflags, array
        assertThat(insns.get(26).getOpcode(), is(OpCode.PUSHDATA1));
        assertThat(insns.get(26).getOperand(), is("decimals".getBytes())); // method, hash, callflags, array
        assertThat(insns.get(36).getOpcode(), is(OpCode.SWAP)); // hash, method, callflags, array
        assertThat(insns.get(37).getOpcode(), is(OpCode.SYSCALL)); // return value
        assertThat(insns.get(42).getOpcode(), is(OpCode.RET));
    }

    @Test
    public void testConstantStringParamInvalidHash160() {
        CompilerException thrown = assertThrows(CompilerException.class,
                () -> new Compiler().compile(TestConstantStringParamInvalidHash160Contract.class.getName()));
        assertThat(thrown.getMessage(), is(format(
                "Contract interface classes can only be initialized with a %s type or a constant %s. Expected opcode " +
                        "'%s' on the stack but found '%s'.",
                Hash160.class.getSimpleName(), String.class.getSimpleName(), OpCode.PUSHDATA1, OpCode.PUSHNULL)));
    }

    static class TestConstantStringParamContract {
        public static int test() {
            return new FungibleToken("ef4073a0f2b305a38ec4050e4d3d28bc40ea63f5").decimals();
        }
    }

    static class TestConstantStringParamInvalidHash160Contract {
        public static int test() {
            return new FungibleToken((String) null).decimals();
        }
    }

    static class TestContractInterfaceContractCallContract {
        public static String test(Hash160 hash, String value) {
            return new ContractInterfaceContract(hash).getSomeValue(value);
        }
    }

    static class TestToManyParamsContract {
        public static Hash160 test(Hash160 hash, int i) {
            return new ContractInterfaceContract(hash, i).getHash();
        }
    }

    static class TestInvalidParamTypeContract {
        public static Hash160 test(ByteString hash) {
            return new ContractInterfaceContract(hash).getHash();
        }
    }

    static class ContractInterfaceContract extends ContractInterface {

        public ContractInterfaceContract(Hash160 contractHash) {
            super(contractHash);
        }

        public ContractInterfaceContract(Hash160 contractHash, int i) {
            super(contractHash);
        }

        public ContractInterfaceContract(ByteString contractHash) {
            super(new Hash160(contractHash));
        }

        public native String getSomeValue(String value);
    }

}
