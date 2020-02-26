package io.neow3j.io;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import io.neow3j.io.exceptions.DeserializationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import org.junit.Test;

public class BinaryReaderTest extends TestBinaryUtils {

    private BinaryReader testBinaryReader;
    private ByteArrayBuilder arrayBuilder;
    private byte[] readResultByteArray;
    private BigInteger readResultInt;
    private String readResultString;

    // region Read push data byte array

    @Test
    public void readPushData_ByteArray_1Byte() throws IOException {

        this.arrayBuilder = new ByteArrayBuilder()
                .setPrefix("01")
                .setAnyDataWithSize(1)
                .setSuffix("0000000000");

        readPushData_ByteArray();

        assertThat(this.readResultByteArray, is(arrayBuilder.getData()));
    }

    @Test
    public void readPushData_ByteArray_75Bytes() throws IOException {

        this.arrayBuilder = new ByteArrayBuilder()
                .setPrefix("4b")
                .setAnyDataWithSize(75)
                .setSuffix("0000000000");

        readPushData_ByteArray();

        assertThat(this.readResultByteArray, is(arrayBuilder.getData()));
    }

    @Test
    public void readPushData_ByteArray_76Bytes() throws IOException {

        this.arrayBuilder = new ByteArrayBuilder()
                .setPrefix("4c4c")
                .setAnyDataWithSize(76)
                .setSuffix("0000000000");

        readPushData_ByteArray();

        assertThat(this.readResultByteArray, is(arrayBuilder.getData()));
    }

    @Test
    public void readPushData_ByteArray_77Bytes() throws IOException {

        this.arrayBuilder = new ByteArrayBuilder()
                .setPrefix("4c4d")
                .setAnyDataWithSize(77)
                .setSuffix("0000000000");

        readPushData_ByteArray();

        assertThat(this.readResultByteArray, is(arrayBuilder.getData()));
    }

    @Test
    public void readPushData_ByteArray_255Bytes() throws IOException {

        this.arrayBuilder = new ByteArrayBuilder()
                .setPrefix("4cff")
                .setAnyDataWithSize(255)
                .setSuffix("0000000000");

        readPushData_ByteArray();

        assertThat(this.readResultByteArray, is(arrayBuilder.getData()));
    }

    @Test
    public void readPushData_ByteArray_256Bytes() throws IOException {

        this.arrayBuilder = new ByteArrayBuilder()
                .setPrefix("4D0001")
                .setAnyDataWithSize(256)
                .setSuffix("0000000000");

        readPushData_ByteArray();

        assertThat(this.readResultByteArray, is(arrayBuilder.getData()));
    }

    @Test
    public void readPushData_ByteArray_2769Bytes() throws IOException {

        this.arrayBuilder = new ByteArrayBuilder()
                .setPrefix("4DD10A")
                .setAnyDataWithSize(2769)
                .setSuffix("0000000000");

        readPushData_ByteArray();

        assertThat(this.readResultByteArray, is(arrayBuilder.getData()));
    }

    @Test
    public void readPushData_ByteArray_4096Bytes() throws IOException {

        this.arrayBuilder = new ByteArrayBuilder()
                .setPrefix("4E00100000")
                .setAnyDataWithSize(4096)
                .setSuffix("0000000000");

        readPushData_ByteArray();

        assertThat(this.readResultByteArray, is(arrayBuilder.getData()));
    }

    @Test
    public void readPushData_ByteArray_14096Bytes() throws IOException {

        this.arrayBuilder = new ByteArrayBuilder()
                .setPrefix("4E10370000")
                .setAnyDataWithSize(14096)
                .setSuffix("0000000000");

        readPushData_ByteArray();

        assertThat(this.readResultByteArray, is(arrayBuilder.getData()));
    }

    //endregion

    //region Read push data string

    @Test
    public void readPushData_String_0Bytes() throws IOException {

        this.arrayBuilder = new ByteArrayBuilder()
                .setPrefix("00");

        readPushData_String();

        assertThat(this.readResultString, is(""));
    }

    @Test
    public void readPushData_String_1Bytes() throws IOException {

        this.arrayBuilder = new ByteArrayBuilder()
                .setPrefix("0161");

        readPushData_String();

        assertThat(this.readResultString, is("a"));
    }

    @Test
    public void readPushData_String_10000Bytes() throws IOException {

        this.arrayBuilder = new ByteArrayBuilder()
                .setPrefix("4E10270000")
                .setAnyStringWithSize(10000)
                .setSuffix("F0F0F0");

        readPushData_String();

        assertThat(this.readResultString, is(new String(this.arrayBuilder.getData())));
    }

    //endregion

    //region Read push integer

    @Test
    public void readPushInteger0() throws IOException, DeserializationException {
        this.arrayBuilder = new ByteArrayBuilder().setData("10"); // PUSH0
        readPushInteger();
        assertThat(this.readResultInt, is(BigInteger.valueOf(0)));
    }

    @Test
    public void readPushInteger1() throws IOException, DeserializationException {
        this.arrayBuilder = new ByteArrayBuilder().setPrefix("11"); // PUSH1
        readPushInteger();
        assertThat(this.readResultInt, is(BigInteger.valueOf(1)));
    }

    @Test
    public void readPushIntegerMinus1() throws IOException, DeserializationException {
        this.arrayBuilder = new ByteArrayBuilder().setPrefix("0f"); // PUSHM1
        readPushInteger();
        assertThat(this.readResultInt, is(BigInteger.valueOf(-1)));
    }

