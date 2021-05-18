package io.neow3j.protocol.core.methods.response;

import io.neow3j.types.Hash256;
import io.neow3j.protocol.core.Response;

public class NeoBlockHash extends Response<Hash256> {

    public Hash256 getBlockHash() {
        return getResult();
    }

}