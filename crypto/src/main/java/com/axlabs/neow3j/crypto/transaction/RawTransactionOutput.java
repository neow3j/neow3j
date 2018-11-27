package com.axlabs.neow3j.crypto.transaction;

import com.axlabs.neow3j.crypto.KeyUtils;
import com.axlabs.neow3j.crypto.NEOAsset;
import com.axlabs.neow3j.io.BinaryReader;
import com.axlabs.neow3j.io.BinaryWriter;
import com.axlabs.neow3j.io.NeoSerializable;
import com.axlabs.neow3j.utils.ArrayUtils;
import com.axlabs.neow3j.utils.Numeric;
import org.bouncycastle.util.BigIntegers;

import java.io.IOException;
import java.util.Objects;

public class RawTransactionOutput extends NeoSerializable {

    private int index;

    private String assetId;

    private String value;

    private String address;

    public RawTransactionOutput() {
    }

    public RawTransactionOutput(int index, String assetId, String value, String address) {
        this.index = index;
        this.assetId = assetId;
        this.value = value;
        this.address = address;
    }

    public int getIndex() {
        return index;
    }

    public String getAssetId() {
        return assetId;
    }

    public String getValue() {
        return value;
    }

    public String getAddress() {
        return address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RawTransactionOutput)) return false;
        RawTransactionOutput that = (RawTransactionOutput) o;
        return getIndex() == that.getIndex() &&
                Objects.equals(getAssetId(), that.getAssetId()) &&
                Objects.equals(getValue(), that.getValue()) &&
                Objects.equals(getAddress(), that.getAddress());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIndex(), getAssetId(), getValue(), getAddress());
    }

    @Override
    public String toString() {
        return "TransactionOutput{" +
                "index=" + index +
                ", assetId='" + assetId + '\'' +
                ", value='" + value + '\'' +
                ", address='" + address + '\'' +
                '}';
    }

    @Override
    public void deserialize(BinaryReader reader) throws IOException {
        this.assetId = Numeric.toHexStringNoPrefix(ArrayUtils.reverseArray(reader.readBytes(32)));
        this.value = Numeric.toBigDecimal(ArrayUtils.reverseArray(reader.readBytes(8))).toString();
        this.address = KeyUtils.toAddress(reader.readBytes(20));
    }

    @Override
    public void serialize(BinaryWriter writer) throws IOException {
        writer.write(ArrayUtils.reverseArray(Numeric.hexStringToByteArray(assetId)));
        byte[] value = BigIntegers.asUnsignedByteArray(8, NEOAsset.toBigInt(this.value));
        writer.write(ArrayUtils.reverseArray(value));
        writer.write(KeyUtils.toScriptHash(this.address));
    }
}
