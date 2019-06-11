package io.neow3j.protocol.core.methods.response.notification;

import com.fasterxml.jackson.annotation.JsonValue;

public enum NotificationStateType {

    ARRAY("Array"),
    MAP("Map");

    private String jsonValue;

    NotificationStateType(String jsonValue) {
        this.jsonValue = jsonValue;
    }

    @JsonValue
    public String jsonValue() {
        return this.jsonValue;
    }
}
