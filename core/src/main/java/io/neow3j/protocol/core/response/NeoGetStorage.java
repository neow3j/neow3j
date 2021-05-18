package io.neow3j.protocol.core.response;

import io.neow3j.protocol.core.Response;

public class NeoGetStorage extends Response<String> {

    public String getStorage() {
        return getResult();
    }

}
