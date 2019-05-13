package io.neow3j.crypto;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

public class Claim {

    private BigDecimal claimValue;
    /** ID of the transaction that is the source of this claim */
    private String txId;
    /** Index of the transaction output that is the source of this claim */
    private int index;
    /** The value of the transaction output in NEO */
    private BigInteger neoValue;
    /** Block height of the transaction that is input to this claim */
    private int startHeight;
    /** Block height of the transaction in which the NEO have been spent */
    private int endHeight;

    public Claim(BigDecimal claimValue, String txId, int index, BigInteger neoValue, int startHeight, int endHeight) {
        this.claimValue = claimValue;
        this.txId = txId;
        this.index = index;
        this.neoValue = neoValue;
        this.startHeight = startHeight;
        this.endHeight = endHeight;
    }

    public BigDecimal getClaimValue() {
        return claimValue;
    }

    public String getTxId() {
        return txId;
    }

    public int getIndex() {
        return index;
    }

    public BigInteger getNeoValue() {
        return neoValue;
    }

    public int getStartHeight() {
        return startHeight;
    }

    public int getEndHeight() {
        return endHeight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Claim claim = (Claim) o;
        return getIndex() == claim.getIndex() &&
                getStartHeight() == claim.getStartHeight() &&
                getEndHeight() == claim.getEndHeight() &&
                Objects.equals(getClaimValue(), claim.getClaimValue()) &&
                Objects.equals(getTxId(), claim.getTxId()) &&
                Objects.equals(getNeoValue(), claim.getNeoValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClaimValue(), getTxId(), getIndex(), getNeoValue(), getStartHeight(), getEndHeight());
    }

    @Override
    public String toString() {
        return "Claim{" +
                "claimValue=" + claimValue +
                ", txId='" + txId + '\'' +
                ", index=" + index +
                ", neoValue=" + neoValue +
                ", startHeight=" + startHeight +
                ", endHeight=" + endHeight +
                '}';
    }
}
