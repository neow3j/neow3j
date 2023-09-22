package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.transaction.TransactionAttributeType;

import java.math.BigInteger;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NotValidBeforeAttribute extends TransactionAttribute {

    @JsonProperty(value = "height", required = true)
    private BigInteger height;

    public NotValidBeforeAttribute() {
        super(TransactionAttributeType.NOT_VALID_BEFORE);
    }

    public NotValidBeforeAttribute(BigInteger height) {
        this();
        this.height = height;
    }

    /**
     * @return the height.
     */
    public BigInteger getHeight() {
        return height;
    }

    /**
     * Transforms a {@link io.neow3j.transaction.NotValidBeforeAttribute} object to an instance of this type (its DTO
     * representation type).
     *
     * @param attr the NotValidBefore transaction attribute.
     * @return the DTO form of the attribute.
     */
    public static TransactionAttribute fromSerializable(io.neow3j.transaction.NotValidBeforeAttribute attr) {
        return new NotValidBeforeAttribute(attr.getHeight());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NotValidBeforeAttribute)) {
            return false;
        }
        NotValidBeforeAttribute other = (NotValidBeforeAttribute) o;
        return Objects.equals(getType(), other.getType()) &&
                Objects.equals(getHeight(), other.getHeight());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getHeight());
    }

    @Override
    public String toString() {
        return "NotValidBeforeAttribute{" +
                "height=" + getHeight() +
                "}";
    }

}
