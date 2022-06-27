package io.neow3j.compiler;

import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.contracts.ContractInterface;
import io.neow3j.script.OpCode;
import io.neow3j.script.ScriptReader;
import org.junit.Test;

import java.io.IOException;
import java.util.SortedMap;

import static io.neow3j.script.InteropService.SYSTEM_CONTRACT_CALL;
import static io.neow3j.utils.Numeric.hexStringToByteArray;
import static java.lang.String.format;
import static jdk.nashorn.internal.codegen.types.Type.getInternalName;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;

public class ContractInterfaceTest {

    @Test
    public void testWrapperCtor() throws IOException {
        CompilationUnit compUnit = new Compiler().compile(CallWrapperCtor.class.getName());
        NeoMethod neoMethod = compUnit.getNeoModule().getSortedMethods().get(0);
        SortedMap<Integer, NeoInstruction> insns = neoMethod.getInstructions();

        System.out.println(ScriptReader.convertToOpCodeString(compUnit.getNefFile().getScript()));

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
    public void testWrapperCtorWithInvalidNumberOfParameters() {
        CompilerException thrown = assertThrows(CompilerException.class,
                () -> new Compiler().compile(CallWrapperWithInvalidCtorParamsContract.class.getName()));
        assertThat(thrown.getMessage(),
                is(format("A constructor of a ContractInterface is required to take exactly one %s type as parameter.",
                        getInternalName(Hash160.class))));
    }

    @Test
    public void testWrapperCtorWithInvalidParameterType() {
        CompilerException thrown = assertThrows(CompilerException.class,
                () -> new Compiler().compile(CallWrapperWithInvalidCtorContract.class.getName()));
        assertThat(thrown.getMessage(),
                is(format("A constructor of a ContractInterface is required to take exactly one %s type as parameter.",
                        getInternalName(Hash160.class))));
    }

    static class CallWrapperCtor {
        public static String test(Hash160 hash, String value) {
            return new Wrapper(hash).getSomeValue(value);
        }
    }

    static class CallWrapperWithInvalidCtorParamsContract {
        public static Hash160 test(Hash160 hash, int i) {
            return new Wrapper(hash, i).getHash();
        }
    }

    static class CallWrapperWithInvalidCtorContract {
        public static Hash160 test(ByteString hash) {
            return new Wrapper(hash).getHash();
        }
    }

    static class Wrapper extends ContractInterface {

        public Wrapper(Hash160 contractHash) {
            super(contractHash);
        }

        public Wrapper(Hash160 contractHash, int i) {
            super(contractHash);
        }

        public Wrapper(ByteString contractHash) {
            super(new Hash160(contractHash));
        }

        public native String getSomeValue(String value);
    }

}
