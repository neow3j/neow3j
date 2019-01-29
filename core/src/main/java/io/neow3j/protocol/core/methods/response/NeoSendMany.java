package io.neow3j.protocol.core.methods.response;

import io.neow3j.protocol.core.Response;

public class NeoSendMany extends Response<Transaction> {

    public Transaction getSendMany() {
        return getResult();
    }

}