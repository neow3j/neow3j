package io.neow3j.protocol.core.response;

import io.neow3j.protocol.core.Response;

public class NeoConnectionCount extends Response<Integer> {

    public Integer getCount() {
        return getResult();
    }

}
