package com.axlabs.neow3j.protocol.core.methods.response;

import com.axlabs.neow3j.protocol.core.Response;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class NeoGetRawMemPool extends Response<List<String>> {

    public List<String> getAddresses() {
        return getResult();
    }

}
