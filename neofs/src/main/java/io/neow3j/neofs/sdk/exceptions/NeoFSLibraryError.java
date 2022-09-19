package io.neow3j.neofs.sdk.exceptions;

import io.neow3j.neofs.lib.responses.PointerResponse;
import io.neow3j.neofs.lib.responses.StringResponse;

import static io.neow3j.neofs.lib.NeoFSLibUtils.getResponseBytes;

public class NeoFSLibraryError extends Exception {

    public NeoFSLibraryError(String message) {
        super(message);
    }

    public NeoFSLibraryError(StringResponse response) {
        super(response.value);
    }

    public NeoFSLibraryError(PointerResponse response) {
        super(new String(getResponseBytes(response)));
    }

}
