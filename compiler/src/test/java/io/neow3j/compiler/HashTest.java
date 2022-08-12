package io.neow3j.compiler;

import io.neow3j.compiler.sourcelookup.MockSourceContainer;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.Hash256;
import io.neow3j.script.OpCode;
import io.neow3j.types.ContractParameterType;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static io.neow3j.devpack.StringLiteralHelper.hexToBytes;
import static io.neow3j.utils.Numeric.hexStringToByteArray;
import static io.neow3j.utils.Numeric.reverseHexString;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class HashTest {

    @Test
    public void hash160ReturnTypeShouldAppearAsHash160InManifest() throws IOException {
        CompilationUnit compUnit = new Compiler().compile(HashTestContract.class.getName());
        assertThat(compUnit.getManifest().getAbi().getMethods().get(0).getName(),
                is("hash160FromBytes"));
        assertThat(compUnit.getManifest().getAbi().getMethods().get(0).getReturnType(),
                is(ContractParameterType.HASH160));
    }

    @Test
    public void hash160ReturnTypeShouldAppearAsHash160InDebugInfo() throws IOException {
        CompilationUnit unit = new Compiler().compile(HashTestContract.class.getName(),
                asList(new MockSourceContainer(new File("/path/to/src/file/io/neow3j/compiler" +
                        "/HashTest$HashTestContract.java"))));

        assertThat(unit.getDebugInfo().getMethods().get(0).getName(),
                is("io.neow3j.compiler.HashTest$HashTestContract,hash160FromBytes"));
        assertThat(unit.getDebugInfo().getMethods().get(0).getReturnType(),
                is(ContractParameterType.HASH160.jsonValue()));
    }

    @Test
    public void hash256ReturnTypeShouldAppearAsHash256InManifest() throws IOException {
        CompilationUnit compUnit = new Compiler().compile(HashTestContract.class.getName());
        assertThat(compUnit.getManifest().getAbi().getMethods().get(1).getName(),
                is("hash256FromBytes"));
        assertThat(compUnit.getManifest().getAbi().getMethods().get(1).getReturnType(),
                is(ContractParameterType.HASH256));
    }

    @Test
    public void hash256ReturnTypeShouldAppearAsHash256InDebugInfo() throws IOException {
        CompilationUnit unit = new Compiler().compile(HashTestContract.class.getName(),
                asList(new MockSourceContainer(new File("/path/to/src/file/io/neow3j/compiler" +
                        "/HashTest$HashTestContract.java"))));

        assertThat(unit.getDebugInfo().getMethods().get(1).getName(),
                is("io.neow3j.compiler.HashTest$HashTestContract,hash256FromBytes"));
        assertThat(unit.getDebugInfo().getMethods().get(1).getReturnType(),
                is(ContractParameterType.HASH256.jsonValue()));
    }

    @Test
    public void testHash160FromString() throws IOException {
        CompilationUnit compUnit = new Compiler().compile(HashFromStringContract.class.getName());
        NeoMethod neoMethod = compUnit.getNeoModule().getSortedMethods().get(0);
        assertThat(neoMethod.getInstructions().get(0).getOpcode(), is(OpCode.PUSHDATA1));
        assertArrayEquals(neoMethod.getInstructions().get(0).getOperand(),
                hexStringToByteArray(reverseHexString("0xcc5e4edd9f5f8dba8bb65734541df7a1c081c67b")));
        assertThat(neoMethod.getInstructions().get(22).getOpcode(), is(OpCode.RET));
    }

    @Test
    public void testHash256FromString() throws IOException {
        CompilationUnit compUnit = new Compiler().compile(HashFromStringContract.class.getName());
        NeoMethod neoMethod = compUnit.getNeoModule().getSortedMethods().get(1);
        assertThat(neoMethod.getInstructions().get(0).getOpcode(), is(OpCode.PUSHDATA1));
        assertArrayEquals(neoMethod.getInstructions().get(0).getOperand(),
                hexStringToByteArray(
                        reverseHexString("0xcb30ea3c29e205e8b1233ac7fa7fa51284c40ab920a915353376014871752ca4")));
        assertThat(neoMethod.getInstructions().get(34).getOpcode(), is(OpCode.RET));
    }

    @Test
    public void testInvalidHash160FromString() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> new Compiler().compile(InvalidHash160.class.getName()));
        assertThat(thrown.getMessage(), is("Hash must be 20 bytes long but was 19 bytes."));
    }

    @Test
    public void testInvalidHash256FromString() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> new Compiler().compile(InvalidHash256.class.getName()));
        assertThat(thrown.getMessage(), is("Hash must be 32 bytes long but was 27 bytes."));
    }

    @Test
    public void testHash160FromStringWithNonConstantString() {
        CompilerException thrown = assertThrows(CompilerException.class,
                () -> new Compiler().compile(Hash160FromStringWithNonConstantString.class.getName()));
        assertThat(thrown.getMessage(),
                is("Hash160, Hash256, and ECPoint constructors with a string argument can only be used with constant " +
                        "string literals."));
    }

    @Test
    public void testHash256FromStringWithNonConstantString() {
        CompilerException thrown = assertThrows(CompilerException.class,
                () -> new Compiler().compile(Hash256FromStringWithNonConstantString.class.getName()));
        assertThat(thrown.getMessage(),
                is("Hash160, Hash256, and ECPoint constructors with a string argument can only be used with constant " +
                        "string literals."));
    }

    static class HashTestContract {

        public static Hash160 hash160FromBytes() {
            return new Hash160(hexToBytes("03b4af8d061b6b320cce6c63bc4ec7894dce107b"));
        }

        public static Hash256 hash256FromBytes() {
            return new Hash256(hexToBytes("03b4af8d061b6b320cce6c63bc4ec7894dce107b03b4af8d061b6b320cce6c63"));
        }
    }

    static class HashFromStringContract {
        public static Hash160 hash160FromString() {
            return new Hash160("0xcc5e4edd9f5f8dba8bb65734541df7a1c081c67b");
        }

        public static Hash256 hash256FromString() {
            return new Hash256("0xcb30ea3c29e205e8b1233ac7fa7fa51284c40ab920a915353376014871752ca4");
        }
    }

    static class InvalidHash160 {
        public static Hash160 test() {
            return new Hash160("0xcc5e4edd9f5f8dba8bb65734541df7a1c081c6"); // Only 19 bytes
        }
    }

    static class InvalidHash256 {
        public static Hash256 test() {
            return new Hash256("0xcb30ea3c29e205e8b1233ac7fa7fa51284c40ab920a91535337601"); // Only 27 bytes
        }
    }

    static class Hash160FromStringWithNonConstantString {
        public static Hash160 test(String value) {
            return new Hash160(value);
        }
    }

    static class Hash256FromStringWithNonConstantString {
        public static Hash256 test(String value) {
            return new Hash256(value);
        }
    }

}
