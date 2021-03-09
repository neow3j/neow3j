package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class NameState extends NFTokenState {

    @JsonProperty(value = "expiration")
    private long expiration;

    public NameState() {
    }

    public NameState(String name, long expiration) {
        super(name);
        this.expiration = expiration;
    }

    public long getExpiration() {
        return expiration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NameState)) {
            return false;
        }
        NameState that = (NameState) o;
        return Objects.equals(getName(), that.getName()) &&
                getExpiration() == that.getExpiration();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getExpiration());
    }

    @Override
    public String toString() {
        return "Properties{" +
                "name='" + getName() + '\'' +
                ", expiration=" + expiration +
                "}";
    }

}
