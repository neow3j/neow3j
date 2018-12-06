package io.neow3j.protocol.core.methods.response;

import io.neow3j.protocol.core.Response;

public class NeoSendRawTransaction extends Response<Boolean> {

    public Boolean getSendRawTransaction() {
        return getResult();
    }

}