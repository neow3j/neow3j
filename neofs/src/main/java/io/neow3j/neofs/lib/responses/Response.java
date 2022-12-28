package io.neow3j.neofs.lib.responses;

import com.sun.jna.Structure;
import io.neow3j.neofs.lib.exceptions.NeoFSLibException;

import static java.lang.String.format;

public abstract class Response extends Structure implements Structure.ByValue {

    public String type;

    public ResponseType getResponseType() throws NeoFSLibException {
        try {
            return ResponseType.fromString(type);
        } catch (IllegalArgumentException e) {
            throw new NeoFSLibException(
                    format("Could not find a matching type for this response. Type was '%s'.", type));
        }
    }

    public boolean isResponseType(ResponseType type) {
        try {
            return getResponseType() == type;
        } catch (NeoFSLibException ignore) {
        }
        return false;
    }

    public boolean isError() {
        return isResponseType(ResponseType.ERROR);
    }

    public NeoFSLibError getError() {
        if (!isError()) {
            throw new IllegalStateException("This response does not contain an error.");
        }
        if (this instanceof StringResponse) {
            String errorMsg = ((StringResponse) this).value;
            return new NeoFSLibError(errorMsg);
        } else if (this instanceof PointerResponse) {
            PointerResponse pointerResponse = (PointerResponse) this;
            String errorMsg = new String((pointerResponse).value.getByteArray(0, (pointerResponse).length));
            return new NeoFSLibError(errorMsg);
        } else {
            return new NeoFSLibError("Shared library returned an error that could not be read.");
        }
    }

}
