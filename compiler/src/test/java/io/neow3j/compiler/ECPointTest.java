package io.neow3j.compiler;

import io.neow3j.compiler.sourcelookup.MockSourceContainer;
import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.ECPoint;
import io.neow3j.types.ContractParameterType;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ECPointTest {

    @Test
    public void ecPointReturnTypeShouldAppearAsPublicKeyInManifest() throws IOException {
        CompilationUnit compUnit = new Compiler().compile(ECPointTestContract.class.getName());
        assertThat(compUnit.getManifest().getAbi().getMethods().get(0).getName(), is(
                "methodReturningECPoint"));
        assertThat(compUnit.getManifest().getAbi().getMethods().get(0).getReturnType(),
                is(ContractParameterType.PUBLIC_KEY));
    }

    @Test
    public void ecPointReturnTypeShouldAppearAsPublicKeyInDebugInfo() throws IOException {
        CompilationUnit unit = new Compiler().compile(ECPointTestContract.class.getName(),
                asList(new MockSourceContainer(new File("/path/to/src/file/io/neow3j/compiler" +
                        "/ECPointTest$ECPointTestContract.java"))));

        assertThat(unit.getDebugInfo().getMethods().get(0).getName(),
                is("io.neow3j.compiler.ECPointTest$ECPointTestContract,methodReturningECPoint"));
        assertThat(unit.getDebugInfo().getMethods().get(0).getReturnType(),
                is(ContractParameterType.PUBLIC_KEY.jsonValue()));
    }

    @Test
    public void testECPointFromStringWithNonConstantString() {
        CompilerException thrown = assertThrows(CompilerException.class,
                () -> new Compiler().compile(ECPointFromStringWithNonConstantString.class.getName()));
        assertThat(thrown.getMessage(),
                is("Hash160, Hash256, and ECPoint constructors with a string argument can only be used with constant " +
                        "string literals."));
    }

    @Test
    public void testInvalidECPointFromString() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> new Compiler().compile(InvalidECPoint.class.getName()));
        assertThat(thrown.getMessage(), containsString("Invalid point encoding"));
    }

    static class ECPointTestContract {

        public static ECPoint methodReturningECPoint() {
            return new ECPoint(new ByteString("03b4af8d061b6b320cce6c63bc4ec7894dce107bfc5f5ef5c68a93b4ad1e136816"));
        }
    }

    static class InvalidECPoint {
        public static ECPoint test() {
            return new ECPoint("0xcb30ea3c29e205e8b1233ac7fa7fa51284c40ab920a91535337601"); // Only 27 bytes
        }
    }

    static class ECPointFromStringWithNonConstantString {
        public static ECPoint test(String value) {
            return new ECPoint(value);
        }
    }

}
