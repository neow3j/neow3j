package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenState {

    @JsonProperty(value = "name", required = true)
    private String name;

    @JsonProperty(value = "description", required = true)
    private String description;

    public TokenState() {
    }

    public TokenState(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * Gets the name.
     *
     * @return the name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the description.
     *
     * @return the description.
     */
    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TokenState)) {
            return false;
        }
        TokenState that = (TokenState) o;
        return Objects.equals(getName(), that.getName()) &&
                Objects.equals(getDescription(), that.getDescription());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getDescription());
    }

    @Override
    public String toString() {
        return "Properties{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                "}";
    }
}
