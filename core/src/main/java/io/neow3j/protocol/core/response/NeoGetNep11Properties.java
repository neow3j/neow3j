package io.neow3j.protocol.core.response;

import io.neow3j.protocol.core.Response;

import java.util.Map;

public class NeoGetNep11Properties extends Response<Map<String, String>> {

    public Map<String, String> getProperties() {
        return getResult();
    }

}
