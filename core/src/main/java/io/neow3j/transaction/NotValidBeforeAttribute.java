package io.neow3j.transaction;

import io.neow3j.serialization.BinaryReader;
import io.neow3j.serialization.BinaryWriter;
import io.neow3j.utils.BigIntegers;

import java.io.IOException;
import java.math.BigInteger;

/**
 * A not valid before attribute can be used to specify a block height from which on the transaction is valid.
 */
public class NotValidBeforeAttribute extends TransactionAttribute {

    /**
     * The height of the block from which on the transaction is valid.
     */
    public BigInteger height;

    public NotValidBeforeAttribute(BigInteger height) {
        super(TransactionAttributeType.NOT_VALID_BEFORE);
        if (height == null) {
            throw new IllegalArgumentException("Height cannot be null.");
        }
        this.height = height;
    }

    /**
     * @return the height of the block from which on the transaction is valid.
     */
    public BigInteger getHeight() {
        return height;
    }

    @Override
    protected int getSizeWithoutType() {
        return 4; // 4 bytes for the height
    }

    @Override
    protected void deserializeWithoutType(BinaryReader reader) throws IOException {
        height = BigIntegers.fromLittleEndianByteArray(reader.readBytes(4));
    }

    @Override
    protected void serializeWithoutType(BinaryWriter writer) throws IOException {
        writer.write(BigIntegers.toLittleEndianByteArray(height));
    }

}
