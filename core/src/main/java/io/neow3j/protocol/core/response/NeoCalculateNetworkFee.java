package io.neow3j.protocol.core.response;

import io.neow3j.protocol.core.Response;

public class NeoCalculateNetworkFee extends Response<NeoNetworkFee> {

    public NeoNetworkFee getNetworkFee() {
        return getResult();
    }
}
