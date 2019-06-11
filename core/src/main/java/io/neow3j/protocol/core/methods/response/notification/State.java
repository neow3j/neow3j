package io.neow3j.protocol.core.methods.response.notification;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public abstract class State {

    @JsonProperty("type")
    protected NotificationStateType type;

    public State(NotificationStateType type) {
        this.type = type;
    }

    public NotificationStateType getType() {
        return type;
    }

    public abstract Object getValue();

    public List<NotificationParameter> getArray() {
        return (List<NotificationParameter>) getValue();
    }

    public Map<String, NotificationParameter> getMap() {
        return (Map<String, NotificationParameter>) getValue();
    }

}
