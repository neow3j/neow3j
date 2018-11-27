package com.axlabs.neow3j.crypto.transaction;

import com.axlabs.neow3j.crypto.Keys;
import com.axlabs.neow3j.crypto.Sign;
import com.axlabs.neow3j.crypto.transaction.RawTransaction;
import com.axlabs.neow3j.crypto.transaction.TransactionEncoder;
import com.axlabs.neow3j.crypto.transaction.RawScript;
import com.axlabs.neow3j.crypto.transaction.RawTransactionAttribute;
import com.axlabs.neow3j.crypto.transaction.RawTransactionInput;
import com.axlabs.neow3j.crypto.transaction.RawTransactionOutput;
import com.axlabs.neow3j.model.types.TransactionType;

import java.math.BigInteger;
import java.security.SignatureException;
import java.util.List;

public class SignedRawTransaction extends RawTransaction {

    private static final int CHAIN_ID_INC = 35;
    private static final int LOWER_REAL_V = 27;

    private Sign.SignatureData signatureData;

    public SignedRawTransaction(TransactionType transactionType, byte version, List<Object> specificTransactionData, List<RawTransactionAttribute> attributes, List<RawTransactionInput> inputs, List<RawTransactionOutput> outputs, List<RawScript> scripts, Sign.SignatureData signatureData) {
        super(transactionType, version, specificTransactionData, attributes, inputs, outputs, scripts);
        this.signatureData = signatureData;
    }

    public Sign.SignatureData getSignatureData() {
        return signatureData;
    }

    public String getFrom() throws SignatureException {
        Integer chainId = getChainId();
        byte[] encodedTransaction;
        if (null == chainId) {
            encodedTransaction = TransactionEncoder.encode(this);
        } else {
            encodedTransaction = TransactionEncoder.encode(this, chainId.byteValue());
        }
        byte v = signatureData.getV();
        byte[] r = signatureData.getR();
        byte[] s = signatureData.getS();
        Sign.SignatureData signatureDataV = new Sign.SignatureData(getRealV(v), r, s);
        BigInteger key = Sign.signedMessageToKey(encodedTransaction, signatureDataV);
        return Keys.getAddress(key);
    }

    public void verify(String from) throws SignatureException {
        String actualFrom = getFrom();
        if (!actualFrom.equals(from)) {
            throw new SignatureException("from mismatch");
        }
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

    public Integer getChainId() {
        byte v = signatureData.getV();
        if (v == LOWER_REAL_V || v == (LOWER_REAL_V + 1)) {
            return null;
        }
        Integer chainId = (v - CHAIN_ID_INC) / 2;
        return chainId;
    }
}
