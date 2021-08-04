package io.neow3j.protocol.core.response;

import io.neow3j.protocol.core.Response;

public class NeoExpressCreateOracleResponseTx extends Response<String> {

    public String getOracleResponseTx() {
        return getResult();
    }

}
