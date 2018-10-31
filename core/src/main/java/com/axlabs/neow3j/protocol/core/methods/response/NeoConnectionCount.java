package com.axlabs.neow3j.protocol.core.methods.response;

import com.axlabs.neow3j.protocol.core.Response;

public class NeoConnectionCount extends Response<Integer> {

    public Integer getCount() {
        return getResult();
    }

}
