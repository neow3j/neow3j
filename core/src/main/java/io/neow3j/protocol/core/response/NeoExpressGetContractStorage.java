package io.neow3j.protocol.core.response;

import io.neow3j.protocol.core.Response;
import io.neow3j.protocol.exceptions.RpcResponseErrorException;

import java.util.List;

public class NeoExpressGetContractStorage extends Response<List<ContractStorageEntry>> {

    /**
     * @return the result.
     * @throws RpcResponseErrorException if the Neo node returned an error.
     */
    public List<ContractStorageEntry> getContractStorage() {
        return getResult();
    }

}
