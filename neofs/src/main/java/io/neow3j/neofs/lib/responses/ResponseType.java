package io.neow3j.neofs.lib.responses;

import static java.lang.String.format;

public enum ResponseType {

    // Accounting
    DECIMAL("accounting.Decimal"),
    // Netmap
    NETWORK("netmap.NetworkInfo"),
    ENDPOINT("main.EndpointResponse"),
    // Container
    CONTAINER("container.Container"),
    CONTAINER_ID("cid.ID"),

    STRING("string"),
    ERROR("error"),
    BOOLEAN("bool");

    private final String type;

    ResponseType(String type) {
        this.type = type;
    }

    public static ResponseType fromString(String type) {
        for (ResponseType e : ResponseType.values()) {
            if (e.type.equals(type)) {
                return e;
            }
        }
        throw new IllegalArgumentException(format("There exists no supported response type with the provided string " +
                "value. The provided string value was %s.", type));
    }

    @Override
    public String toString() {
        return type;
    }

}
