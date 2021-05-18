package io.neow3j.protocol.core.response;

import io.neow3j.protocol.core.Response;

public class NeoGetRawTransaction extends Response<String> {

    public String getRawTransaction() {
        return getResult();
    }

}
