package io.neow3j.transaction;

import io.neow3j.io.BinaryReader;
import io.neow3j.io.BinaryWriter;
import io.neow3j.io.NeoSerializable;
import io.neow3j.model.types.TransactionAttributeUsageType;
import io.neow3j.utils.Numeric;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public class RawTransactionAttribute extends NeoSerializable {

    public TransactionAttributeUsageType usage;

    public byte[] data;

    public RawTransactionAttribute() {
    }

    public RawTransactionAttribute(TransactionAttributeUsageType usage, byte[] data) {
        this.usage = usage;
        this.data = data;
        if (usage.fixedDataLength() != null && data.length != usage.fixedDataLength()) {
            throw new IllegalArgumentException("The data has different length than the length " +
                    "required by the attribute usage type.");
        }
        if (usage.maxDataLength() != null && data.length > usage.maxDataLength()) {
            throw new IllegalArgumentException("The data is longer then the maximum length " +
                    "allowed by the attribute usage type.");
        }
    }

    public RawTransactionAttribute(TransactionAttributeUsageType usage, String data) {
        this(usage, (data != null ? Numeric.hexStringToByteArray(data) : null));
    }

    public TransactionAttributeUsageType getUsage() {
        return usage;
    }

    public byte[] getDataAsBytes() {
        return this.data;
    }

    public String getData() {
        return this.data != null ? Numeric.toHexString(data) : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RawTransactionAttribute)) return false;
        RawTransactionAttribute that = (RawTransactionAttribute) o;
        return getUsage() == that.getUsage() &&
                Arrays.equals(getDataAsBytes(), that.getDataAsBytes());
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(getUsage());
        result = 31 * result + Arrays.hashCode(getDataAsBytes());
        return result;
    }

    @Override
    public String toString() {
        return "TransactionAttribute{" +
                "usage=" + usage +
                ", data=" + (data != null ? Numeric.toHexStringNoPrefix(data) : "null") +
                '}';
    }

    @Override
    public void deserialize(BinaryReader reader) throws IOException {
        this.usage = TransactionAttributeUsageType.valueOf(reader.readByte());
        if (usage.fixedDataLength() != null) {
            this.data = reader.readBytes(usage.fixedDataLength());
        } else if (usage.maxDataLength() != null){
            this.data = reader.readVarBytes(usage.maxDataLength());
        } else {
            this.data = reader.readVarBytes();
        }
    }

    @Override
    public void serialize(BinaryWriter writer) throws IOException {
        writer.writeByte(usage.byteValue());
        if (usage.fixedDataLength() != null) {
            writer.write(data);
        } else {
            writer.writeVarBytes(data);
        }
    }
}
