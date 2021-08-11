package io.neow3j.protocol.core.response;

import io.neow3j.protocol.core.Response;

public class NeoGetContractState extends Response<ContractState> {

    public ContractState getContractState() {
        return getResult();
    }

}
