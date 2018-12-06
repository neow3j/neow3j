package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class TransactionInput {

    @JsonProperty("txid")
    public String prevHash;

    @JsonProperty("vout")
    public int prevIndex;

    public TransactionInput() {
    }

    public TransactionInput(String prevHash, int prevIndex) {
        this.prevHash = prevHash;
        this.prevIndex = prevIndex;
    }

    public String getPrevHash() {
        return prevHash;
    }

    public int getPrevIndex() {
        return prevIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransactionInput)) return false;
        TransactionInput that = (TransactionInput) o;
        return getPrevIndex() == that.getPrevIndex() &&
                Objects.equals(getPrevHash(), that.getPrevHash());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPrevHash(), getPrevIndex());
    }

    @Override
    public String toString() {
        return "TransactionInput{" +
                "prevHash='" + prevHash + '\'' +
                ", prevIndex=" + prevIndex +
                '}';
    }
}
