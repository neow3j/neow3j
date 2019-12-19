package io.neow3j.wallet;

import io.neow3j.transaction.RawTransactionInput;

import java.math.BigDecimal;

/**
 * Represents a transaction output.
 */
public class Utxo {

    private String assetId;

    private String txId;

    private Integer index;

    private BigDecimal value;

    public Utxo(String assetId, String txId, Integer index, BigDecimal value) {
        this.assetId = assetId;
        this.txId = txId;
        this.index = index;
        this.value = value;
    }

    public Utxo(String assetId, String txId, Integer index, String value) {
        this(assetId, txId, index, new BigDecimal(value));
    }

    public Utxo(String assetId, String txId, Integer index, double value) {
        this(assetId, txId, index, Double.toString(value));
    }

    public String getAssetId() {
        return assetId;
    }

    public String getTxId() {
        return txId;
    }

    public Integer getIndex() {
        return index;
    }

    public BigDecimal getValue() {
        return value;
    }

    public RawTransactionInput toTransactionInput() {
        return new RawTransactionInput(txId, index);
    }
}
