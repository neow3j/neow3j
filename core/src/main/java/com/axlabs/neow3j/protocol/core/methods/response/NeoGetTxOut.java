package com.axlabs.neow3j.protocol.core.methods.response;

import com.axlabs.neow3j.protocol.core.Response;

public class NeoGetTxOut extends Response<TransactionOutput> {

    public TransactionOutput getTransaction() {
        return getResult();
    }

}
