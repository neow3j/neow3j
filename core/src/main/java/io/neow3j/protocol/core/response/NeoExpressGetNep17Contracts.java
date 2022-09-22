package io.neow3j.protocol.core.response;

import io.neow3j.protocol.core.Response;
import io.neow3j.protocol.exceptions.RpcResponseErrorException;

import java.util.List;

public class NeoExpressGetNep17Contracts extends Response<List<Nep17Contract>> {

    /**
     * @return the result.
     * @throws RpcResponseErrorException if the Neo node returned an error.
     */
    public List<Nep17Contract> getNep17Contracts() {
        return getResult();
    }

}
