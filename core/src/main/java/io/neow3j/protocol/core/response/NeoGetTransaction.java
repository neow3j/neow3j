package io.neow3j.protocol.core.response;

import io.neow3j.protocol.core.Response;

public class NeoGetTransaction extends Response<Transaction> {

    public Transaction getTransaction() {
        return getResult();
    }

}
