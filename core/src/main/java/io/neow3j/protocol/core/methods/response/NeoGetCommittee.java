package io.neow3j.protocol.core.methods.response;

import io.neow3j.protocol.core.Response;

import java.util.List;

public class NeoGetCommittee extends Response<List<String>> {

    public List<String> getCommittee() {
        return getResult();
    }

}
