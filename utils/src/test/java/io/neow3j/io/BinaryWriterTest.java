package io.neow3j.io;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;

public class BinaryWriterTest {

    private ByteArrayOutputStream outStream;
    private BinaryWriter writer;

    @Before
    public void setUp() {
        this.outStream = new ByteArrayOutputStream();
        this.writer = new BinaryWriter(outStream);
    }

    @Test
    public void writeUInt32() throws IOException {
        Long uint = (long) Math.pow(2, 32) - 1;
        writer.writeUInt32(uint);
        writer.flush();
        byte[] bytes = outStream.toByteArray();
        assertArrayEquals(bytes, new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff});

        outStream.reset();

        uint = 0L;
        writer.writeUInt32(uint);
        writer.flush();
        bytes = outStream.toByteArray();
        assertArrayEquals(bytes, new byte[]{0, 0, 0, 0});

        outStream.reset();

        uint = 12345L;
        writer.writeUInt32(uint);
        writer.flush();
        bytes = outStream.toByteArray();
        assertArrayEquals(bytes, new byte[]{0x39, 0x30, 0, 0});
    }

    @Test
    public void failWritingUInt32OutsideOfRange() throws IOException {
        try {
            Long uint = (long) Math.pow(2, 32);
            writer.writeUInt32(uint);
            fail();
        } catch (IllegalArgumentException ignored) {}

        try {
            Long uint = -1L;
            writer.writeUInt32(uint);
            fail();
        } catch (IllegalArgumentException ignored) {}
    }

    @Test
    public void writeLong() throws IOException {
        Long l = Long.MAX_VALUE;
        writer.writeInt64(l);
        writer.flush();
        byte[] bytes = outStream.toByteArray();
        assertArrayEquals(bytes, new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0x7f});

        outStream.reset();

        l = Long.MIN_VALUE;
        writer.writeInt64(l);
        writer.flush();
        bytes = outStream.toByteArray();
        assertArrayEquals(bytes, new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x80});

        outStream.reset();

        l = 0L;
        writer.writeInt64(l);
        writer.flush();
        bytes = outStream.toByteArray();
        assertArrayEquals(bytes, new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00});

        outStream.reset();

        l = 1234567890L;
        writer.writeInt64(l);
        writer.flush();
        bytes = outStream.toByteArray();
        assertArrayEquals(bytes, new byte[]{(byte) 0xd2, (byte) 0x02, (byte) 0x96, (byte) 0x49,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00});
    }

}