package io.neow3j.protocol.core.methods.response.notification;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

import java.util.List;

public class ArrayState extends State {

    @JsonProperty("value")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<NotificationParameter> value;

    public ArrayState() {
        super(NotificationStateType.ARRAY);
    }

    public ArrayState(List<NotificationParameter> value) {
        super(NotificationStateType.ARRAY);
        this.value = value;
    }

    public List<NotificationParameter> getValue() {
        return value;
    }
}
