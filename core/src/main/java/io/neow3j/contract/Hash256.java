package io.neow3j.contract;

import io.neow3j.constants.NeoConstants;
import io.neow3j.io.BinaryReader;
import io.neow3j.io.BinaryWriter;
import io.neow3j.io.NeoSerializable;
import io.neow3j.io.exceptions.DeserializationException;
import io.neow3j.utils.ArrayUtils;
import io.neow3j.utils.Numeric;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;

/**
 * A Hash256 is a 32 bytes long hash created from some data by applying SHA-256.
 * These hashes are mostly used for obtaining transaction or block hashes.
 */
public class Hash256 extends NeoSerializable implements Comparable<Hash256> {

    /**
     * The hash is stored as an unsigned integer in little-endian order.
     */
    private byte[] hash;

    /**
     * A zero address hash.
     */
    public static final Hash256 ZERO =
            new Hash256("0000000000000000000000000000000000000000000000000000000000000000");

    /**
     * Constructs a new hash with 32 zero bytes.
     */
    public Hash256() {
        hash = new byte[NeoConstants.HASH256_SIZE];
    }

    /**
     * Constructs a new hash from the given byte array. The byte array must be in little-endian
     * order and 256 bits long.
     *
     * @param hash the hash in little-endian order.
     */
    public Hash256(byte[] hash) {
        checkAndThrowHashLength(hash);
        this.hash = hash;
    }

    /**
     * Constructs a new hash from the given hexadecimal string. The string must be in big-endian
     * order and 256 bits long.
     *
     * @param hash the hash in big-endian order.
     */
    public Hash256(String hash) {
        if (Numeric.isValidHexString(hash)) {
            this.hash = ArrayUtils.reverseArray(Numeric.hexStringToByteArray(hash));
            checkAndThrowHashLength(this.hash);
        } else {
            throw new IllegalArgumentException("String argument is not hexadecimal.");
        }
    }

    @Override
    public void deserialize(BinaryReader reader) throws DeserializationException {
        try {
            hash = reader.readBytes(NeoConstants.HASH256_SIZE);
        } catch (IOException e) {
            throw new DeserializationException(e);
        }
    }

    @Override
    public void serialize(BinaryWriter writer) throws IOException {
        writer.write(hash);
    }

    @Override
    public int getSize() {
        return NeoConstants.HASH256_SIZE;
    }

    /**
     * Gets the hash as a byte array in little-endian order.
     *
     * @return the hash byte array in little-endian order.
     */
    @Override
    public byte[] toArray() {
        return super.toArray();
    }

    /**
     * Gets the hash as a hexadecimal string in big-endian order without the '0x' prefix.
     *
     * @return the hash as hex string in big-endian order.
     */
    public String toString() {
        return Numeric.toHexStringNoPrefix(ArrayUtils.reverseArray(hash));
    }

    private void checkAndThrowHashLength(byte[] hash) {
        if (hash.length != NeoConstants.HASH256_SIZE) {
            throw new IllegalArgumentException("Hash must be " + NeoConstants.HASH256_SIZE +
                    " bytes long but was " + hash.length + " bytes.");
        }
    }

    @Override
    public int compareTo(Hash256 o) {
        return new BigInteger(1, ArrayUtils.reverseArray(hash))
                .compareTo(new BigInteger(1, ArrayUtils.reverseArray(o.toArray())));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Hash256 that = (Hash256) o;
        return Arrays.equals(hash, that.hash);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(hash);
    }

}
