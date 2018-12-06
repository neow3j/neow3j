package io.neow3j.protocol.core;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * NEO does not have the same notion of "default block parameter" as described on
 * https://github.com/ethereum/wiki/wiki/JSON-RPC#the-default-block-parameter ,
 * but we will leave this here as a nice abstraction layer for neow3j lib.
 */
public enum BlockParameterName implements BlockParameter {

    EARLIEST("earliest"),
    LATEST("latest");

    private String name;

    BlockParameterName(String name) {
        this.name = name;
    }

    @JsonValue
    @Override
    public String getValue() {
        return name;
    }

    public static BlockParameterName fromString(String name) {
        if (name != null) {
            for (BlockParameterName blockParameterName :
                    BlockParameterName.values()) {
                if (name.equalsIgnoreCase(blockParameterName.name)) {
                    return blockParameterName;
                }
            }
        }
        return valueOf(name);
    }
}
