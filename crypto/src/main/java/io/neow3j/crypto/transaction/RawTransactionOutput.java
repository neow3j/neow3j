package io.neow3j.crypto.transaction;

import io.neow3j.utils.Keys;
import io.neow3j.io.BinaryReader;
import io.neow3j.io.BinaryWriter;
import io.neow3j.io.NeoSerializable;
import io.neow3j.model.types.NEOAsset;
import io.neow3j.utils.ArrayUtils;
import io.neow3j.utils.Numeric;

import java.io.IOException;
import java.util.Objects;

public class RawTransactionOutput extends NeoSerializable {

    private String assetId;

    private String value;

    private String address;

    public RawTransactionOutput() {
    }

    public RawTransactionOutput(String assetId, String value, String address) {
        this.assetId = assetId;
        this.value = value;
        this.address = address;
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

    public static RawTransactionOutput createNeoTransactionOutput(String value, String address) {
        return new RawTransactionOutput(NEOAsset.HASH_ID, value, address);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RawTransactionOutput)) return false;
        RawTransactionOutput that = (RawTransactionOutput) o;
        return Objects.equals(getAssetId(), that.getAssetId()) &&
                Objects.equals(getValue(), that.getValue()) &&
                Objects.equals(getAddress(), that.getAddress());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAssetId(), getValue(), getAddress());
    }

    @Override
    public String toString() {
        return "TransactionOutput{" +
                "assetId='" + assetId + '\'' +
                ", value='" + value + '\'' +
                ", address='" + address + '\'' +
                '}';
    }

    @Override
    public void deserialize(BinaryReader reader) throws IOException {
        this.assetId = Numeric.toHexStringNoPrefix(ArrayUtils.reverseArray(reader.readBytes(32)));
        this.value = Numeric.fromFixed8ToDecimal(ArrayUtils.reverseArray(reader.readBytes(8))).toString();
        this.address = Keys.toAddress(reader.readBytes(20));
    }

    @Override
    public void serialize(BinaryWriter writer) throws IOException {
        writer.write(ArrayUtils.reverseArray(Numeric.hexStringToByteArray(assetId)));
        byte[] value = Numeric.fromBigDecimalToFixed8Bytes(this.value);
        writer.write(ArrayUtils.reverseArray(value));
        writer.write(Keys.toScriptHash(this.address));
    }
}
