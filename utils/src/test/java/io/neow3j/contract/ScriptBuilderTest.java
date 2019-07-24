package io.neow3j.contract;

import io.neow3j.constants.OpCode;
import io.neow3j.io.TestBinaryUtils;
import org.junit.Before;
import org.junit.Test;

import static io.neow3j.utils.Numeric.hexStringToByteArray;
import static java.util.Arrays.copyOfRange;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ScriptBuilderTest extends TestBinaryUtils {

    private ScriptBuilder builder;

    @Before
    public void setUp() {
        this.builder = new ScriptBuilder();
    }

    @Test
    public void pushData_ByteArray_Exactly_1Byte() {
        byte[] data = buildArray(1);
        builder.pushData(data);

        assertThat(copyOfRange(builder.toArray(), 0, 1), is(hexStringToByteArray("01")));
    }

    @Test
    public void pushData_ByteArray_Less_Than_76Bytes() {
        byte[] data = buildArray(75);
        builder.pushData(data);

        assertThat(copyOfRange(builder.toArray(), 0, 1), is(hexStringToByteArray("4B")));
    }

    @Test
    public void pushData_ByteArray_Exactly_76Bytes() {
        byte[] data = buildArray(76);
        builder.pushData(data);

        assertThat(copyOfRange(builder.toArray(), 0, 2), is(hexStringToByteArray("4C4C")));
    }

    @Test
    public void pushData_ByteArray_Exactly_77Bytes() {
        byte[] data = buildArray(77);
        builder.pushData(data);

        assertThat(copyOfRange(builder.toArray(), 0, 2), is(hexStringToByteArray("4C4D")));
    }

    @Test
    public void pushData_ByteArray_Less_Than_256Bytes() {
        byte[] data = buildArray(255);
        builder.pushData(data);

        assertThat(copyOfRange(builder.toArray(), 0, 2), is(hexStringToByteArray("4CFF")));
    }


    @Test
    public void pushData_ByteArray_Exactly_256Bytes() {
        byte[] data = buildArray(256);
        builder.pushData(data);

        assertThat(copyOfRange(builder.toArray(), 0, 3), is(hexStringToByteArray("4D0001")));
    }

    @Test
    public void pushData_ByteArray_More_Than_256Bytes() {
        byte[] data = buildArray(2769);
        builder.pushData(data);

        assertThat(copyOfRange(builder.toArray(), 0, 3), is(hexStringToByteArray("4DD10A")));
    }

    @Test
    public void pushData_ByteArray_Exactly_4096Bytes() {
        byte[] data = buildArray(4096);
        builder.pushData(data);

        assertThat(copyOfRange(builder.toArray(), 0, 3), is(hexStringToByteArray("4D0010")));
    }

    @Test
    public void pushData_ByteArray_More_Than_4096Bytes() {
        byte[] data = buildArray(14096);

        builder.pushData(data);

        assertThat(copyOfRange(builder.toArray(), 0, 3), is(hexStringToByteArray("4D1037")));
    }

    @Test
    public void pushData_ByteArray_Exactly_65535_Bytes() {
        byte[] data = buildArray(65535);
        builder.pushData(data);

        assertThat(copyOfRange(builder.toArray(), 0, 3), is(hexStringToByteArray("4Dffff")));
    }

    @Test
    public void pushData_ByteArray_Exactly_65536_Bytes() {
        byte[] data = buildArray(65536);
        builder.pushData(data);

        assertThat(copyOfRange(builder.toArray(), 0, 5), is(hexStringToByteArray("4E00000100")));
    }

    @Test
    public void pushData_Integer_0() {
        builder.pushInteger(0);
        assertThat(copyOfRange(builder.toArray(), 0, 1), is(hexStringToByteArray(OpCode.PUSH0.toString())));
    }

    @Test
    public void pushData_Integer_1() {
        builder.pushInteger(1);
        assertThat(copyOfRange(builder.toArray(), 0, 1), is(hexStringToByteArray(OpCode.PUSH1.toString())));
    }

    @Test
    public void pushData_Integer_16() {
        builder.pushInteger(16);
        assertThat(copyOfRange(builder.toArray(), 0, 1), is(hexStringToByteArray(OpCode.PUSH16.toString())));
    }

    @Test
    public void pushData_Integer_17() {
        builder.pushInteger(17);
        assertThat(copyOfRange(builder.toArray(), 0, 2), is(hexStringToByteArray("0111")));
    }

    @Test
    public void pushData_String_0() {
        builder.pushData("");
        assertThat(copyOfRange(builder.toArray(), 0, 1), is(hexStringToByteArray("00")));
    }

    @Test
    public void pushData_String_1() {
        builder.pushData("a");
        assertThat(copyOfRange(builder.toArray(), 0, 2), is(hexStringToByteArray("0161")));
    }

    @Test
    public void pushData_String_10000() {
        String data = buildString(10000);

        builder.pushData(data);
        assertThat(copyOfRange(builder.toArray(), 0, 3), is(hexStringToByteArray("4D1027")));
    }

}
