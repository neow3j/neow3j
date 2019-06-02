package io.neow3j.io;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class BinaryReaderTest extends TestBinaryUtils {

    private ByteArrayInputStream is;
    private BinaryReader testBinaryReader;
    private ByteArrayBuilder arrayBuilder;
    private byte[] readResultByteArray;
    private BigInteger readResultBigInt;
    private int readResultInt;
    private String readResultString;

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

    @Test
    public void readPushInteger_0() throws IOException {

        this.arrayBuilder = new ByteArrayBuilder().setPrefix("00");

        readPushInteger();

        assertThat(this.readResultInt, is(0));
    }

    @Test
    public void readPushInteger_1() throws IOException {

        this.arrayBuilder = new ByteArrayBuilder().setPrefix("51");

        readPushInteger();

        assertThat(this.readResultInt, is(1));
    }

    @Test
    public void readPushInteger_Minus() throws IOException {

        this.arrayBuilder = new ByteArrayBuilder().setPrefix("4F");

        readPushInteger();

        assertThat(this.readResultInt, is(-1));
    }

    @Test
    public void readPushInteger_16() throws IOException {

        this.arrayBuilder = new ByteArrayBuilder().setPrefix("60");

        readPushInteger();

        assertThat(this.readResultInt, is(16));
    }

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

    private void buildBinaryReader(byte[] data) {
        this.is = new ByteArrayInputStream(data);
        this.testBinaryReader = new BinaryReader(is);
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

    private void readPushInteger() throws IOException {
        byte[] testArray = this.arrayBuilder.build();

        buildBinaryReader(testArray);
        this.readResultInt = this.testBinaryReader.readPushInteger();
    }


}