    @Test
    public void readPushInteger16() throws IOException, DeserializationException {
        this.arrayBuilder = new ByteArrayBuilder().setPrefix("20"); // PUSH16
        readPushInteger();
        assertThat(this.readResultInt, is(BigInteger.valueOf(16)));
    }

    @Test(expected = DeserializationException.class)
    public void failReadPushIntegerUnsupported() throws IOException, DeserializationException {
        this.arrayBuilder = new ByteArrayBuilder().setPrefix("0e"); // Not a PUSH OpCode
        readPushInteger();
    }

    @Test
    public void readPushIntegerMin8BitInt() throws IOException, DeserializationException {
        this.arrayBuilder = new ByteArrayBuilder().setPrefix("0080");
        readPushInteger();
        assertThat(this.readResultInt, is(BigInteger.valueOf(-128)));
    }

    @Test
    public void readPushIntegerMax8BitInt() throws IOException, DeserializationException {
        this.arrayBuilder = new ByteArrayBuilder().setPrefix("007f");
        readPushInteger();
        assertThat(this.readResultInt, is(BigInteger.valueOf(127)));
    }

    @Test
    public void readPushIntegerMin16BitInt() throws IOException, DeserializationException {
        this.arrayBuilder = new ByteArrayBuilder().setPrefix("010080");
        readPushInteger();
        assertThat(this.readResultInt, is(BigInteger.valueOf(-32_768)));
    }

    @Test
    public void readPushInteger16BitInt255() throws IOException, DeserializationException {
        this.arrayBuilder = new ByteArrayBuilder().setPrefix("01ff00");
        readPushInteger();
        assertThat(this.readResultInt, is(BigInteger.valueOf(255)));
    }

    @Test
    public void readPushIntegerMax16BitInt() throws IOException, DeserializationException {
        this.arrayBuilder = new ByteArrayBuilder().setPrefix("01ff7f");
        readPushInteger();
        assertThat(this.readResultInt, is(BigInteger.valueOf(32_767)));
    }

    @Test
    public void readPushIntegerMin32BitInt() throws IOException, DeserializationException {
        this.arrayBuilder = new ByteArrayBuilder().setPrefix("0200000080");
        readPushInteger();
        assertThat(this.readResultInt, is(BigInteger.valueOf(-2_147_483_648)));
    }

    @Test
    public void readPushInteger32BitInt65535() throws IOException, DeserializationException {
        this.arrayBuilder = new ByteArrayBuilder().setPrefix("02ffff0000");
        readPushInteger();
        assertThat(this.readResultInt, is(BigInteger.valueOf(65_535)));
    }

    @Test
    public void readPushIntegerMax32BitInt() throws IOException, DeserializationException {
        this.arrayBuilder = new ByteArrayBuilder().setPrefix("02ffffff7f");
        readPushInteger();
        assertThat(this.readResultInt, is(BigInteger.valueOf(2_147_483_647)));
    }

    @Test
    public void readPushIntegerMin64BitInt() throws IOException, DeserializationException {
        this.arrayBuilder = new ByteArrayBuilder().setPrefix("030000000000000080");
        readPushInteger();
        assertThat(this.readResultInt, is(BigInteger.valueOf(-9_223_372_036_854_775_808L)));
    }

    @Test
    public void readPushInteger64BitInt65535() throws IOException, DeserializationException {
        this.arrayBuilder = new ByteArrayBuilder().setPrefix("03ffffffff00000000");
        readPushInteger();
        assertThat(this.readResultInt, is(BigInteger.valueOf(4_294_967_295L)));
    }

    @Test
    public void readPushIntegerMax64BitInt() throws IOException, DeserializationException {
        this.arrayBuilder = new ByteArrayBuilder().setPrefix("03ffffffffffffff7f");
        readPushInteger();
        assertThat(this.readResultInt, is(BigInteger.valueOf(9_223_372_036_854_775_807L)));
    }

    @Test
    public void readPushInteger128BitInt() throws IOException, DeserializationException {
        this.arrayBuilder = new ByteArrayBuilder().setPrefix("04ffffffffffffffff0000000000000000");
        readPushInteger();
        assertThat(this.readResultInt, is(new BigInteger("18446744073709551615")));
    }

    @Test
    public void readPushInteger256BitInt() throws IOException, DeserializationException {
        this.arrayBuilder = new ByteArrayBuilder().setPrefix("050100000000000000feffffffffffffff00000000000000000000000000000000");
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

    private void buildBinaryReader(byte[] data) {
        this.testBinaryReader = new BinaryReader(new ByteArrayInputStream(data));
    }

    private void readPushData_ByteArray() throws IOException {
        byte[] testArray = this.arrayBuilder.build();

        buildBinaryReader(testArray);
        this.readResultByteArray = this.testBinaryReader.readPushData();
    }

    private void readPushData_String() throws IOException {
        byte[] testArray = this.arrayBuilder.build();

        buildBinaryReader(testArray);
        this.readResultString = this.testBinaryReader.readPushString();
    }

    private void readPushInteger() throws IOException, DeserializationException {
        byte[] testArray = this.arrayBuilder.build();
        buildBinaryReader(testArray);
        this.readResultInt = this.testBinaryReader.readPushBigInteger();
    }

}
