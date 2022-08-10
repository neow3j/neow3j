package io.neow3j.serialization;

import io.neow3j.utils.ArrayUtils;
import io.neow3j.utils.Numeric;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BinaryWriterTest {

    private ByteArrayOutputStream outStream;
    private BinaryWriter writer;

    @BeforeAll
    public void setUp() {
        this.outStream = new ByteArrayOutputStream();
        this.writer = new BinaryWriter(outStream);
    }

    @Test
    public void writeUInt32() throws IOException {
        long uint = (long) Math.pow(2, 32) - 1;
        writer.writeUInt32(uint);
        assertAndResetStreamContents(
            new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff});

        uint = 0L;
        writer.writeUInt32(uint);
        assertAndResetStreamContents(new byte[]{0, 0, 0, 0});

        uint = 12345L;
        writer.writeUInt32(uint);
        assertAndResetStreamContents(new byte[]{0x39, 0x30, 0, 0});
    }

    @Test
    public void failWritingUInt32NotInRange() {
        long uint = (long) Math.pow(2, 32);
        assertThrows(IllegalArgumentException.class, () -> writer.writeUInt32(uint));

        long negativeUint = -1L;
        assertThrows(IllegalArgumentException.class, () -> writer.writeUInt32(negativeUint));
    }

    @Test
    public void writeInt64() throws IOException {
        long l = Long.MAX_VALUE;
        writer.writeInt64(l);
        assertAndResetStreamContents(new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0x7f});

        l = Long.MIN_VALUE;
        writer.writeInt64(l);
        assertAndResetStreamContents(new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x80});

        l = 0L;
        writer.writeInt64(l);
        assertAndResetStreamContents(new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00});

        l = 1234567890L;
        writer.writeInt64(l);
        assertAndResetStreamContents(new byte[]{(byte) 0xd2, (byte) 0x02, (byte) 0x96, (byte) 0x49,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00});
    }

    @Test
    public void writeUInt16() throws IOException {
        int uint = (int) Math.pow(2, 16) - 1;
        writer.writeUInt16(uint);
        assertAndResetStreamContents(new byte[]{(byte) 0xff, (byte) 0xff});

        uint = 0;
        writer.writeUInt16(uint);
        assertAndResetStreamContents(new byte[]{0, 0});

        uint = 12345;
        writer.writeUInt16(uint);
        assertAndResetStreamContents(new byte[]{0x39, 0x30});
    }

    @Test
    public void failWritingUInt16NotInRange() {
        int uint = (int) Math.pow(2, 16);
        assertThrows(IllegalArgumentException.class, () -> writer.writeUInt16(uint));

        int negativeUint = -1;
        assertThrows(IllegalArgumentException.class, () -> writer.writeUInt16(negativeUint));
    }

    @Test
    public void writeVarInt() throws IOException {
        // v == 0, encode with one byte
        writer.writeVarInt(0L);
        assertAndResetStreamContents(new byte[]{(byte) 0x00});

        // v == 0xfd - 1, encode with one byte
        writer.writeVarInt(252);
        assertAndResetStreamContents(new byte[]{(byte) 0xfc});

        // v == 0xfd, encode with uint16
        writer.writeVarInt(253);
        assertAndResetStreamContents(new byte[]{(byte) 0xfd, (byte) 0xfd, (byte) 0x00});

        // v == 0xfd + 1, encode with uint16
        writer.writeVarInt(254);
        assertAndResetStreamContents(new byte[]{(byte) 0xfd, (byte) 0xfe, (byte) 0x00});

        // v == 0xffff - 1, encode with uint16
        writer.writeVarInt(65_534);
        assertAndResetStreamContents(new byte[]{(byte) 0xfd, (byte) 0xfe, (byte) 0xff});

        // v == 0xffff, encode with uint16
        writer.writeVarInt(65_535);
        assertAndResetStreamContents(new byte[]{(byte) 0xfd, (byte) 0xff, (byte) 0xff});

        // v == 0xffff + 1, encode with uint32
        writer.writeVarInt(65_536);
        assertAndResetStreamContents(new byte[]{(byte) 0xfe, (byte) 0x00, (byte) 0x00, (byte) 0x01,
            (byte) 0x00});

        // v == 0xffffffff - 1, encode with uint32
        writer.writeVarInt(4_294_967_294L);
        assertAndResetStreamContents(new byte[]{(byte) 0xfe, (byte) 0xfe, (byte) 0xff, (byte) 0xff,
            (byte) 0xff});

        // v == 0xffffffff, encode with uint32
        writer.writeVarInt(4_294_967_295L);
        assertAndResetStreamContents(new byte[]{(byte) 0xfe, (byte) 0xff, (byte) 0xff, (byte) 0xff,
            (byte) 0xff});

        // v == 0xffffffff + 1, encode with uint64
        writer.writeVarInt(4_294_967_296L);
        assertAndResetStreamContents(new byte[]{(byte) 0xff, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00,
            0x00, 0x00});
    }

    @Test
    public void failWriteVarInt() {
        assertThrows(IllegalArgumentException.class, () -> writer.writeVarInt(-1L));
    }

    private void assertAndResetStreamContents(byte[] expected) throws IOException {
        writer.flush();
        byte[] actual = outStream.toByteArray();
        assertArrayEquals(expected, actual);
        outStream.reset();
    }

    @Test
    public void writeVarBytes() throws IOException {
        // Short byte array
        writer.writeVarBytes(Numeric.hexStringToByteArray("010203"));
        assertAndResetStreamContents(Numeric.hexStringToByteArray("03" + "010203"));

        // Longer byte array
        String hex =
                "00102030102030102030102030102030102030102030102030102030102030102031020301020301020301020301020301020301020301020301020301020301020310203010203010203010203010203010203010203010203010203010203010203102030102030102030102030102030102030102030102030102030102030102030010203010203010203010203010203010203010203010203010203010203010203102030102030102030102030102030102030102030102030102030102030102031020301020301020301020301020301020301020301020301020301020301020310203010203010203010203010203010203010203010203010203010203010203";
        writer.writeVarBytes(Numeric.hexStringToByteArray(hex));
        assertAndResetStreamContents(Numeric.hexStringToByteArray("fd" + "0601" + hex));

        // Not tested for arrays longer than 65'535 (0xffff) bytes or even longer than 4'294'967'295
        // (0xFFFFFFFF) bytes. But that is covered by the test `writeVarInt`.
    }

    @Test
    public void writeVarString() throws IOException {
        // Short String
        writer.writeVarString("hello, world!");
        assertAndResetStreamContents(Numeric.hexStringToByteArray("0d" + "68656c6c6f2c20776f726c6421"));

        // Longer byte array
        String string =
                "hello, world!hello, world!hello, world!hello, world!hello, world!hello, world!hello, world!hello, world!hello, world!hello, world!hello, world!hello, world!hello, world!hello, world!hello, world!hello, world!hello, world!hello, world!hello, world!hello, world!hello, world!hello, world!hello, world!hello, world!hello, world!hello, world!hello, world!hello, world!hello, world!hello, world!hello, world!hello, world!hello, world!hello, world!hello, world!hello, world!hello, world!hello, world!hello, world!hello, world!hello, world!";
        writer.writeVarString(string);
        byte[] expected = ArrayUtils.concatenate(Numeric.hexStringToByteArray("fd" + "1502"),
                string.getBytes(UTF_8));
        assertAndResetStreamContents(expected);

        // Not tested for strings longer than 65'535 (0xffff) bytes or even longer than
        // 4'294'967'295 (0xFFFFFFFF) bytes. But that is covered by the test `writeVarInt`.
    }

}