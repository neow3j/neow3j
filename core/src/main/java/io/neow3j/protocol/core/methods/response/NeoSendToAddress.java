package io.neow3j.protocol.core.methods.response;

import io.neow3j.protocol.core.Response;

public class NeoSendToAddress extends Response<Transaction> {

    public Transaction getSendToAddress() {
        return getResult();
    }

}