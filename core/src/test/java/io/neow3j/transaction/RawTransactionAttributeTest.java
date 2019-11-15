package io.neow3j.transaction;

import io.neow3j.io.BinaryReader;
import io.neow3j.io.BinaryWriter;
import io.neow3j.model.types.TransactionAttributeUsageType;
import io.neow3j.utils.ArrayUtils;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.*;

public class RawTransactionAttributeTest {

    @Test
    public void createAttribute() {
        // positive case
        for (TransactionAttributeUsageType type : TransactionAttributeUsageType.values()) {
            if (type.fixedDataLength() != null) {
                new RawTransactionAttribute(type, createByteArray(type.fixedDataLength(), 1));
            } else if (type.maxDataLength() != null) {
                new RawTransactionAttribute(type, createByteArray(type.maxDataLength(), 1));
            } else {
                new RawTransactionAttribute(type, createByteArray(100, 1));
            }
        }

        // negative cases
        for (TransactionAttributeUsageType type : TransactionAttributeUsageType.values()) {
            try {
                if (type.fixedDataLength() != null) {
                    new RawTransactionAttribute(type, createByteArray(type.fixedDataLength() + 1, 1));
                }
                else if (type.maxDataLength() != null) {
                    new RawTransactionAttribute(type, createByteArray(type.maxDataLength() + 1, 1));
                } else {
                    continue;
                }
            } catch (IllegalArgumentException e) { continue; }
            fail();
        }

        for (TransactionAttributeUsageType type : TransactionAttributeUsageType.values()) {
            try {
                if (type.fixedDataLength() != null) {
                    new RawTransactionAttribute(type, createByteArray(type.fixedDataLength() - 1, 1));
                } else {
                    continue;
                }
            } catch (IllegalArgumentException e) { continue; }
            fail();
        }
    }

    @Test
    public void deserialize() throws IOException {
        for (TransactionAttributeUsageType type : TransactionAttributeUsageType.values()) {
            byte usage = type.byteValue();
            byte[] data;
            byte[] input;
            if (type.fixedDataLength() != null) {
                data = createByteArray(type.fixedDataLength(), 1);
                input = ArrayUtils.concatenate(usage, data);
            } else {
                byte dataLength = 64;
                data = createByteArray(dataLength, 1);
                input = ArrayUtils.concatenate(new byte[]{usage, dataLength}, data);
            }
            RawTransactionAttribute attr = new RawTransactionAttribute();
            attr.deserialize(new BinaryReader(new ByteArrayInputStream(input, 0, input.length)));
            assertEquals(type, attr.usage);
            assertEquals(data.length, attr.data.length);
            assertArrayEquals(data, attr.getDataAsBytes());
        }
    }

    @Test
    public void serialize() throws IOException {
        for (TransactionAttributeUsageType type : TransactionAttributeUsageType.values()) {
            byte[] data;
            byte dataLength = 64;
            if (type.fixedDataLength() != null) {
                 data = createByteArray(type.fixedDataLength(), 1);
            } else {
                data = createByteArray(dataLength, 1);
            }
            RawTransactionAttribute attr = new RawTransactionAttribute(type, data);

            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            attr.serialize(new BinaryWriter(byteStream));
            byte[] output = byteStream.toByteArray();
            assertEquals(type.byteValue(), output[0]);
            if (type.fixedDataLength() != null) {
                assertEquals(data.length + 1, output.length);
                assertArrayEquals(ArrayUtils.concatenate(type.byteValue(), data), output);
            } else {
                assertEquals(data.length + 2, output.length);
                assertEquals(dataLength, output[1]);
                assertArrayEquals(ArrayUtils.concatenate(new byte[]{type.byteValue(), dataLength}, data), output);
            }
        }
    }

    private static byte[] createByteArray(int length, int replicateByte) {
        byte[] array = new byte[length];
        Arrays.fill(array, (byte)replicateByte);
        return array;
    }
}