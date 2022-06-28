package io.neow3j.compiler;

import static io.neow3j.devpack.StringLiteralHelper.hexToBytes;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import io.neow3j.compiler.sourcelookup.MockSourceContainer;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.Hash256;
import io.neow3j.types.ContractParameterType;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class HashTest {

    @Test
    public void hash160ReturnTypeShouldAppearAsHash160InManifest() throws IOException {
        CompilationUnit compUnit = new Compiler().compile(HashTestContract.class.getName());
        assertThat(compUnit.getManifest().getAbi().getMethods().get(0).getName(),
                is("methodReturningHash160"));
        assertThat(compUnit.getManifest().getAbi().getMethods().get(0).getReturnType(),
                is(ContractParameterType.HASH160));
    }

    @Test
    public void hash160ReturnTypeShouldAppearAsHash160InDebugInfo() throws IOException {
        CompilationUnit unit = new Compiler().compile(HashTestContract.class.getName(),
                asList(new MockSourceContainer(new File("/path/to/src/file/io/neow3j/compiler" +
                        "/HashTest$HashTestContract.java"))));

        assertThat(unit.getDebugInfo().getMethods().get(0).getName(),
                is("io.neow3j.compiler.HashTest$HashTestContract,methodReturningHash160"));
        assertThat(unit.getDebugInfo().getMethods().get(0).getReturnType(),
                is(ContractParameterType.HASH160.jsonValue()));
    }

    @Test
    public void hash256ReturnTypeShouldAppearAsHash256InManifest() throws IOException {
        CompilationUnit compUnit = new Compiler().compile(HashTestContract.class.getName());
        assertThat(compUnit.getManifest().getAbi().getMethods().get(1).getName(),
                is("methodReturningHash256"));
        assertThat(compUnit.getManifest().getAbi().getMethods().get(1).getReturnType(),
                is(ContractParameterType.HASH256));
    }

    @Test
    public void hash256ReturnTypeShouldAppearAsHash256InDebugInfo() throws IOException {
        CompilationUnit unit = new Compiler().compile(HashTestContract.class.getName(),
                asList(new MockSourceContainer(new File("/path/to/src/file/io/neow3j/compiler" +
                        "/HashTest$HashTestContract.java"))));

        assertThat(unit.getDebugInfo().getMethods().get(1).getName(),
                is("io.neow3j.compiler.HashTest$HashTestContract,methodReturningHash256"));
        assertThat(unit.getDebugInfo().getMethods().get(1).getReturnType(),
                is(ContractParameterType.HASH256.jsonValue()));
    }

    static class HashTestContract {

        public static Hash160 methodReturningHash160() {
            return new Hash160(hexToBytes("03b4af8d061b6b320cce6c63bc4ec7894dce107b"));
        }

        public static Hash256 methodReturningHash256() {
            return new Hash256(hexToBytes("03b4af8d061b6b320cce6c63bc4ec7894dce107b03b4af8d061b6b320cce6c63"));
        }
    }

}
