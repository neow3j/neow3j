package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.neow3j.transaction.TransactionAttributeType;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HighPriorityAttribute extends TransactionAttribute {

    public HighPriorityAttribute() {
        super(TransactionAttributeType.HIGH_PRIORITY);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof HighPriorityAttribute)) {
            return false;
        }
        HighPriorityAttribute other = (HighPriorityAttribute) o;
        return getType() == other.getType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }

}
