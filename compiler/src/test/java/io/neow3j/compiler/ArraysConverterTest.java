package io.neow3j.compiler;

import static io.neow3j.model.types.StackItemType.BUFFER_CODE;
import static io.neow3j.utils.Numeric.toHexString;
import static io.neow3j.utils.Numeric.toHexStringNoPrefix;
import static org.junit.Assert.assertTrue;

import io.neow3j.constants.OpCode;
import java.io.IOException;
import org.junit.Test;

public class ArraysConverterTest {

    @Test
    public void emptyByteArrayInitializationShouldConvertToPUSHDATAInstruction()
            throws IOException {

        CompilationUnit c = new Compiler().compileClass(EmptyByteArray.class.getName());
        String script = toHexString(c.getNefFile().getScript());
        String expectedSequence = toHexStringNoPrefix((byte) OpCode.PUSHDATA1.getCode()) + "00" +
                toHexStringNoPrefix((byte) OpCode.CONVERT.getCode()) + toHexStringNoPrefix(
                BUFFER_CODE);
        assertTrue(script.contains(expectedSequence));
    }

    @Test
    public void filledByteArrayInitializationShouldConvertToPUSHDATAInstruction()
            throws IOException {

        CompilationUnit c = new Compiler().compileClass(FilledByteArray.class.getName());
        String script = toHexString(c.getNefFile().getScript());
        byte[] data = new byte[]{0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09};
        String expectedSequence = toHexStringNoPrefix((byte) OpCode.PUSHDATA1.getCode()) + "0a" +
                toHexStringNoPrefix(data) + toHexStringNoPrefix((byte) OpCode.CONVERT.getCode())
                + toHexStringNoPrefix(BUFFER_CODE);
        assertTrue(script.contains(expectedSequence));
    }

    @Test
    public void byteArrayInitializationWithSizeShouldConvertToPUSHDATAInstruction()
            throws IOException {

        CompilationUnit c = new Compiler().compileClass(ByteArrayWithSize.class.getName());
        String script = toHexString(c.getNefFile().getScript());
        byte[] data = new byte[10];
        String expectedSequence = toHexStringNoPrefix((byte) OpCode.PUSHDATA1.getCode()) + "0a" +
                toHexStringNoPrefix(data) + toHexStringNoPrefix((byte) OpCode.CONVERT.getCode()) +
                toHexStringNoPrefix(BUFFER_CODE);
        assertTrue(script.contains(expectedSequence));
    }

    static class EmptyByteArray {

        public static byte[] construct() {
            return new byte[]{};
        }

    }

    static class FilledByteArray {

        public static byte[] construct() {
            return new byte[]{0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09};
        }

    }

    static class ByteArrayWithSize {

        public static byte[] construct() {
            return new byte[10];
        }

    }
}
