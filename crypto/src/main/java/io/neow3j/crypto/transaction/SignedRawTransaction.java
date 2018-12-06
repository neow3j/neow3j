package io.neow3j.crypto.transaction;

import io.neow3j.crypto.Keys;
import io.neow3j.crypto.Sign;

import java.math.BigInteger;
import java.security.SignatureException;
import java.util.Objects;

public class SignedRawTransaction {

    private static final int LOWER_REAL_V = 27;

    private RawTransaction rawTransaction;
    private Sign.SignatureData signatureData;

    public SignedRawTransaction(RawTransaction rawTransaction, Sign.SignatureData signatureData) {
        this.rawTransaction = rawTransaction;
        this.signatureData = signatureData;
    }

    public Sign.SignatureData getSignatureData() {
        return signatureData;
    }

    public String getFrom() throws SignatureException {
        byte[] encodedTransaction = rawTransaction.toArray();
        byte v = signatureData.getV();
        byte[] r = signatureData.getR();
        byte[] s = signatureData.getS();
        Sign.SignatureData signatureDataV = new Sign.SignatureData(getRealV(v), r, s);
        BigInteger key = Sign.signedMessageToKey(encodedTransaction, signatureDataV);
        return Keys.getAddress(key);
    }

    public boolean verify(String from) throws SignatureException {
        String actualFrom = getFrom();
        return actualFrom.equals(from);
    }

    private byte getRealV(byte v) {
        if (v == LOWER_REAL_V || v == (LOWER_REAL_V + 1)) {
            return v;
        }
        byte realV = LOWER_REAL_V;
        int inc = 0;
        if ((int) v % 2 == 0) {
            inc = 1;
        }
        return (byte) (realV + inc);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SignedRawTransaction)) return false;
        SignedRawTransaction that = (SignedRawTransaction) o;
        return Objects.equals(getSignatureData(), that.getSignatureData());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSignatureData());
    }

    @Override
    public String toString() {
        return "SignedRawTransaction{" +
                "signatureData=" + signatureData +
                '}';
    }
}
