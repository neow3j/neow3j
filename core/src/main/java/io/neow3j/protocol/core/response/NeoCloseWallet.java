package io.neow3j.protocol.core.response;

import io.neow3j.protocol.core.Response;

public class NeoCloseWallet extends Response<Boolean> {

    public Boolean getCloseWallet() {
        return getResult();
    }

}