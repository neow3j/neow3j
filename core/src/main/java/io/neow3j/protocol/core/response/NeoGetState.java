package io.neow3j.protocol.core.response;

import io.neow3j.protocol.core.Response;

public class NeoGetState extends Response<String> {

    public String getState() {
        return getResult();
    }

}
