/*
 * MIT License
 * <p>
 * Copyright (c) 2018 AlienWorks
 * Copyright (c) 2018 AxLabs GmbH (https://axlabs.com)
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.neow3j.io;

import io.neow3j.constants.NeoConstants;
import io.neow3j.constants.OpCode;
import io.neow3j.utils.BigIntegers;
import org.bouncycastle.math.ec.ECPoint;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import static io.neow3j.utils.Numeric.toBigInt;

public class BinaryReader implements AutoCloseable {

    private DataInputStream reader;
    private byte[] array = new byte[8];
    private ByteBuffer buffer = ByteBuffer.wrap(array).order(ByteOrder.LITTLE_ENDIAN);
    private int position = 0;
    private int mark = -1;

    public BinaryReader(InputStream stream) {
        this.reader = new DataInputStream(stream);
    }

    public BinaryReader(byte[] input) {
        this(new ByteArrayInputStream(input, 0, input.length));
    }

    public int getPosition() {
        return position;
    }

    public int getMark() {
        return mark;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    public void mark(int readlimit) {
        reader.mark(readlimit);
        mark = getPosition();
    }

    public void reset() throws IOException {
        reader.reset();
        position = getMark();
    }


    public void read(byte[] buffer) throws IOException {
        reader.readFully(buffer);
        position += buffer.length;
    }

    public void read(byte[] buffer, int index, int length) throws IOException {
        reader.readFully(buffer, index, length);
        position += length;
    }

    public boolean readBoolean() throws IOException {
        boolean result = reader.readBoolean();
        position += Byte.BYTES;
        return result;
    }

    public int readUnsignedByte() throws IOException {
        int result = reader.readUnsignedByte();
        position += Byte.BYTES;
        return result;
    }

    public byte readByteKeepPosition() throws IOException {
        byte result = reader.readByte();
        return result;
    }

    public byte readByte() throws IOException {
        byte result = reader.readByte();
        position += Byte.BYTES;
        return result;
    }

    public byte[] readBytes(int count) throws IOException {
        byte[] buffer = new byte[count];
        reader.readFully(buffer);
        position += buffer.length;
        return buffer;
    }

    public double readDouble() throws IOException {
        reader.readFully(array, 0, 8);
        position += 8;
        return buffer.getDouble(0);
    }

    public ECPoint readECPoint() throws IOException {
        // based on: https://tools.ietf.org/html/rfc5480#section-2.2
        byte[] encoded;
        byte fb = reader.readByte();
        position += Byte.BYTES;
        switch (fb) {
            case 0x00:
                encoded = new byte[1];
                break;
            case 0x02:
            case 0x03:
                encoded = new byte[33];
                encoded[0] = fb;
                reader.readFully(encoded, 1, 32);
                position += 32;
                break;
            case 0x04:
                encoded = new byte[65];
                encoded[0] = fb;
                reader.readFully(encoded, 1, 64);
                position += 64;
                break;
            default:
                throw new IOException();
        }
        return NeoConstants.CURVE.getCurve().decodePoint(encoded);
    }

    public float readFloat() throws IOException {
        reader.readFully(array, 0, 4);
        position += 4;
        return buffer.getFloat(0);
    }

    public int readInt() throws IOException {
        reader.readFully(array, 0, 4);
        position += 4;
        return buffer.getInt(0);
    }

    public long readLong() throws IOException {
        reader.readFully(array, 0, 8);
        position += 8;
        return buffer.getLong(0);
    }

    public <T extends NeoSerializable> T readSerializable(Class<T> t) throws InstantiationException, IllegalAccessException, IOException {
        T obj = t.newInstance();
        obj.deserialize(this);
        return obj;
    }

    public <T extends NeoSerializable> List<T> readSerializableListVarBytes(Class<T> t) throws IOException, IllegalAccessException, InstantiationException {
        int length = (int) readVarInt(0x10000000);
        int bytesRead = 0;
        int initialOffset = getPosition();
        List<T> list = new ArrayList<>();
        while (bytesRead < length) {
            T objInstance = t.newInstance();
            list.add(objInstance);
            objInstance.deserialize(this);
            int currentOffset = getPosition();
            bytesRead = (currentOffset - initialOffset);
        }
        return list;
    }

    public <T extends NeoSerializable> List<T> readSerializableList(Class<T> t) throws IOException, IllegalAccessException, InstantiationException {
        int length = (int) readVarInt(0x10000000);
        List<T> list = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            T objInstance = t.newInstance();
            list.add(objInstance);
            objInstance.deserialize(this);
        }
        return list;
    }

    public short readShort() throws IOException {
        reader.readFully(array, 0, 2);
        position += 2;
        return buffer.getShort(0);
    }

    public byte[] readVarBytes() throws IOException {
        return readVarBytes(0x7fffffc7);
    }

    public byte[] readPushData() throws IOException {
        byte singleByte = readByte();
        int size = 0;
        if (singleByte == OpCode.PUSHDATA1.getValue()) {
            // read the next byte (as the data size)
            size = readUnsignedByte();
        } else if (singleByte == OpCode.PUSHDATA2.getValue()) {
            // read the next 2 bytes (as the data size)
            // short is 2 bytes
            size = readShort();
        } else if (singleByte == OpCode.PUSHDATA4.getValue()) {
            // read the next 4 bytes (as the data size)
            // int is 4 bytes
            size = readInt();
        } else {
            // the singleByte is actually the data size already
            size = singleByte;
        }
        // read the buffer based on the data size
        if (size == 1) {
            return new byte[]{readByte()};
        }
        return readBytes(size);
    }

    public byte[] readVarBytes(int max) throws IOException {
        return readBytes((int) readVarInt(max));
    }

    public long readVarInt() throws IOException {
        return readVarInt(Long.MAX_VALUE);
    }

    public long readVarInt(long max) throws IOException {
        long fb = Byte.toUnsignedLong(readByte());
        long value;
        if (fb == 0xFD) {
            value = Short.toUnsignedLong(readShort());
        } else if (fb == 0xFE) {
            value = Integer.toUnsignedLong(readInt());
        } else if (fb == 0xFF) {
            value = readLong();
        } else {
            value = fb;
        }
        if (Long.compareUnsigned(value, max) > 0) {
            throw new IOException();
        }
        return value;
    }

    public String readPushString() throws IOException {
        return new String(readPushData(), "UTF-8");
    }

    public int readPushInteger() throws IOException {
        return readPushBigInteger().intValue();
    }

    public BigInteger readPushBigInteger() throws IOException {
        mark(2);
        byte read = readByte();
        if (read == OpCode.PUSHM1.getValue()) {
            return BigInteger.ONE.negate();
        } else if (read == OpCode.PUSH0.getValue()) {
            return BigInteger.ZERO;
        } else if (read >= OpCode.PUSH1.getValue() && read <= OpCode.PUSH16.getValue()) {
            int base = (OpCode.PUSH1.getValue() - (byte) 0x01);
            return BigInteger.valueOf(read - base);
        } else {
            // If the value is larger than the PUSH16 opcode then read as data array.
            // The same byte that has just been read needs to be read again in pushData(), so we
            // reset the stream to the mark.
            reset();
            return BigIntegers.fromLittleEndianByteArray(readPushData());
        }
    }

}
