package io.neow3j.protocol.core.methods.response;

import io.neow3j.protocol.core.Response;

public class NeoGetBlockSysFee extends Response<String> {

    public String getFee() {
        return getResult();
    }

}
