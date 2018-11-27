package com.axlabs.neow3j.crypto.transaction;

import com.axlabs.neow3j.io.BinaryReader;
import com.axlabs.neow3j.io.BinaryWriter;
import com.axlabs.neow3j.io.NeoSerializable;
import com.axlabs.neow3j.model.types.TransactionAttributeUsageType;
import com.axlabs.neow3j.utils.Numeric;

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
    }

    public RawTransactionAttribute(TransactionAttributeUsageType usage, String data) {
        this.usage = usage;
        this.data = (data != null ? Numeric.hexStringToByteArray(data) : null);
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

    public void setData(byte[] data) {
        this.data = data;
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
        int bytesToRead = readBytesBaseOnUsageType();
        if (bytesToRead > 32) {
            this.data = reader.readVarBytes(bytesToRead);
        } else {
            this.data = reader.readBytes(bytesToRead);
        }
    }

    @Override
    public void serialize(BinaryWriter writer) throws IOException {
        writer.writeByte(this.usage.byteValue());
        int bytesToWrite = readBytesBaseOnUsageType();
        if (bytesToWrite > 32) {
            writer.writeVarBytes(this.data);
        } else {
            writer.writeVarBytes(this.data);
        }
    }

    private int readBytesBaseOnUsageType() {
        switch (this.usage) {
            case CONTRACT_HASH:
            case ECDH02:
            case ECDH03:
            case HASH1:
            case HASH2:
            case HASH3:
            case HASH4:
            case HASH5:
            case HASH6:
            case HASH7:
            case HASH8:
            case HASH9:
            case HASH10:
            case HASH11:
            case HASH12:
            case HASH13:
            case HASH15:
                return 32;
            default:
                return 255;
        }
    }

}
