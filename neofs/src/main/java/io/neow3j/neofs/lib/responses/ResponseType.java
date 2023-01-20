package io.neow3j.neofs.lib.responses;

import static java.lang.String.format;

public enum ResponseType {

    // Accounting
    DECIMAL("accounting.Decimal"),
    // Netmap
    NETWORK("netmap.NetworkInfo"),
    ENDPOINT("netmap.EndpointResponse"),
    // Container
    CONTAINER_ID("cid.ID"),
    CONTAINER("container.Container"),
    CONTAINER_LIST("container.ListResponse"),
    // Object
    OBJECT_ID("oid.ID"),
    OBJECT("object.Object"),

    STRING("string"),
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
        throw new IllegalArgumentException(format("No ResponseType match found for '%s'.", type));
    }

    @Override
    public String toString() {
        return type;
    }

}
