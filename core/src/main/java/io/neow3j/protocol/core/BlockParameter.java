package io.neow3j.protocol.core;

import java.math.BigInteger;

/**
 * Represents a block parameter. It takes either a block number or block name as input.
 *
 * NEO does not specifies the notion of "block name". This is an abstraction built by
 * neow3j library.
 */
public interface BlockParameter {

    static BlockParameter valueOf(BigInteger blockNumber) {
        return new BlockParameterIndex(blockNumber);
    }

    static BlockParameter valueOf(String blockName) {
        return BlockParameterName.fromString(blockName);
    }

    String getValue();
}
