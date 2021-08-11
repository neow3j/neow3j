package io.neow3j.protocol.core.response;

import io.neow3j.protocol.core.Response;

public class NeoExpressShutdown extends Response<ExpressShutdown> {

    public ExpressShutdown getExpressShutdown() {
        return getResult();
    }

}
