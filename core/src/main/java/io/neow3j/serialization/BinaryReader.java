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
package io.neow3j.serialization;

import io.neow3j.constants.NeoConstants;
import io.neow3j.script.OpCode;
import io.neow3j.serialization.exceptions.DeserializationException;
import io.neow3j.utils.BigIntegers;
import org.bouncycastle.math.ec.ECPoint;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * Reads a 16-bit unsigned integer in little-endian format from the underlying input stream.
     * <p>
     * Since Java does not support unsigned numeral types, the 16-bit short is represented by a int.
     *
     * @return the 16-bit unsigned integer as a normal Java int.
     * @throws IOException if an I/O exception occurs.
     */
    public int readUInt16() throws IOException {
        reader.readFully(array, 0, 2);
        position += 2;
        return Short.toUnsignedInt(buffer.getShort(0));
    }

    /**
     * Reads a 16-bit signed integer in little-endian format from the underlying input stream.
     *
     * @return the 16-bit signed integer.
     * @throws IOException if an I/O exception occurs.
     */
    public short readInt16() throws IOException {
        reader.readFully(array, 0, 2);
        position += 2;
        return buffer.getShort(0);
    }

    /**
     * Reads a 32-bit unsigned integer in little-endian format from the underlying input stream.
     * <p>
     * Since Java does not support unsigned numeral types, the unsigned integer is represented by a long.
     *
     * @return the 32-bit unsigned integer.
     * @throws IOException if an I/O exception occurs.
     */
    public long readUInt32() throws IOException {
        reader.readFully(array, 0, 4);
        position += 4;
        return Integer.toUnsignedLong(buffer.getInt(0));
    }

    /**
     * Reads a 32-bit signed integer in little-endian format from the underlying input stream.
     *
     * @return the 32-bit signed integer.
     * @throws IOException if an I/O exception occurs.
     */
    public int readInt32() throws IOException {
        reader.readFully(array, 0, 4);
        position += 4;
        return buffer.getInt(0);
    }

    /**
     * Reads a 64-bit signed integer in little-endian format from the underlying input stream.
     *
     * @return the 34-bit unsigned integer represented as a long.
     * @throws IOException if an I/O exception occurs.
     */
    public long readInt64() throws IOException {
        reader.readFully(array, 0, 8);
        position += 8;
        return buffer.getLong(0);
    }

    /**
     * Tries to read an encoded EC point from the underlying stream.
     *
     * @return the encoded EC point byte array.
     * @throws DeserializationException if an the stream does not contain an EC point or an IOException occurs.
     */
    public byte[] readEncodedECPoint() throws DeserializationException {
        byte[] ecPoint = new byte[33];
        try {
            byte encoded = reader.readByte();
            position += Byte.BYTES;
            if (encoded == 0x02 || encoded == 0x03) {
                ecPoint[0] = encoded;
                reader.readFully(ecPoint, 1, 32);
                position += 32;
                return ecPoint;
            }
        } catch (IOException e) {
            throw new DeserializationException(e);
        }
        throw new DeserializationException("Failed parsing encoded EC point.");
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
        return NeoConstants.secp256r1DomainParams().getCurve().decodePoint(encoded);
    }

    public <T extends NeoSerializable> T readSerializable(Class<T> t)
            throws DeserializationException {

        try {
            T obj = t.newInstance();
            obj.deserialize(this);
            return obj;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new DeserializationException(e);
        }
    }

    public <T extends NeoSerializable> List<T> readSerializableListVarBytes(Class<T> t)
            throws DeserializationException {

        try {
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
        } catch (IOException | IllegalAccessException | InstantiationException e) {
            throw new DeserializationException(e);
        }
    }

    public <T extends NeoSerializable> List<T> readSerializableList(Class<T> t)
            throws DeserializationException {

        try {
            int length = (int) readVarInt(0x10000000);
            List<T> list = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                T objInstance = t.newInstance();
                list.add(objInstance);
                objInstance.deserialize(this);
            }
            return list;
        } catch (IOException | IllegalAccessException | InstantiationException e) {
            throw new DeserializationException(e);
        }
    }

    public byte[] readVarBytes() throws IOException {
        return readVarBytes(0x1000000);
    }

    /**
     * Reads bytes as a UTF8 encoded string. It is assumed that the string is prefixed with its length.
     *
     * @return the read string.
     * @throws IOException if an I/O exception occurs.
     */
    public String readVarString() throws IOException {
        return new String(readVarBytes(), StandardCharsets.UTF_8);
    }

    /**
     * Tries to read a PUSHDATA OpCode and the following data from the underlying byte stream.
     *
     * @return the data read.
     * @throws DeserializationException if the sequence of data cannot be deserialized.
     */
    public byte[] readPushData() throws DeserializationException {
        try {
            byte singleByte = readByte();
            int size = 0;
            if (singleByte == OpCode.PUSHDATA1.getCode()) {
                size = readUnsignedByte();
            } else if (singleByte == OpCode.PUSHDATA2.getCode()) {
                size = readInt16();
            } else if (singleByte == OpCode.PUSHDATA4.getCode()) {
                size = readInt32();
            } else {
                throw new DeserializationException("Stream did not contain a PUSHDATA OpCode at the current position.");
            }
            // read the buffer based on the data's byte size
            if (size == 1) {
                return new byte[]{readByte()};
            }
            return readBytes(size);
        } catch (IOException e) {
            throw new DeserializationException(e);
        }
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
            value = Short.toUnsignedLong(readInt16());
        } else if (fb == 0xFE) {
            value = Integer.toUnsignedLong(readInt32());
        } else if (fb == 0xFF) {
            value = readInt64();
        } else {
            value = fb;
        }
        if (Long.compareUnsigned(value, max) > 0) {
            throw new IOException();
        }
        return value;
    }

    public String readPushString() throws DeserializationException {
        return new String(readPushData(), StandardCharsets.UTF_8);
    }

    public int readPushInteger() throws DeserializationException {
        return readPushBigInteger().intValue();
    }

    public BigInteger readPushBigInteger() throws DeserializationException {
        try {
            byte opCode = readByte();
            if (opCode >= OpCode.PUSHM1.getCode() && opCode <= OpCode.PUSH16.getCode()) {
                return BigInteger.valueOf(opCode - OpCode.PUSH0.getCode());
            } else if (opCode == OpCode.PUSHINT8.getCode()) {
                return BigIntegers.fromLittleEndianByteArray(new byte[]{readByte()});
            } else if (opCode == OpCode.PUSHINT16.getCode()) {
                return BigIntegers.fromLittleEndianByteArray(readBytes(2));
            } else if (opCode == OpCode.PUSHINT32.getCode()) {
                return BigIntegers.fromLittleEndianByteArray(readBytes(4));
            } else if (opCode == OpCode.PUSHINT64.getCode()) {
                return BigIntegers.fromLittleEndianByteArray(readBytes(8));
            } else if (opCode == OpCode.PUSHINT128.getCode()) {
                return BigIntegers.fromLittleEndianByteArray(readBytes(16));
            } else if (opCode == OpCode.PUSHINT256.getCode()) {
                return BigIntegers.fromLittleEndianByteArray(readBytes(32));
            }
        } catch (IOException e) {
            throw new DeserializationException(e);
        }
        throw new DeserializationException("Couldn't parse PUSHINT OpCode");
    }

    public int available() throws IOException {
        return reader.available();
    }

}
