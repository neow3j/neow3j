package io.neow3j.protocol.core.response;

import io.neow3j.protocol.core.Response;

import java.util.List;

public class NeoExpressListOracleRequests extends Response<List<OracleRequest>> {

    public List<OracleRequest> getOracleRequests() {
        return getResult();
    }

}
