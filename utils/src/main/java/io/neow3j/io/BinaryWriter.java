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

import io.neow3j.constants.OpCode;
import org.bouncycastle.math.ec.ECPoint;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import static io.neow3j.utils.ArrayUtils.trimLeadingZeroes;
import static java.nio.charset.StandardCharsets.UTF_8;

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

    public void writeInt(int v) throws IOException {
        buffer.putInt(0, v);
        writer.write(array, 0, 4);
    }

    public void writeLong(long v) throws IOException {
        buffer.putLong(0, v);
        writer.write(array, 0, 8);
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

    public void writeShort(short v) throws IOException {
        buffer.putShort(0, v);
        writer.write(array, 0, 2);
    }

    public void pushData(byte[] data) throws IOException {
        int dataLength = data.length;
        // if the length is in between "PUSHBYTES75" (75 decimal) and
        // "0x100" (256 decimal, excluding), then we use an additional byte to
        // tell how many bytes should be pushed to the stack.
        if (dataLength > OpCode.PUSHBYTES75.getValue() && dataLength < 0x100) {
            writer.write(OpCode.PUSHDATA1.getValue());
            writeByte((byte) dataLength);
        } else if (dataLength >= 0x100 && dataLength < 0x1000) {
            writer.write(OpCode.PUSHDATA2.getValue());
            writeShort((short) dataLength);
        } else if (dataLength >= 0x1000 && dataLength < 0x10000) {
            writer.write(OpCode.PUSHDATA4.getValue());
            writeInt(dataLength);
        } else {
            // the length can be anything (inclusive) from
            // PUSHBYTES01 (0x01, 1 decimal)
            // to
            // PUSHBYTES75 (0x4B, 75 decimal)
            writeByte((byte) dataLength);
        }
        writer.write(data);
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
            writeShort((short) v);
        } else if (v <= 0xFFFFFFFF) {
            writeByte((byte) 0xFE);
            writeInt((int) v);
        } else {
            writeByte((byte) 0xFF);
            writeLong(v);
        }
    }

    public void pushData(String v) throws IOException {
        if (v != null) {
            pushData(v.getBytes(UTF_8));
        } else {
            pushData("".getBytes());
        }
    }

    public void pushInteger(int v) throws IOException {
        pushInteger(BigInteger.valueOf(v));
    }

    public void pushInteger(BigInteger v) throws IOException {
        if (v.intValue() == -1) {
            writeByte(OpCode.PUSHM1.getValue());
        } else if (v.intValue() == 0) {
            writeByte(OpCode.PUSH0.getValue());
        } else if (v.intValue() > 0 && v.intValue() <= 16) {
            int base = (OpCode.PUSH1.getValue() - (byte) 0x01);
            writeByte((byte) (base + v.intValue()));
        } else {
            // if the value is larger than the PUSH16 opcode,
            // then push as data array...
            pushData(trimLeadingZeroes(v.toByteArray()));
        }
    }

    public void writeEmitSysCall(String operation) throws IOException {
        writeByte(OpCode.SYSCALL.getValue());
        pushData(operation);
    }
}
