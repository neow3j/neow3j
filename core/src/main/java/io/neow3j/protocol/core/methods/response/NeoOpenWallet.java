package io.neow3j.protocol.core.methods.response;

import io.neow3j.protocol.core.Response;

public class NeoOpenWallet extends Response<Boolean> {

    public Boolean getOpenWallet() {
        return getResult();
    }

}