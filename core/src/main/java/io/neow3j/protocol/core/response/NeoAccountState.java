package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.crypto.ECKeyPair.ECPublicKey;

import java.math.BigInteger;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NeoAccountState {

    @JsonProperty(value = "balance")
    private BigInteger balance;

    @JsonProperty(value = "balanceHeight")
    private BigInteger balanceHeight;

    @JsonProperty(value = "voteTo")
    private ECPublicKey publicKey;

    @JsonProperty(value = "lastGasPerVote")
    private BigInteger lastGasPerVote;

    public NeoAccountState() {
    }

    public NeoAccountState(BigInteger balance, BigInteger balanceHeight, ECPublicKey publicKey,
            BigInteger lastGasPerVote) {
        this.balance = balance;
        this.balanceHeight = balanceHeight;
        this.publicKey = publicKey;
        this.lastGasPerVote = lastGasPerVote;
    }

    public static NeoAccountState withNoVote(BigInteger balance, BigInteger updateHeight, BigInteger lastGasPerVote) {
        return new NeoAccountState(balance, updateHeight, null, lastGasPerVote);
    }

    public static NeoAccountState withNoBalance() {
        return new NeoAccountState(BigInteger.ZERO, null, null, null);
    }

    public BigInteger getBalance() {
        return balance;
    }

    public BigInteger getBalanceHeight() {
        return balanceHeight;
    }

    public ECPublicKey getPublicKey() {
        return publicKey;
    }

    public BigInteger getLastGasPerVote() {
        return lastGasPerVote;
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
                Objects.equals(getBalanceHeight(), that.getBalanceHeight()) &&
                Objects.equals(getPublicKey(), that.getPublicKey()) &&
                Objects.equals(getLastGasPerVote(), that.getLastGasPerVote());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBalance(), getBalanceHeight(), getPublicKey(), getLastGasPerVote());
    }

    @Override
    public String toString() {
        return "NeoAccountState{" +
                "balance=" + getBalance().toString() +
                ", updateHeight=" + getBalanceHeight() +
                ", voteTo=" + getPublicKey() +
                ", lastGasPerVote=" + getLastGasPerVote() +
                "}";
    }

}
