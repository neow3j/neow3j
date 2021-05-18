package io.neow3j.protocol.core.response;

import io.neow3j.protocol.core.Response;

import java.util.List;

public class NeoListAddress extends Response<List<NeoAddress>> {

    public List<NeoAddress> getAddresses() {
        return getResult();
    }

}
