package io.neow3j.protocol.core.methods.response;

import io.neow3j.protocol.core.Response;

public class NeoGetApplicationLog extends Response<NeoApplicationLog> {

    public NeoApplicationLog getApplicationLog() {
        return getResult();
    }

}
