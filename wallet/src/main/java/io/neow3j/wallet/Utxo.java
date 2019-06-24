package io.neow3j.wallet;

import io.neow3j.crypto.transaction.RawTransactionInput;

import java.math.BigDecimal;

/**
 * Represents a transaction output.
 */
public class Utxo {

    private String txId;

    private Integer index;

    private BigDecimal value;

    public Utxo(String txId, Integer index, BigDecimal value) {
        this.txId = txId;
        this.index = index;
        this.value = value;
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
