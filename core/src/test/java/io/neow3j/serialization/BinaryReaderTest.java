package io.neow3j.serialization;

import io.neow3j.serialization.exceptions.DeserializationException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BinaryReaderTest extends TestBinaryUtils {

    private BinaryReader testBinaryReader;
    private ByteArrayBuilder arrayBuilder;
    private byte[] readResultByteArray;
    private BigInteger readResultInt;
    private String readResultString;

    // region Read push data byte array

    @Test
    public void failReadPushDataByteArray() {
        // Uses a prefix different from any of the PUSHDATA OpCodes and should therefore fail.
        this.arrayBuilder = new ByteArrayBuilder()
                .setPrefix("4b")
                .setAnyDataWithSize(1)
                .setSuffix("0000");

        DeserializationException thrown = assertThrows(DeserializationException.class, this::readPushDataByteArray);
        assertThat(thrown.getMessage(), is("Stream did not contain a PUSHDATA OpCode at the current position."));
    }

    @Test
    public void readPushDataByteArray1Byte() throws DeserializationException {
        this.arrayBuilder = new ByteArrayBuilder()
                .setPrefix("0c01")
                .setAnyDataWithSize(1);
        readPushDataByteArray();
        assertThat(this.readResultByteArray, is(this.arrayBuilder.getData()));
    }

    @Test
    public void readPushDataByteArray255Bytes() throws DeserializationException {
        this.arrayBuilder = new ByteArrayBuilder()
                .setPrefix("0cff")
                .setAnyDataWithSize(255);
        readPushDataByteArray();
        assertThat(this.readResultByteArray, is(this.arrayBuilder.getData()));
    }

    @Test
    public void readPushDataByteArray256Bytes() throws DeserializationException {
        this.arrayBuilder = new ByteArrayBuilder()
                .setPrefix("0d0001")
                .setAnyDataWithSize(256);
        readPushDataByteArray();
        assertThat(this.readResultByteArray, is(this.arrayBuilder.getData()));
    }

    @Test
    public void readPushDataByteArray4096Bytes() throws DeserializationException {
        this.arrayBuilder = new ByteArrayBuilder()
                .setPrefix("0d0010")
                .setAnyDataWithSize(4096);
        readPushDataByteArray();
        assertThat(this.readResultByteArray, is(this.arrayBuilder.getData()));
    }

    @Test
    public void readPushDataByteArray65536Bytes() throws DeserializationException {
        this.arrayBuilder = new ByteArrayBuilder()
                .setPrefix("0e00000100")
                .setAnyDataWithSize(65536);
        readPushDataByteArray();
        assertThat(this.readResultByteArray, is(this.arrayBuilder.getData()));
    }

    //endregion

    //region Read push data string

    @Test
    public void readPushDataString0Bytes() throws DeserializationException {
        this.arrayBuilder = new ByteArrayBuilder()
                .setPrefix("0c00");
        readPushDataString();
        assertThat(this.readResultString, is(""));
    }

    @Test
    public void readPushDataString1Byte() throws DeserializationException {
        this.arrayBuilder = new ByteArrayBuilder()
                .setPrefix("0c0161");
        readPushDataString();
        assertThat(this.readResultString, is("a"));
    }

    @Test
    public void readPushDataString10000Bytes() throws DeserializationException {
        this.arrayBuilder = new ByteArrayBuilder()
                .setPrefix("0e10270000")
                .setAnyStringWithSize(10000);
        readPushDataString();
        assertThat(this.readResultString, is(new String(this.arrayBuilder.getData())));
    }

    //endregion

    //region Read push integer

    @Test
    public void readPushInteger0() throws DeserializationException {
        this.arrayBuilder = new ByteArrayBuilder().setData("10"); // PUSH0
        readPushInteger();
        assertThat(this.readResultInt, is(BigInteger.valueOf(0)));
    }

    @Test
    public void readPushInteger1() throws DeserializationException {
        this.arrayBuilder = new ByteArrayBuilder().setPrefix("11"); // PUSH1
        readPushInteger();
        assertThat(this.readResultInt, is(BigInteger.valueOf(1)));
    }

    @Test
    public void readPushIntegerMinus1() throws DeserializationException {
        this.arrayBuilder = new ByteArrayBuilder().setPrefix("0f"); // PUSHM1
        readPushInteger();
        assertThat(this.readResultInt, is(BigInteger.valueOf(-1)));
    }

    @Test
    public void readPushInteger16() throws DeserializationException {
        this.arrayBuilder = new ByteArrayBuilder().setPrefix("20"); // PUSH16
        readPushInteger();
        assertThat(this.readResultInt, is(BigInteger.valueOf(16)));
    }

    @Test
    public void failReadPushIntegerUnsupported() {
        this.arrayBuilder = new ByteArrayBuilder().setPrefix("0e"); // Not a PUSH OpCode
        DeserializationException thrown = assertThrows(DeserializationException.class, this::readPushInteger);
        assertThat(thrown.getMessage(), is("Couldn't parse PUSHINT OpCode"));
    }

    @Test
    public void readPushIntegerMin8BitInt() throws DeserializationException {
        this.arrayBuilder = new ByteArrayBuilder().setPrefix("0080");
        readPushInteger();
        assertThat(this.readResultInt, is(BigInteger.valueOf(-128)));
    }

    @Test
    public void readPushIntegerMax8BitInt() throws DeserializationException {
        this.arrayBuilder = new ByteArrayBuilder().setPrefix("007f");
        readPushInteger();
        assertThat(this.readResultInt, is(BigInteger.valueOf(127)));
    }

    @Test
    public void readPushIntegerMin16BitInt() throws DeserializationException {
        this.arrayBuilder = new ByteArrayBuilder().setPrefix("010080");
        readPushInteger();
        assertThat(this.readResultInt, is(BigInteger.valueOf(-32_768)));
    }

    @Test
    public void readPushInteger16BitInt255() throws DeserializationException {
        this.arrayBuilder = new ByteArrayBuilder().setPrefix("01ff00");
        readPushInteger();
        assertThat(this.readResultInt, is(BigInteger.valueOf(255)));
    }

    @Test
    public void readPushIntegerMax16BitInt() throws DeserializationException {
        this.arrayBuilder = new ByteArrayBuilder().setPrefix("01ff7f");
        readPushInteger();
        assertThat(this.readResultInt, is(BigInteger.valueOf(32_767)));
    }

    @Test
    public void readPushIntegerMin32BitInt() throws DeserializationException {
        this.arrayBuilder = new ByteArrayBuilder().setPrefix("0200000080");
        readPushInteger();
        assertThat(this.readResultInt, is(BigInteger.valueOf(-2_147_483_648)));
    }

    @Test
    public void readPushInteger32BitInt65535() throws DeserializationException {
        this.arrayBuilder = new ByteArrayBuilder().setPrefix("02ffff0000");
        readPushInteger();
        assertThat(this.readResultInt, is(BigInteger.valueOf(65_535)));
    }

    @Test
    public void readPushIntegerMax32BitInt() throws DeserializationException {
        this.arrayBuilder = new ByteArrayBuilder().setPrefix("02ffffff7f");
        readPushInteger();
        assertThat(this.readResultInt, is(BigInteger.valueOf(2_147_483_647)));
    }

    @Test
    public void readPushIntegerMin64BitInt() throws DeserializationException {
        this.arrayBuilder = new ByteArrayBuilder().setPrefix("030000000000000080");
        readPushInteger();
        assertThat(this.readResultInt, is(BigInteger.valueOf(-9_223_372_036_854_775_808L)));
    }

    @Test
    public void readPushInteger64BitInt65535() throws DeserializationException {
        this.arrayBuilder = new ByteArrayBuilder().setPrefix("03ffffffff00000000");
        readPushInteger();
        assertThat(this.readResultInt, is(BigInteger.valueOf(4_294_967_295L)));
    }

    @Test
    public void readPushIntegerMax64BitInt() throws DeserializationException {
        this.arrayBuilder = new ByteArrayBuilder().setPrefix("03ffffffffffffff7f");
        readPushInteger();
        assertThat(this.readResultInt, is(BigInteger.valueOf(9_223_372_036_854_775_807L)));
    }

    @Test
    public void readPushInteger128BitInt() throws DeserializationException {
        this.arrayBuilder = new ByteArrayBuilder().setPrefix("04ffffffffffffffff0000000000000000");
        readPushInteger();
        assertThat(this.readResultInt, is(new BigInteger("18446744073709551615")));
    }

    @Test
    public void readPushInteger256BitInt() throws DeserializationException {
        this.arrayBuilder = new ByteArrayBuilder()
                .setPrefix("050100000000000000feffffffffffffff00000000000000000000000000000000");
        readPushInteger();
        BigInteger uLong = new BigInteger("18446744073709551615");
        BigInteger val = uLong.multiply(uLong);
        assertThat(this.readResultInt, is(val));
    }

    //endregion

    @Test
    public void readUInt32() throws IOException {
        // Max value
        byte[] data = new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
        buildBinaryReader(data);
        long value = this.testBinaryReader.readUInt32();
        assertThat(value, is(4_294_967_295L));

        // Value 1
        data = new byte[]{(byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        buildBinaryReader(data);
        value = this.testBinaryReader.readUInt32();
        assertThat(value, is(1L));

        // Min value
        data = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        buildBinaryReader(data);
        value = this.testBinaryReader.readUInt32();
        assertThat(value, is(0L));

        // Any value with longer input than needed. Last 0xff should be ignored.
        data = new byte[]{(byte) 0x8c, (byte) 0xae, (byte) 0x00, (byte) 0x00, (byte) 0xff};
        buildBinaryReader(data);
        value = this.testBinaryReader.readUInt32();
        assertThat(value, is(44_684L));
    }

    @Test
    public void readInt64() throws IOException {
        // Min value (-2^63)
        byte[] data = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x80};
        buildBinaryReader(data);
        long value = this.testBinaryReader.readInt64();
        assertThat(value, is(Long.MIN_VALUE));

        // Max value (2^63-1)
        data = new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                (byte) 0xff, (byte) 0xff, (byte) 0x7f};
        buildBinaryReader(data);
        value = this.testBinaryReader.readInt64();
        assertThat(value, is(Long.MAX_VALUE));

        // Zero value
        data = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00};
        buildBinaryReader(data);
        value = this.testBinaryReader.readInt64();
        assertThat(value, is(0L));

        // Any value with longer input than needed. Last 0xff should be ignored.
        data = new byte[]{(byte) 0x11, (byte) 0x33, (byte) 0x22, (byte) 0x8c, (byte) 0xae,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xff};
        buildBinaryReader(data);
        value = this.testBinaryReader.readInt64();
        assertThat(value, is(749_675_361_041L));
    }

    @Test
    public void availableIsNonZero() throws IOException {
        byte[] data = new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                (byte) 0xff, (byte) 0xff, (byte) 0x7f};
        buildBinaryReader(data);
        assertThat(testBinaryReader.available(), is(greaterThan(0)));
    }

    private void buildBinaryReader(byte[] data) {
        this.testBinaryReader = new BinaryReader(new ByteArrayInputStream(data));
    }

    private void readPushDataByteArray() throws DeserializationException {
        byte[] testArray = this.arrayBuilder.build();

        buildBinaryReader(testArray);
        this.readResultByteArray = this.testBinaryReader.readPushData();
    }

    private void readPushDataString() throws DeserializationException {
        byte[] testArray = this.arrayBuilder.build();

        buildBinaryReader(testArray);
        this.readResultString = this.testBinaryReader.readPushString();
    }

    private void readPushInteger() throws DeserializationException {
        byte[] testArray = this.arrayBuilder.build();
        buildBinaryReader(testArray);
        this.readResultInt = this.testBinaryReader.readPushBigInteger();
    }

}
