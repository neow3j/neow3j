package io.neow3j.contract;

import static io.neow3j.utils.Numeric.hexStringToByteArray;
import static java.util.Arrays.copyOfRange;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

import io.neow3j.constants.InteropServiceCode;
import io.neow3j.constants.OpCode;
import io.neow3j.io.TestBinaryUtils;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class ScriptBuilderTest extends TestBinaryUtils {

    private ScriptBuilder builder;

    @Before
    public void setUp() {
        this.builder = new ScriptBuilder();
    }

    @Test
    public void pushData_ByteArray_1Byte() {
        byte[] data = buildArray(1);
        builder.pushData(data);
        assertThat(copyOfRange(builder.toArray(), 0, 2), is(hexStringToByteArray("0c01")));
    }

    @Test
    public void pushData_ByteArray_76Bytes() {
        byte[] data = buildArray(75);
        builder.pushData(data);
        assertThat(copyOfRange(builder.toArray(), 0, 2), is(hexStringToByteArray("0c4b")));
    }

    @Test
    public void pushData_ByteArray_256Bytes() {
        byte[] data = buildArray(256);
        builder.pushData(data);
        assertThat(copyOfRange(builder.toArray(), 0, 3), is(hexStringToByteArray("0d0001")));
    }

    @Test
    public void pushData_ByteArray_65536() {
        byte[] data = buildArray(65536);
        builder.pushData(data);
        assertThat(copyOfRange(builder.toArray(), 0, 5), is(hexStringToByteArray("0e00000100")));
    }

    @Test
    public void pushData_String_0() {
        builder.pushData("");
        assertThat(copyOfRange(builder.toArray(), 0, 2), is(hexStringToByteArray("0c00")));
    }

    @Test
    public void pushData_String_1() {
        builder.pushData("a");
        assertThat(copyOfRange(builder.toArray(), 0, 3), is(hexStringToByteArray("0c0161")));
    }

    @Test
    public void pushData_String_10000() {
        String data = buildString(10000);
        builder.pushData(data);
        assertThat(copyOfRange(builder.toArray(), 0, 3), is(hexStringToByteArray("0d1027")));
    }

    @Test
    public void pushInteger_0() {
        builder.pushInteger(0);
        assertThat(copyOfRange(builder.toArray(), 0, 1), is(hexStringToByteArray(OpCode.PUSH0.toString())));
    }

    @Test
    public void pushInteger_1() {
        builder.pushInteger(1);
        assertThat(copyOfRange(builder.toArray(), 0, 1), is(hexStringToByteArray(OpCode.PUSH1.toString())));
    }

    @Test
    public void pushInteger_16() {
        builder.pushInteger(16);
        assertThat(copyOfRange(builder.toArray(), 0, 1), is(hexStringToByteArray(OpCode.PUSH16.toString())));
    }

    @Test
    public void pushInteger_17() {
        builder.pushInteger(17);
        assertThat(copyOfRange(builder.toArray(), 0, 2), is(hexStringToByteArray("0011")));
    }

    @Test
    public void buildVerificationScriptFromMultiplePublicKeys() {
        final String key1 = "035fdb1d1f06759547020891ae97c729327853aeb1256b6fe0473bc2e9fa42ff50";
        final String key2 = "03eda286d19f7ee0b472afd1163d803d620a961e1581a8f2704b52c0285f6e022d";
        List<byte[]> keys = Arrays.asList(hexStringToByteArray(key1), hexStringToByteArray(key2));
        byte[] script = ScriptBuilder.buildVerificationScript(keys, 2);

        byte[] expected = hexStringToByteArray(""
                + OpCode.PUSH2.toString() // n = 2, signing threshold
                + OpCode.PUSHDATA1.toString() + "21"  // PUSHDATA 33 bytes
                + key1 // public key
                + OpCode.PUSHDATA1.toString() + "21"  // PUSHDATA 33 bytes
                + key2 // public key
                + OpCode.PUSH2.toString() // m = 2, number of keys
                + OpCode.PUSHNULL.toString()
                + OpCode.SYSCALL.toString()
                + InteropServiceCode.NEO_CRYPTO_CHECKMULTISIGWITHECDSASECP256R1.getHash()
        );
        assertArrayEquals(expected, script);
    }

    @Test
    public void buildVerificationScriptFromSinglePublicKey() {
        final String key = "035fdb1d1f06759547020891ae97c729327853aeb1256b6fe0473bc2e9fa42ff50";
        byte[] script = ScriptBuilder.buildVerificationScript(hexStringToByteArray(key));

        byte[] expected = hexStringToByteArray(""
                + OpCode.PUSHDATA1.toString() + "21"  // PUSHDATA 33 bytes
                + key // public key
                + OpCode.PUSHNULL.toString()
                + OpCode.SYSCALL.toString()
                + InteropServiceCode.NEO_CRYPTO_VERIFYWITHECDSASECP256R1.getHash()
        );
        assertArrayEquals(expected, script);
    }

}
