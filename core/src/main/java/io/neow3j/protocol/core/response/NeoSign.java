package io.neow3j.protocol.core.response;

import io.neow3j.protocol.core.Response;
import io.neow3j.protocol.exceptions.RpcResponseErrorException;
import io.neow3j.transaction.ContractParametersContext;

public class NeoSign extends Response<ContractParametersContext> {

    /**
     * @return the result.
     * @throws RpcResponseErrorException if the Neo node returned an error.
     */
    public ContractParametersContext getContext() {
        return getResult();
    }

}
