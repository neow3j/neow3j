package io.neow3j.protocol.core.methods.response;

import io.neow3j.protocol.core.Response;

public class NeoInvokeScript extends Response<InvocationResult> {

    public InvocationResult getInvocationResult() {
        return getResult();
    }

}
