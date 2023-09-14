package io.neow3j.transaction;

import io.neow3j.serialization.BinaryReader;
import io.neow3j.serialization.BinaryWriter;
import io.neow3j.utils.BigIntegers;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Objects;

/**
 * A not valid before attribute can be used to specify a block height (index) from which on the transaction is valid.
 */
public class NotValidBeforeAttribute extends TransactionAttribute {

    private static final int HEIGHT_BYTE_SIZE = 4;

    /**
     * The height (index) of the block from which on the transaction is valid.
     */
    private BigInteger height;

    public NotValidBeforeAttribute(BigInteger height) {
        super(TransactionAttributeType.NOT_VALID_BEFORE);
        if (height == null) {
            throw new IllegalArgumentException("Height cannot be null.");
        }
        this.height = height;
    }

    /**
     * @return the height (index) of the block from which on the transaction is valid.
     */
    public BigInteger getHeight() {
        return height;
    }

    @Override
    protected int getSizeWithoutType() {
        return HEIGHT_BYTE_SIZE;
    }

    @Override
    protected void deserializeWithoutType(BinaryReader reader) throws IOException {
        height = BigIntegers.fromLittleEndianByteArray(reader.readBytes(HEIGHT_BYTE_SIZE));
    }

    @Override
    protected void serializeWithoutType(BinaryWriter writer) throws IOException {
        writer.write(BigIntegers.toLittleEndianByteArrayZeroPadded(height, HEIGHT_BYTE_SIZE));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NotValidBeforeAttribute)) {
            return false;
        }
        NotValidBeforeAttribute that = (NotValidBeforeAttribute) o;
        return Objects.equals(getHeight(), that.getHeight());
    }

}
