package io.neow3j.protocol.core.response;

import io.neow3j.protocol.core.Response;

public class NeoExpressGetPopulatedBlocks extends Response<PopulatedBlocks> {

    public PopulatedBlocks getPopulatedBlocks() {
        return getResult();
    }

}
