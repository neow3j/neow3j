package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.transaction.TransactionAttributeType;
import io.neow3j.types.Hash256;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConflictsAttribute extends TransactionAttribute {

    @JsonProperty(value = "hash", required = true)
    private Hash256 hash;

    public ConflictsAttribute() {
        super(TransactionAttributeType.CONFLICTS);
    }

    public ConflictsAttribute(Hash256 hash) {
        this();
        this.hash = hash;
    }

    /**
     * @return the conflict hash.
     */
    public Hash256 getHash() {
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ConflictsAttribute)) {
            return false;
        }
        ConflictsAttribute other = (ConflictsAttribute) o;
        return Objects.equals(getType(), other.getType()) &&
                Objects.equals(getHash(), other.getHash());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getHash());
    }

    @Override
    public String toString() {
        return "ConflictsAttribute{" +
                "hash=" + getHash() +
                "}";
    }

}
