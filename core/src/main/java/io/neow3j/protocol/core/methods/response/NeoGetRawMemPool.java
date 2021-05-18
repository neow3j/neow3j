package io.neow3j.protocol.core.methods.response;

import io.neow3j.types.Hash256;
import io.neow3j.protocol.core.Response;

import java.util.List;

public class NeoGetRawMemPool extends Response<List<Hash256>> {

    public List<Hash256> getAddresses() {
        return getResult();
    }

}
