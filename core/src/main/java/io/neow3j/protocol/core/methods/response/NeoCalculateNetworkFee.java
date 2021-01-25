package io.neow3j.protocol.core.methods.response;

import io.neow3j.protocol.core.Response;

public class NeoCalculateNetworkFee extends Response<NeoNetworkFee> {

    public NeoNetworkFee getNetworkFee() {
        return getResult();
    }
}
