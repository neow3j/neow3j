package io.neow3j.transaction;

import io.neow3j.serialization.BinaryReader;
import io.neow3j.serialization.BinaryWriter;

import java.util.Objects;

/**
 * A high priority attribute can be used by committee members to prioritize a transaction.
 */
public class HighPriorityAttribute extends TransactionAttribute {

    public HighPriorityAttribute() {
        super(TransactionAttributeType.HIGH_PRIORITY);
    }

    @Override
    protected int getSizeWithoutType() {
        return 0;
    }

    @Override
    protected void deserializeWithoutType(BinaryReader reader) {
    }

    @Override
    protected void serializeWithoutType(BinaryWriter writer) {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof HighPriorityAttribute)) {
            return false;
        }
        HighPriorityAttribute that = (HighPriorityAttribute) o;
        return Objects.equals(getType(), that.getType());
    }

}
