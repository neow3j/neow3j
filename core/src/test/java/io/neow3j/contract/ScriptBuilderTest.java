package io.neow3j.contract;

import static io.neow3j.types.ContractParameter.bool;
import static io.neow3j.types.ContractParameter.byteArray;
import static io.neow3j.types.ContractParameter.integer;
import static io.neow3j.types.ContractParameter.map;
import static io.neow3j.types.ContractParameter.string;
import static io.neow3j.utils.Numeric.hexStringToByteArray;
import static java.util.Arrays.copyOfRange;
import static org.hamcrest.Matchers.isOneOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

import io.neow3j.script.InteropService;
import io.neow3j.script.OpCode;
import io.neow3j.serialization.TestBinaryUtils;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import io.neow3j.script.ScriptBuilder;
import io.neow3j.types.ContractParameter;
import io.neow3j.utils.ArrayUtils;
import io.neow3j.utils.Numeric;
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
        assertThat(copyOfRange(builder.toArray(), 0, 1),
                is(hexStringToByteArray(OpCode.PUSH0.toString())));
    }

    @Test
    public void pushInteger_1() {
        builder.pushInteger(1);
        assertThat(copyOfRange(builder.toArray(), 0, 1),
                is(hexStringToByteArray(OpCode.PUSH1.toString())));
    }

    @Test
    public void pushInteger_16() {
        builder.pushInteger(16);
        assertThat(copyOfRange(builder.toArray(), 0, 1),
                is(hexStringToByteArray(OpCode.PUSH16.toString())));
    }

    @Test
    public void pushInteger_17() {
        builder.pushInteger(17);
        assertThat(copyOfRange(builder.toArray(), 0, 2), is(hexStringToByteArray("0011")));
    }

    @Test
    public void pushInteger_minus800_000() {
        builder.pushInteger(-800_000);
        byte[] bytes = builder.toArray();
        assertThat(bytes.length, is(5)); // PUSHINT opcode plus 4 integer bytes.
        bytes = ArrayUtils.getLastNBytes(bytes, 4); // Remove the PUSHINT opcode byte.
        // Two's complement of -800'000 in big-endian order
        byte[] expected = new byte[]{(byte) 0xff, (byte) 0xf3, (byte) 0xcb, 0x00};
        assertThat(bytes, is(ArrayUtils.reverseArray(expected)));
    }

    @Test
    public void pushInteger_minus100_000_000_000() {
        builder.pushInteger(-100_000_000_000L);
        byte[] bytes = builder.toArray();
        assertThat(bytes.length, is(9)); // PUSHINT opcode plus 8 integer bytes.
        bytes = ArrayUtils.getLastNBytes(bytes, 8); // Remove the PUSHINT opcode byte.
        // Two's complement of -100'000'000'000 in big-endian order
        byte[] expected = new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xe8,
                (byte) 0xb7, (byte) 0x89, 0x18, 0x00};
        assertThat(bytes, is(ArrayUtils.reverseArray(expected)));
    }

    @Test
    public void pushInteger_plus100_000_000_000() {
        builder.pushInteger(100_000_000_000L);
        byte[] bytes = builder.toArray();
        assertThat(bytes.length, is(9)); // PUSHINT opcode plus 8 integer bytes.
        bytes = ArrayUtils.getLastNBytes(bytes, 8); // Remove the PUSHINT opcode byte.
        // Two's complement of -100'000'000'000 in big-endian order
        byte[] expected = new byte[]{0x00, 0x00, 0x00, 0x17, 0x48, 0x76, (byte) 0xe8, 0x00};
        assertThat(bytes, is(ArrayUtils.reverseArray(expected)));
    }


    @Test
    public void pushInteger_minus10ToThePowerOf23() {
        builder.pushInteger(BigInteger.TEN.pow(23).negate());
        byte[] bytes = builder.toArray();
        assertThat(bytes.length, is(17)); // PUSHINT opcode plus 16 integer bytes.
        bytes = ArrayUtils.getLastNBytes(bytes, 16); // Remove the PUSHINT opcode byte.
        // Two's complement in big-endian order.
        byte[] expected = hexStringToByteArray("ffffffffffffead2fd381eb509800000");
        assertThat(bytes, is(ArrayUtils.reverseArray(expected)));
    }

    @Test
    public void pushInteger_plus10ToThePowerOf23() {
        builder.pushInteger(BigInteger.TEN.pow(23));
        byte[] bytes = builder.toArray();
        assertThat(bytes.length, is(17)); // PUSHINT opcode plus 16 integer bytes.
        bytes = ArrayUtils.getLastNBytes(bytes, 16); // Remove the PUSHINT opcode byte.
        // Two's complement in big-endian order.
        byte[] expected = hexStringToByteArray("000000000000152d02c7e14af6800000");
        assertThat(bytes, is(ArrayUtils.reverseArray(expected)));
    }

    @Test
    public void pushInteger_minus10ToThePowerOf30() {
        builder.pushInteger(BigInteger.TEN.pow(40).negate());
        byte[] bytes = builder.toArray();
        assertThat(bytes.length, is(33)); // PUSHINT opcode plus 32 integer bytes.
        bytes = ArrayUtils.getLastNBytes(bytes, 32); // Remove the PUSHINT opcode byte.
        // Two's complement in big-endian order.
        byte[] expected = hexStringToByteArray("0xffffffffffffffffffffffffffffffe29cd60e3ca35b4054460a9f0000000000");
        assertThat(bytes, is(ArrayUtils.reverseArray(expected)));
    }

    @Test
    public void pushInteger_plus10ToThePowerOf30() {
        builder.pushInteger(BigInteger.TEN.pow(40));
        byte[] bytes = builder.toArray();
        assertThat(bytes.length, is(33)); // PUSHINT opcode plus 32 integer bytes.
        bytes = ArrayUtils.getLastNBytes(bytes, 32); // Remove the PUSHINT opcode byte.
        // Two's complement in big-endian order.
        byte[] expected = hexStringToByteArray("0x0000000000000000000000000000001d6329f1c35ca4bfabb9f5610000000000");
        assertThat(bytes, is(ArrayUtils.reverseArray(expected)));
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
                + OpCode.SYSCALL.toString()
                + InteropService.SYSTEM_CRYPTO_CHECKMULTISIG.getHash()
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
                + OpCode.SYSCALL.toString()
                + InteropService.SYSTEM_CRYPTO_CHECKSIG.getHash()
        );
        assertArrayEquals(expected, script);
    }

    @Test
    public void testMap() {
        HashMap<ContractParameter, ContractParameter> map = new HashMap<>();
        map.put(integer(1), string("first"));
        map.put(byteArray("7365636f6e64"), bool(true));
        String possibleExpected1 = Numeric.toHexString(
                new ScriptBuilder()
                        .opCode(OpCode.NEWMAP)
                        .opCode(OpCode.DUP)
                        .pushInteger(1)
                        .pushData("first")
                        .opCode(OpCode.SETITEM)
                        .opCode(OpCode.DUP)
                        .pushData(hexStringToByteArray("7365636f6e64"))
                        .pushBoolean(true)
                        .opCode(OpCode.SETITEM)
                        .toArray()
        );

        String possibleExpected2 = Numeric.toHexString(
                new ScriptBuilder()
                        .opCode(OpCode.NEWMAP)
                        .opCode(OpCode.DUP)
                        .pushData(hexStringToByteArray("7365636f6e64"))
                        .pushBoolean(true)
                        .opCode(OpCode.SETITEM)
                        .opCode(OpCode.DUP)
                        .pushInteger(1)
                        .pushData("first")
                        .opCode(OpCode.SETITEM)
                        .toArray()
        );

        String actual = Numeric.toHexString(new ScriptBuilder().pushMap(map).toArray());

        assertThat(actual, isOneOf(possibleExpected1, possibleExpected2));
    }

    @Test
    public void testMap_nested() {
        HashMap<ContractParameter, ContractParameter> map = new HashMap<>();
        map.put(integer(1), string("first"));

        HashMap<ContractParameter, ContractParameter> nestedMap = new HashMap<>();
        nestedMap.put(integer(10), string("nestedFirst"));

        map.put(byteArray("6e6573746564"), map(nestedMap));

        String possibleExpected1 = Numeric.toHexString(
                new ScriptBuilder()
                        .opCode(OpCode.NEWMAP)
                        // first param start
                        .opCode(OpCode.DUP)
                        .pushInteger(1)
                        .pushData("first")
                        .opCode(OpCode.SETITEM)
                        .opCode(OpCode.DUP)
                        // second param start
                        .pushData("nested")
                        // second param value start
                        .opCode(OpCode.NEWMAP)
                        .opCode(OpCode.DUP)
                        .pushInteger(10)
                        .pushData("nestedFirst")
                        .opCode(OpCode.SETITEM)
                        // second param value end
                        .opCode(OpCode.SETITEM)
                        .toArray()
        );

        String possibleExpected2 = Numeric.toHexString(
                new ScriptBuilder()
                        .opCode(OpCode.NEWMAP)
                        .opCode(OpCode.DUP)
                        // first param start
                        .pushData("nested")
                        // second param value start
                        .opCode(OpCode.NEWMAP)
                        .opCode(OpCode.DUP)
                        .pushInteger(10)
                        .pushData("nestedFirst")
                        .opCode(OpCode.SETITEM)
                        // second param value end
                        .opCode(OpCode.SETITEM)
                        .opCode(OpCode.DUP)
                        .pushInteger(1)
                        .pushData("first")
                        .opCode(OpCode.SETITEM)
                        .toArray()
        );

        String actual = Numeric.toHexString(new ScriptBuilder().pushMap(map).toArray());

        assertThat(actual, isOneOf(possibleExpected1, possibleExpected2));
    }

}
