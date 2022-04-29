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

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import org.bouncycastle.math.ec.ECPoint;

public class BinaryWriter implements AutoCloseable {

    private DataOutputStream writer;
    private byte[] array = new byte[8];
    private ByteBuffer buffer = ByteBuffer.wrap(array).order(ByteOrder.LITTLE_ENDIAN);

    public BinaryWriter(OutputStream stream) {
        this.writer = new DataOutputStream(stream);
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }

    public void flush() throws IOException {
        writer.flush();
    }

    public void write(byte[] buffer) throws IOException {
        writer.write(buffer);
    }

    public void write(byte[] buffer, int index, int length) throws IOException {
        writer.write(buffer, index, length);
    }

    public void writeBoolean(boolean v) throws IOException {
        writer.writeBoolean(v);
    }

    public void writeByte(byte v) throws IOException {
        writer.writeByte(v);
    }

    public void writeDouble(double v) throws IOException {
        buffer.putDouble(0, v);
        writer.write(array, 0, 8);
    }

    public void writeECPoint(ECPoint v) throws IOException {
        writer.write(v.getEncoded(true));
    }

    public void writeFixedString(String v, int length) throws IOException {
        if (v == null) {
            throw new IllegalArgumentException();
        }
        if (v.length() > length) {
            throw new IllegalArgumentException();
        }
        byte[] bytes = v.getBytes(UTF_8);
        if (bytes.length > length) {
            throw new IllegalArgumentException();
        }
        writer.write(bytes);
        if (bytes.length < length) {
            writer.write(new byte[length - bytes.length]);
        }
    }

    public void writeFloat(float v) throws IOException {
        buffer.putFloat(0, v);
        writer.write(array, 0, 4);
    }

    public void writeInt32(int v) throws IOException {
        buffer.putInt(0, v);
        writer.write(array, 0, 4);
    }

    /**
     * Writes the given long (signed 64-bit integer) to the underlying output stream in little-endian order. The
     * long's byte representation is its two's complement.
     *
     * @param v the value.
     * @throws IOException if an I/O exception occurs.
     */
    public void writeInt64(long v) throws IOException {
        buffer.putLong(0, v);
        writer.write(array, 0, 8);
    }

    /**
     * Writes the first (least-significant) 32 bits of the given long (signed 64-bit integer) to the underlying
     * output stream in little-endian order. I.e. the byte output represents an unsigned 32-bit integer in the range
     * [0, 2^32).
     *
     * @param v the value which needs to be in the range [0, 2^32).
     * @throws IOException              if an I/O exception occurs.
     * @throws IllegalArgumentException if the arguments value does not lie in the interval [0, 2^32).
     */
    public void writeUInt32(long v) throws IOException {
        if (v < 0 || v >= (long) Math.pow(2, 32)) {
            throw new IllegalArgumentException("Value of 32-bit unsigned integer was not in interval [0, 2^32).");
        }
        buffer.putLong(0, v);
        writer.write(array, 0, 4);
    }

    public void writeSerializableVariableBytes(NeoSerializable v) throws IOException {
        writeVarInt(v.toArray().length);
        v.serialize(this);
    }

    public void writeSerializableVariable(List<? extends NeoSerializable> v) throws IOException {
        writeVarInt(v.size());
        writeSerializableFixed(v);
    }

    public void writeSerializableVariableBytes(List<? extends NeoSerializable> v) throws IOException {
        int sumLength = 0;
        for (int i = 0; i < v.size(); i++) {
            sumLength += v.get(i).toArray().length;
        }
        writeVarInt(sumLength);
        writeSerializableFixed(v);
    }

    public void writeSerializableFixed(NeoSerializable v) throws IOException {
        v.serialize(this);
    }

    public void writeSerializableFixed(List<? extends NeoSerializable> v) throws IOException {
        for (int i = 0; i < v.size(); i++) {
            v.get(i).serialize(this);
        }
    }

    /**
     * Writes the first (least-significant) 16 bits of the given int (signed 32-bit integer) to the underlying output
     * stream in little-endian order. I.e. the byte output represents an unsigned 16-bit integer in the range [0, 2^16).
     *
     * @param v the value which needs to be in the range [0, 2^16).
     * @throws IOException              if an I/O exception occurs.
     * @throws IllegalArgumentException if the arguments value does not lie in the interval [0, 2^16).
     */
    public void writeUInt16(int v) throws IOException {
        if (v < 0 || v >= (int) Math.pow(2, 16)) {
            throw new IllegalArgumentException("Value of 16-bit unsigned integer was not in interval [0, 2^16).");
        }
        buffer.putInt(0, v);
        writer.write(array, 0, 2);
    }

    public void writeVarBytes(byte[] v) throws IOException {
        writeVarInt(v.length);
        writer.write(v);
    }

    public void writeVarInt(long v) throws IOException {
        if (v < 0) {
            throw new IllegalArgumentException();
        }
        if (v < 0xFD) {
            writeByte((byte) v);
        } else if (v <= 0xFFFF) {
            writeByte((byte) 0xFD);
            writeUInt16((int) v);
        } else if (v <= 0xFFFFFFFFL) {
            writeByte((byte) 0xFE);
            writeUInt32(v);
        } else {
            writeByte((byte) 0xFF);
            writeInt64(v);
        }
    }

    /**
     * Writes the given variable-sized string as a UTF8-encoded byte array. The array is prefixed with its size.
     *
     * @param value the string to write.
     * @throws IOException if an I/O exception occurs.
     */
    public void writeVarString(String value) throws IOException {
        writeVarBytes(value.getBytes(UTF_8));
    }

}
