package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.crypto.ECKeyPair.ECPublicKey;

import java.math.BigInteger;
import java.util.Objects;

public class NeoAccountState {

    @JsonProperty(value = "balance")
    private BigInteger balance;

    @JsonProperty(value = "updateHeight")
    private BigInteger updateHeight;

    @JsonProperty(value = "voteTo")
    private ECPublicKey publicKey;

    public NeoAccountState() {
    }

    public NeoAccountState(BigInteger balance, BigInteger updateHeight, ECPublicKey publicKey) {
        this.balance = balance;
        this.updateHeight = updateHeight;
        this.publicKey = publicKey;
    }

    public static NeoAccountState withNoVote(BigInteger balance, BigInteger updateHeight) {
        return new NeoAccountState(balance, updateHeight, null);
    }

    public static NeoAccountState withNoBalance() {
        return new NeoAccountState(BigInteger.ZERO, null, null);
    }

    public BigInteger getBalance() {
        return balance;
    }

    public BigInteger getUpdateHeight() {
        return updateHeight;
    }

    public ECPublicKey getPublicKey() {
        return publicKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NeoAccountState)) {
            return false;
        }
        NeoAccountState that = (NeoAccountState) o;
        return Objects.equals(getBalance(), that.getBalance()) &&
                Objects.equals(getUpdateHeight(), that.getUpdateHeight()) &&
                Objects.equals(getPublicKey(), that.getPublicKey());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBalance(), getUpdateHeight(), getPublicKey());
    }

    @Override
    public String toString() {
        return "NeoAccountState{" +
                "balance=" + getBalance().toString() +
                ", updateHeight=" + getUpdateHeight() +
                ", voteTo=" + getPublicKey() +
                "}";
    }

}
