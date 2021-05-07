package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.neow3j.protocol.ObjectMapperFactory;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NFTokenState {

    @JsonProperty(value = "name", required = true)
    private String name;

    public NFTokenState() {
    }

    public NFTokenState(String name) {
        this.name = name;
    }

    /**
     * Gets the name.
     *
     * @return the name.
     */
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NFTokenState)) {
            return false;
        }
        NFTokenState that = (NFTokenState) o;
        return Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }

    public String toJson() throws JsonProcessingException {
        return ObjectMapperFactory.getObjectMapper().writeValueAsString(this);
    }

    @Override
    public String toString() {
        return "Properties{" +
                "name='" + name + '\'' +
                "}";
    }

}
