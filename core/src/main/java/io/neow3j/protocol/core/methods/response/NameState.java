package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class NameState extends TokenState {

    @JsonProperty(value = "expiration")
    private Integer expiration;

    public NameState() {
    }

    public NameState(String name, String description, Integer expiration) {
        super(name, description);
        this.expiration = expiration;
    }

    public Integer getExpiration() {
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
                Objects.equals(getDescription(), that.getDescription()) &&
                Objects.equals(getExpiration(), that.getExpiration());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getDescription());
    }

    @Override
    public String toString() {
        return "Properties{" +
                "name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", expiration=" + expiration +
                "}";
    }
}
