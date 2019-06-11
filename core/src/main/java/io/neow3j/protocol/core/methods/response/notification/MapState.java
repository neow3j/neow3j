package io.neow3j.protocol.core.methods.response.notification;

import java.util.Map;

public class MapState extends State {

    private Map<String, NotificationParameter> value;

    public MapState() {
        super(NotificationStateType.MAP);
    }

    public MapState(Map<String, NotificationParameter> value) {
        super(NotificationStateType.MAP);
        this.value = value;
    }

    @Override
    public Object getValue() {
        return value;
    }
}
