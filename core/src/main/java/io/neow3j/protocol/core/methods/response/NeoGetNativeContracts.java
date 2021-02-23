package io.neow3j.protocol.core.methods.response;

import io.neow3j.protocol.core.Response;

import java.util.List;

public class NeoGetNativeContracts extends Response<List<NeoGetContractState.ContractState>> {

    public List<NeoGetContractState.ContractState> getNativeContracts() {
        return getResult();
    }
}
