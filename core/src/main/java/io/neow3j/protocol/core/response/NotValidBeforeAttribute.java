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
        return Objects.hash(type, getHeight());
    }

}
