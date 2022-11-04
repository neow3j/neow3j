package io.neow3j.protocol.core.response;

import io.neow3j.protocol.core.Response;
import io.neow3j.protocol.exceptions.RpcResponseErrorException;

public class NeoGetApplicationLog extends Response<NeoApplicationLog> {

    /**
     * @return the result.
     * @throws RpcResponseErrorException if the Neo node returned an error.
     */
    public NeoApplicationLog getApplicationLog() {
        return getResult();
    }

}
