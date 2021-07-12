package io.neow3j.protocol.core.response;

import io.neow3j.protocol.core.Response;

public class NeoExpressCreateCheckpoint extends Response<String> {

    public String getFilename() {
        return getResult();
    }

}
