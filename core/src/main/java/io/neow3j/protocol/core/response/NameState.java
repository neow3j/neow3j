package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.types.Hash160;

import java.util.Objects;

/**
 * Represents the state of a Neo Name Service domain, i.e., the properties of a Neo Name Service NFT.
 */
public class NameState {

    @JsonProperty(value = "name", required = true)
    private String name;

    @JsonProperty(value = "expiration")
    private Long expiration;

    @JsonProperty(value = "admin")
    private Hash160 admin;

    public NameState() {
    }

    public NameState(String name, Long expiration, Hash160 admin) {
        this.name = name;
        this.expiration = expiration;
        this.admin = admin;
    }

    /**
     * @return the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @return the expiration.
     */
    public Long getExpiration() {
        return expiration;
    }

    /**
     * @return the admin.
     */
    public Hash160 getAdmin() {
        return admin;
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
                Objects.equals(getExpiration(), that.getExpiration()) &&
                Objects.equals(getAdmin(), that.getAdmin());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getExpiration(), getAdmin());
    }

    @Override
    public String toString() {
        String adminAddress = getAdmin() != null ? getAdmin().toAddress() : null;
        return "NameState{" +
                "name=" + getName() +
                ", expiration=" + getExpiration() +
                ", admin=" + adminAddress +
                "}";
    }

}
