package com.axlabs.neow3j.protocol.core.methods.response;

import com.axlabs.neow3j.protocol.core.Response;

public class NeoGetBlockSysFee extends Response<String> {

    public String getFee() {
        return getResult();
    }

}
