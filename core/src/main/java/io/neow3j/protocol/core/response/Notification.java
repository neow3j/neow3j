package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.types.Hash160;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Notification {

    @JsonProperty("contract")
    private Hash160 contract;

    @JsonProperty("eventname")
    private String eventName;

    @JsonProperty("state")
    private StackItem state;

    public Notification() {
    }

    public Notification(Hash160 contract, String eventName, StackItem state) {
        this.contract = contract;
        this.eventName = eventName;
        this.state = state;
    }

    /**
     * @return the script hash of the contract that fired this notification.
     */
    public Hash160 getContract() {
        return contract;
    }

    /**
     * Gets the event name as described in the manifest of the contract that fired the notification.
     * <p>
     * The words event and notification can be used synonymously here.
     *
     * @return the event name.
     */
    public String getEventName() {
        return eventName;
    }

    /**
     * Gets the state attached to this notification.
     * <p>
     * It is up to the developer to know what to in the state and it needs knowledge about the contract that
     * triggered the notification.
     *
     * @return the notification's state as a NeoVM stack item.
     */
    public StackItem getState() {
        return state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Notification)) {
            return false;
        }
        Notification that = (Notification) o;
        return Objects.equals(getContract(), that.getContract()) &&
                Objects.equals(getEventName(), that.getEventName()) &&
                Objects.equals(getState(), that.getState());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getContract(), getEventName(), getState());
    }

    @Override
    public String toString() {
        return "Notification{" +
                "contract='" + contract + '\'' +
                ", eventname=" + eventName +
                ", state=" + state +
                '}';
    }
}
