package io.neow3j.compiler;

import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.Helper;
import io.neow3j.devpack.Runtime;
import io.neow3j.devpack.StringLiteralHelper;
import io.neow3j.devpack.contracts.NeoToken;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class StringLiteralHelperTest {

    private static final String UNSUPPORTED_STRING_CAT_MESSAGE = "String concatenation with an array or a type other " +
            "than string or char is not supported.";

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

    @Test
    public void unsupportedStringConcatenationWithStaticValue() {
        CompilerException thrown = assertThrows(CompilerException.class,
                () -> new Compiler().compile(UnsupportedStringConcatenationWithStaticValue.class.getName()));
        assertThat(thrown.getMessage(), is(UNSUPPORTED_STRING_CAT_MESSAGE));
    }

    @Test
    public void unsupportedStringConcatenationWithHash160() {
        CompilerException thrown = assertThrows(CompilerException.class,
                () -> new Compiler().compile(UnsupportedStringConcatenationWithHash160.class.getName()));
        assertThat(thrown.getMessage(), is(UNSUPPORTED_STRING_CAT_MESSAGE));
    }

    @Test
    public void unsupportedStringConcatenationWithMethodReturningInt() {
        CompilerException thrown = assertThrows(CompilerException.class,
                () -> new Compiler().compile(UnsupportedStringConcatenationWithMethodReturningInt.class.getName()));
        assertThat(thrown.getMessage(), is(UNSUPPORTED_STRING_CAT_MESSAGE));
    }

    @Test
    public void unsupportedStringConcatenationWithInteger() {
        CompilerException thrown = assertThrows(CompilerException.class,
                () -> new Compiler().compile(UnsupportedStringConcatenationWithInteger.class.getName()));
        assertThat(thrown.getMessage(), is(UNSUPPORTED_STRING_CAT_MESSAGE));
    }

    @Test
    public void unsupportedStringConcatenationWithArray() {
        CompilerException thrown = assertThrows(CompilerException.class,
                () -> new Compiler().compile(UnsupportedStringConcatenationWithArray.class.getName()));
        assertThat(thrown.getMessage(), is(UNSUPPORTED_STRING_CAT_MESSAGE));
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

    static class UnsupportedStringConcatenationWithStaticValue {
        static int value = 51;

        public static String main() {
            return "area" + value;
        }
    }

    static class UnsupportedStringConcatenationWithHash160 {
        public static String main(Hash160 hash160) {
            return "hash: " + Hash160.isValid(hash160);
        }
    }

    static class UnsupportedStringConcatenationWithMethodReturningInt {

        public static String main() {
            return "token" + new NeoToken().decimals();
        }
    }

    static class UnsupportedStringConcatenationWithInteger {
        public static String main(byte[] s3) {
            return Helper.toString(s3) + 25;
        }
    }

    static class UnsupportedStringConcatenationWithArray {
        public static String[] getArray() {
            return new String[]{"hello ", "world!"};
        }

        public static String main(byte[] s3) {
            return Helper.toString(s3) + getArray();
        }
    }

}
