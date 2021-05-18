package io.neow3j.protocol.core.response;

import io.neow3j.protocol.core.Response;

public class NeoOpenWallet extends Response<Boolean> {

    public Boolean getOpenWallet() {
        return getResult();
    }

}