package io.neow3j.protocol.core.response;

import io.neow3j.protocol.core.Response;

public class NeoGetNewAddress extends Response<String> {

    public String getAddress() {
        return getResult();
    }

}
