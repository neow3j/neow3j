package io.neow3j.protocol.core.response;

import io.neow3j.protocol.core.Response;

import java.util.List;

public class NeoExpressGetContractStorage extends Response<List<ContractStorageEntry>> {

    public List<ContractStorageEntry> getContractStorage() {
        return getResult();
    }

}
