package io.neow3j.protocol.core.methods.response;

import io.neow3j.protocol.core.Response;

public class NeoSubmitBlock extends Response<Boolean> {

    public Boolean getSubmitBlock() {
        return getResult();
    }

}