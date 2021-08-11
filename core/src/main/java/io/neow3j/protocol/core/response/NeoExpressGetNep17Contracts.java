package io.neow3j.protocol.core.response;

import io.neow3j.protocol.core.Response;

import java.util.List;

public class NeoExpressGetNep17Contracts extends Response<List<Nep17Contract>> {

    public List<Nep17Contract> getNep17Contracts() {
        return getResult();
    }

}
