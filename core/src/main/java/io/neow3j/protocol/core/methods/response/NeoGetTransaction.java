package io.neow3j.protocol.core.methods.response;

import io.neow3j.protocol.core.Response;

public class NeoGetTransaction extends Response<Transaction> {

    public Transaction getTransaction() {
        return getResult();
    }

}
