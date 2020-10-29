package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NFTokenProperties {

    @JsonProperty(value = "name", required = true)
    private String name;

    @JsonProperty(value = "description", required = true)
    private String description;

    @JsonProperty("image")
    private String image;

    @JsonProperty("tokenURI")
    private String tokenURI;

    public NFTokenProperties() {
    }

    public NFTokenProperties(String name, String description, String image, String tokenURI) {
        this.name = name;
        this.description = description;
        this.image = image;
        this.tokenURI = tokenURI;
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

    /**
     * Gets the image URI.
     * <p>
     * This is an optional entry in the properties, the method returns null if no image URI is present.
     *
     * @return the image URI.
     */
    public String getImage() {
        if (image == null) {

        }
        return image;
    }

    /**
     * Gets the token URI.
     * <p>
     * This is an optional entry in the properties, the method returns null if no token URI is present.
     *
     * @return the token URI.
     */
    public String getTokenURI() {
        return tokenURI;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NFTokenProperties)) {
            return false;
        }
        NFTokenProperties that = (NFTokenProperties) o;
        return Objects.equals(getName(), that.getName()) &&
                Objects.equals(getDescription(), that.getDescription()) &&
                Objects.equals(getImage(), that.getImage()) &&
                Objects.equals(getTokenURI(), that.getTokenURI());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getDescription(), getImage(), getTokenURI());
    }

    @Override
    public String toString() {
        return "Properties{" +
                "name='" + name + '\'' +
                "description='" + description + '\'' +
                "image='" + image + '\'' +
                "tokenURI='" + tokenURI + '\'' +
                "}";
    }
}
