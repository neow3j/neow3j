package io.neow3j.protocol.core.response;

import io.neow3j.protocol.core.Response;

public class NeoImportPrivKey extends Response<NeoAddress> {

    public NeoAddress getAddresses() {
        return getResult();
    }

}
