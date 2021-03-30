package io.neow3j.compiler;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.Hash256;
import io.neow3j.model.types.ContractParameterType;
import java.io.IOException;
import org.junit.Test;

public class HashTest {

    @Test
    public void hash160ReturnTypeShouldAppearAsHash160InManifest() throws IOException {
        CompilationUnit compUnit = new Compiler().compileClass(HashTestContract.class.getName());
        assertThat(compUnit.getManifest().getAbi().getMethods().get(0).getName(),
                is("methodReturningHash160"));
        assertThat(compUnit.getManifest().getAbi().getMethods().get(0).getReturnType(),
                is(ContractParameterType.HASH160));
    }

    @Test
    public void hash160ReturnTypeShouldAppearAsHash160InDebugInfo() throws IOException {
        CompilationUnit unit = new Compiler().compileClass(HashTestContract.class.getName(),
                "/path/to/src/file/io/neow3j/compiler/HashTest$HashTestContract.java");
        assertThat(unit.getDebugInfo().getMethods().get(0).getName(),
                is("io.neow3j.compiler.HashTest$HashTestContract,methodReturningHash160"));
        assertThat(unit.getDebugInfo().getMethods().get(0).getReturnType(),
                is(ContractParameterType.HASH160.jsonValue()));
    }

    @Test
    public void hash256ReturnTypeShouldAppearAsHash256InManifest() throws IOException {
        CompilationUnit compUnit = new Compiler().compileClass(HashTestContract.class.getName());
        assertThat(compUnit.getManifest().getAbi().getMethods().get(1).getName(),
                is("methodReturningHash256"));
        assertThat(compUnit.getManifest().getAbi().getMethods().get(1).getReturnType(),
                is(ContractParameterType.HASH256));
    }

    @Test
    public void hash256ReturnTypeShouldAppearAsHash256InDebugInfo() throws IOException {
        CompilationUnit unit = new Compiler().compileClass(HashTestContract.class.getName(),
                "/path/to/src/file/io/neow3j/compiler/HashTest$HashTestContract.java");
        assertThat(unit.getDebugInfo().getMethods().get(1).getName(),
                is("io.neow3j.compiler.HashTest$HashTestContract,methodReturningHash256"));
        assertThat(unit.getDebugInfo().getMethods().get(1).getReturnType(),
                is(ContractParameterType.HASH256.jsonValue()));
    }

    static class HashTestContract {

        public static Hash160 methodReturningHash160() {
            return new Hash160(new ByteString("03b4af8d061b6b320cce6c63bc4ec7894dce107b"));
        }

        public static Hash256 methodReturningHash256() {
            return new Hash256(new ByteString("03b4af8d061b6b320cce6c63bc4ec7894dce107b03b4af8d061b6b320cce6c63"));
        }
    }
}


