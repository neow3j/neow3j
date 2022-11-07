package io.neow3j.neofs.lib.responses;

import com.sun.jna.Structure;

public abstract class Response extends Structure implements Structure.ByValue {

    public String type;

    public ResponseType getType() {
        return ResponseType.fromString(type);
    }

    public boolean isType(ResponseType type) {
        return getType() == type;
    }

}
