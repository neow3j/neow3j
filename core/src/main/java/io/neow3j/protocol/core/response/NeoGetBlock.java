package io.neow3j.protocol.core.response;

import io.neow3j.protocol.core.Response;

public class NeoGetBlock extends Response<NeoBlock> {

    public NeoBlock getBlock() {
        return getResult();
    }

}
