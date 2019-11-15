package io.neow3j.transaction;

import io.neow3j.contract.ScriptHash;
import io.neow3j.io.BinaryReader;
import io.neow3j.io.BinaryWriter;
import io.neow3j.io.NeoSerializable;
import io.neow3j.model.types.NEOAsset;
import io.neow3j.utils.ArrayUtils;
import io.neow3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Objects;

public class RawTransactionOutput extends NeoSerializable {

    private String assetId;

    private BigDecimal value;

    private String address;

    public RawTransactionOutput() {
    }

    /**
     * Constructs a new transaction output with the given address as the recipient.
     *
     * @param assetId The asset.
     * @param amount  The asset amount.
     * @param address The receiving address.
     */
    public RawTransactionOutput(String assetId, String amount, String address) {
        this.assetId = assetId;
        this.value = new BigDecimal(amount);
        this.address = address;
    }

    /**
     * Constructs a new transaction output with the given address as the recipient
     *
     * @param assetId The asset.
     * @param amount  The asset amount.
     * @param address The receiving address.
     */
    public RawTransactionOutput(String assetId, double amount, String address) {
        this(assetId, Double.toString(amount), address);
    }

    public String getAssetId() {
        return assetId;
    }

    public String getValue() {
        return value.toPlainString();
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
                this.value.compareTo(that.value) == 0 &&
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
        this.value = Numeric.fromFixed8ToDecimal(reader.readBytes(8));
        this.address = new ScriptHash(reader.readBytes(20)).toAddress();
    }

    @Override
    public void serialize(BinaryWriter writer) throws IOException {
        writer.write(ArrayUtils.reverseArray(Numeric.hexStringToByteArray(assetId)));
        writer.write(Numeric.fromDecimalToFixed8ByteArray(this.value));
        writer.write(ScriptHash.fromAddress(this.address).toArray());
    }
}
