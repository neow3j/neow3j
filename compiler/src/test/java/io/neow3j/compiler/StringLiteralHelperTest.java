package io.neow3j.compiler;

import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.StringLiteralHelper;
import io.neow3j.devpack.Runtime;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.Assert.assertThrows;

public class StringLiteralHelperTest {

    @Test
    public void invalidAddressStringLiteralThrowsCompilerExceptions() {
        CompilerException thrown = assertThrows(CompilerException.class,
                () -> new Compiler().compile(InvalidAddressVariable.class.getName())
        );
        assertThat(thrown.getMessage(), stringContainsInOrder(
                asList("Invalid address", "A0unErzotcQTNWP2qktA7LgkXZVdHea97H")));
    }

    @Test
    public void invalidHexStringLiteralThrowsCompilerExceptions() {
        CompilerException thrown = assertThrows(CompilerException.class,
                () -> new Compiler().compile(InvalidHexStringVariable.class.getName())
        );
        assertThat(thrown.getMessage(),
                stringContainsInOrder(asList("Invalid hex string", "0x0h02")));
    }

    @Test
    public void invalidIntegerStringLiteralThrowsCompilerExceptions() {
        CompilerException thrown = assertThrows(CompilerException.class,
                () -> new Compiler().compile(InvalidIntStringVariable.class.getName())
        );
        assertThat(thrown.getMessage(), stringContainsInOrder(
                asList("Invalid number string", "100e0000000000000000000000000000")));
    }

    @Test
    public void illegalInputToConverterMethodLeadsToCompilerException() {
        CompilerException thrown = assertThrows(CompilerException.class,
                () -> new Compiler().compile(IllegalInputConverterMethod.class.getName())
        );
        assertThat(thrown.getMessage(), containsString("constant string literals"));
    }

    static class InvalidAddressVariable {

        private static final Hash160 scriptHash = StringLiteralHelper.addressToScriptHash(
                "A0unErzotcQTNWP2qktA7LgkXZVdHea97H");

        public static Hash160 main() {
            return scriptHash;
        }

    }

    static class InvalidHexStringVariable {

        private static final ByteString bytes = StringLiteralHelper.hexToBytes("0x0h02");

        public static ByteString main() {
            return bytes;
        }

    }

    static class InvalidIntStringVariable {

        private static final int integer = StringLiteralHelper.stringToInt(
                "100e0000000000000000000000000000");

        public static int main() {
            return integer;
        }

    }

    static class IllegalInputConverterMethod {

        private static final ByteString bytes =
                StringLiteralHelper.hexToBytes(Runtime.getPlatform());

        public static ByteString main() {
            return bytes;
        }

    }

}
