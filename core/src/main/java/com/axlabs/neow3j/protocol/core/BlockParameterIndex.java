package com.axlabs.neow3j.protocol.core;

import com.axlabs.neow3j.utils.Numeric;
import com.fasterxml.jackson.annotation.JsonValue;

import java.math.BigInteger;

/**
 * BlockParameter implementation that takes a numeric value.
 */
public class BlockParameterIndex implements BlockParameter {

    private BigInteger blockIndex;

    public BlockParameterIndex(BigInteger blockIndex) {
        this.blockIndex = blockIndex;
    }

    public BlockParameterIndex(long blockIndex) {
        this(BigInteger.valueOf(blockIndex));
    }

    @Override
    @JsonValue
    public String getValue() {
        return Numeric.encodeQuantity(blockIndex);
    }

    public BigInteger getBlockIndex() {
        return blockIndex;
    }
}
