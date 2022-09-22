package io.neow3j.protocol.core.response;

import io.neow3j.protocol.exceptions.RpcResponseErrorException;
import io.neow3j.types.Hash256;
import io.neow3j.protocol.core.Response;

public class NeoBlockHash extends Response<Hash256> {

    /**
     * @return the result.
     * @throws RpcResponseErrorException if the Neo node returned an error.
     */
    public Hash256 getBlockHash() {
        return getResult();
    }

}