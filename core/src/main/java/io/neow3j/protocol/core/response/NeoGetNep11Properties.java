package io.neow3j.protocol.core.response;

import io.neow3j.protocol.core.Response;
import io.neow3j.protocol.exceptions.RpcResponseErrorException;

import java.util.Map;

public class NeoGetNep11Properties extends Response<Map<String, String>> {

    /**
     * @return the result.
     * @throws RpcResponseErrorException if the Neo node returned an error.
     */
    public Map<String, String> getProperties() {
        return getResult();
    }

}
