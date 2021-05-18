package io.neow3j.protocol.core.response;

import io.neow3j.protocol.core.Response;

public class NeoGetWalletUnclaimedGas extends Response<String> {

    public String getWalletUnclaimedGas() {
        return getResult();
    }

}
