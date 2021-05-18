package io.neow3j.protocol.core.response;

import io.neow3j.protocol.core.Response;

public class NeoVerifyProof extends Response<String> {

    public String verifyProof() {
        return getResult();
    }

}
