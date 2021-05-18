package io.neow3j.protocol.core.response;

import io.neow3j.protocol.core.Response;

public class NeoGetRawBlock extends Response<String> {

    public String getRawBlock() {
        return getResult();
    }

}
