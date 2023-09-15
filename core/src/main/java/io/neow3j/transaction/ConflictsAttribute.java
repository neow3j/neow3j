package io.neow3j.transaction;

import io.neow3j.constants.NeoConstants;
import io.neow3j.serialization.BinaryReader;
import io.neow3j.serialization.BinaryWriter;
import io.neow3j.serialization.exceptions.DeserializationException;
import io.neow3j.types.Hash256;

import java.io.IOException;
import java.util.Objects;

/**
 * A conflicts attribute can be used to specify to only accept one of two conflicting transactions.
 * <p>
 * If the conflicting transaction is already contained in a block, the transaction with this attribute will be
 * rejected as invalid.
 * <p>
 * If the conflicting transaction is not yet contained in a block, but in the mempool, the transaction with the
 * bigger network fee will be accepted, and the other one will be rejected as invalid or removed from the mempool.
 */
public class ConflictsAttribute extends TransactionAttribute {

    /**
     * The hash of the conflicting transaction.
     */
    private Hash256 hash;

    public ConflictsAttribute(Hash256 hash) {
        super(TransactionAttributeType.CONFLICTS);
        if (hash == null) {
            throw new IllegalArgumentException("Conflict hash cannot be null.");
        }
        this.hash = hash;
    }

    /**
     * @return the hash of the conflicting transaction.
     */
    public Hash256 getHash() {
        return hash;
    }

    @Override
    protected int getSizeWithoutType() {
        return NeoConstants.HASH256_SIZE;
    }

    @Override
    protected void deserializeWithoutType(BinaryReader reader) throws DeserializationException {
        hash = reader.readSerializable(Hash256.class);
    }

    @Override
    protected void serializeWithoutType(BinaryWriter writer) throws IOException {
        hash.serialize(writer);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ConflictsAttribute)) {
            return false;
        }
        ConflictsAttribute that = (ConflictsAttribute) o;
        return Objects.equals(getHash(), that.getHash());
    }

}
