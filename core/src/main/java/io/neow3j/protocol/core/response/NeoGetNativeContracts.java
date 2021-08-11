package io.neow3j.protocol.core.response;

import io.neow3j.protocol.core.Response;

import java.util.List;

public class NeoGetNativeContracts extends Response<List<NativeContractState>> {

    public List<NativeContractState> getNativeContracts() {
        return getResult();
    }

}
