package io.neow3j.protocol.core.methods.response;

import io.neow3j.protocol.core.Response;

public class NeoSendFrom extends Response<Transaction> {

    public Transaction getSendFrom() {
        return getResult();
    }

}