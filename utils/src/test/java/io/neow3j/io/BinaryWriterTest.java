package io.neow3j.io;

import io.neow3j.constants.OpCode;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static io.neow3j.utils.Numeric.hexStringToByteArray;
import static java.util.Arrays.copyOfRange;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class BinaryWriterTest extends TestBinaryUtils {

    private ByteArrayOutputStream os;
    private BinaryWriter testBinaryWriter;

    @Before
    public void setUp() {
        this.os = new ByteArrayOutputStream();
        this.testBinaryWriter = new BinaryWriter(os);
    }

    @Test
    public void pushData_ByteArray_Exactly_1Byte() throws IOException {
        byte[] data = buildArray(1);
        testBinaryWriter.pushData(data);

        assertThat(copyOfRange(os.toByteArray(), 0, 1), is(hexStringToByteArray("01")));
    }

    @Test
    public void pushData_ByteArray_Less_Than_76Bytes() throws IOException {
        byte[] data = buildArray(75);
        testBinaryWriter.pushData(data);

        assertThat(copyOfRange(os.toByteArray(), 0, 1), is(hexStringToByteArray("4B")));
    }

    @Test
    public void pushData_ByteArray_Exactly_76Bytes() throws IOException {
        byte[] data = buildArray(76);
        testBinaryWriter.pushData(data);

        assertThat(copyOfRange(os.toByteArray(), 0, 2), is(hexStringToByteArray("4C4C")));
    }

    @Test
    public void pushData_ByteArray_Exactly_77Bytes() throws IOException {
        byte[] data = buildArray(77);
        testBinaryWriter.pushData(data);

        assertThat(copyOfRange(os.toByteArray(), 0, 2), is(hexStringToByteArray("4C4D")));
    }

    @Test
    public void pushData_ByteArray_Less_Than_256Bytes() throws IOException {
        byte[] data = buildArray(255);
        testBinaryWriter.pushData(data);

        assertThat(copyOfRange(os.toByteArray(), 0, 2), is(hexStringToByteArray("4CFF")));
    }


    @Test
    public void pushData_ByteArray_Exactly_256Bytes() throws IOException {
        byte[] data = buildArray(256);
        testBinaryWriter.pushData(data);

        assertThat(copyOfRange(os.toByteArray(), 0, 3), is(hexStringToByteArray("4D0001")));
    }

    @Test
    public void pushData_ByteArray_More_Than_256Bytes() throws IOException {
        byte[] data = buildArray(2769);
        testBinaryWriter.pushData(data);

        assertThat(copyOfRange(os.toByteArray(), 0, 3), is(hexStringToByteArray("4DD10A")));
    }

    @Test
    public void pushData_ByteArray_Exactly_4096Bytes() throws IOException {
        byte[] data = buildArray(4096);
        testBinaryWriter.pushData(data);

        assertThat(copyOfRange(os.toByteArray(), 0, 5), is(hexStringToByteArray("4E00100000")));
    }

    @Test
    public void pushData_ByteArray_More_Than_4096Bytes() throws IOException {
        byte[] data = buildArray(14096);

        testBinaryWriter.pushData(data);

        assertThat(copyOfRange(os.toByteArray(), 0, 5), is(hexStringToByteArray("4E10370000")));
    }

    @Test
    public void pushData_Integer_0() throws IOException {
        testBinaryWriter.pushInteger(0);
        assertThat(copyOfRange(os.toByteArray(), 0, 1), is(hexStringToByteArray(OpCode.PUSH0.toString())));
    }

    @Test
    public void pushData_Integer_1() throws IOException {
        testBinaryWriter.pushInteger(1);
        assertThat(copyOfRange(os.toByteArray(), 0, 1), is(hexStringToByteArray(OpCode.PUSH1.toString())));
    }

    @Test
    public void pushData_Integer_16() throws IOException {
        testBinaryWriter.pushInteger(16);
        assertThat(copyOfRange(os.toByteArray(), 0, 1), is(hexStringToByteArray(OpCode.PUSH16.toString())));
    }

    @Test
    public void pushData_Integer_17() throws IOException {
        testBinaryWriter.pushInteger(17);
        assertThat(copyOfRange(os.toByteArray(), 0, 2), is(hexStringToByteArray("0111")));
    }

    @Test
    public void pushData_String_0() throws IOException {
        testBinaryWriter.pushData("");
        assertThat(copyOfRange(os.toByteArray(), 0, 1), is(hexStringToByteArray("00")));
    }

    @Test
    public void pushData_String_1() throws IOException {
        testBinaryWriter.pushData("a");
        assertThat(copyOfRange(os.toByteArray(), 0, 2), is(hexStringToByteArray("0161")));
    }

    @Test
    public void pushData_String_10000() throws IOException {
        String data = buildString(10000);

        testBinaryWriter.pushData(data);
        assertThat(copyOfRange(os.toByteArray(), 0, 5), is(hexStringToByteArray("4E10270000")));
    }

}
