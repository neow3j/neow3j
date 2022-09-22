package io.neow3j.protocol.core.response;

import io.neow3j.protocol.core.Response;
import io.neow3j.protocol.exceptions.RpcResponseErrorException;

public class NeoCalculateNetworkFee extends Response<NeoNetworkFee> {

    /**
     * @return the result.
     * @throws RpcResponseErrorException if the Neo node returned an error.
     */
    public NeoNetworkFee getNetworkFee() {
        return getResult();
    }

}
