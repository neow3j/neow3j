package io.neow3j.compiler;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.ECPoint;
import io.neow3j.model.types.ContractParameterType;
import java.io.IOException;
import org.junit.Test;

public class ECPointTest {

    @Test
    public void ecPointReturnTypeShouldAppearAsPublicKeyInManifest() throws IOException {
        CompilationUnit compUnit = new Compiler().compileClass(ECPointTestContract.class.getName());
        assertThat(compUnit.getManifest().getAbi().getMethods().get(0).getName(), is("methodReturningECPoint"));
        assertThat(compUnit.getManifest().getAbi().getMethods().get(0).getReturnType(),
                is(ContractParameterType.PUBLIC_KEY));
    }

    @Test
    public void ecPointReturnTypeShouldAppearAsPublicKeyInDebugInfo() throws IOException {
        CompilationUnit unit = new Compiler().compileClass(ECPointTestContract.class.getName(),
                "/path/to/src/file/io/neow3j/compiler/ECPointTest$ECPointTestContract.java");
        assertThat(unit.getDebugInfo().getMethods().get(0).getName(),
                is("io.neow3j.compiler.ECPointTest$ECPointTestContract,methodReturningECPoint"));
        assertThat(unit.getDebugInfo().getMethods().get(0).getReturnType(),
                is(ContractParameterType.PUBLIC_KEY.jsonValue()));
    }

    static class ECPointTestContract {

        public static ECPoint methodReturningECPoint() {
            return new ECPoint(new ByteString(
                    "03b4af8d061b6b320cce6c63bc4ec7894dce107bfc5f5ef5c68a93b4ad1e136816"));
        }
    }
}
