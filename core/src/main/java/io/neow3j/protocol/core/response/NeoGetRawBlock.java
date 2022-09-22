package io.neow3j.protocol.core.response;

import io.neow3j.protocol.core.Response;
import io.neow3j.protocol.exceptions.RpcResponseErrorException;

public class NeoGetRawBlock extends Response<String> {

    /**
     * @return the result.
     * @throws RpcResponseErrorException if the Neo node returned an error.
     */
    public String getRawBlock() {
        return getResult();
    }

}
