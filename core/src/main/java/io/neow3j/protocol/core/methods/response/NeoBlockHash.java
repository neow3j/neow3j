package io.neow3j.protocol.core.methods.response;

import io.neow3j.protocol.core.Response;

public class NeoBlockHash extends Response<String> {

    public String getBlockHash() {
        return getResult();
    }

}