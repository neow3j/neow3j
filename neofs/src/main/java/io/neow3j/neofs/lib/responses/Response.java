package io.neow3j.neofs.lib.responses;

import com.sun.jna.Structure;
import io.neow3j.neofs.lib.exceptions.NeoFSLibException;

public abstract class Response extends Structure implements Structure.ByValue {

    public String type;

    public String getResponseType() throws NeoFSLibException {
        return type;
    }

    public boolean isResponseType(ExpectedResponseType expectedResponseType) {
        try {
            return getResponseType().equals(expectedResponseType.getValue());
        } catch (NeoFSLibException ignore) {
        }
        return false;
    }

    public String getUnexpectedResponseMessage() {
        if (this instanceof StringResponse) {
            String errorMsg = ((StringResponse) this).value;
            return errorMsg;
        } else if (this instanceof PointerResponse) {
            PointerResponse pointerResponse = (PointerResponse) this;
            String errorMsg = new String((pointerResponse).value.getByteArray(0, (pointerResponse).length));
            return errorMsg;
        } else {
            return "Shared library returned an error that could not be read.";
        }
    }

}
