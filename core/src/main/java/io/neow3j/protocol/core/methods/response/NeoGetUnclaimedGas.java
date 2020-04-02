package io.neow3j.protocol.core.methods.response;

import io.neow3j.protocol.core.Response;

public class NeoGetUnclaimedGas extends Response<String> {

    public String getUnclaimedGas() {
        return getResult();
    }

}
