package io.neow3j.protocol.core.response;

import io.neow3j.protocol.core.Response;

public class NeoTerminateSession extends Response<Boolean> {

    public boolean getTerminateSession() {
        return getResult();
    }

}
