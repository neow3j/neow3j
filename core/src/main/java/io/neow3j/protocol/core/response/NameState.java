package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class NameState extends NFTokenState {

    @JsonProperty(value = "expiration")
    private Long expiration;

    public NameState() {
    }

    public NameState(String name, Long expiration) {
        super(name);
        this.expiration = expiration;
    }

    public Long getExpiration() {
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
                Objects.equals(getExpiration(), that.getExpiration());
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
