package io.neow3j.protocol.core.response;

import io.neow3j.protocol.core.Response;

public class NeoGetProof extends Response<String> {

    public String getProof() {
        return getResult();
    }

}
