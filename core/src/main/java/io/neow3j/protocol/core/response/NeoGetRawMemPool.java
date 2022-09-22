package io.neow3j.protocol.core.response;

import io.neow3j.protocol.exceptions.RpcResponseErrorException;
import io.neow3j.types.Hash256;
import io.neow3j.protocol.core.Response;

import java.util.List;

public class NeoGetRawMemPool extends Response<List<Hash256>> {

    /**
     * @return the result.
     * @throws RpcResponseErrorException if the Neo node returned an error.
     */
    public List<Hash256> getAddresses() {
        return getResult();
    }

}
