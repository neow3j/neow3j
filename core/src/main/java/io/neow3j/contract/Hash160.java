package io.neow3j.contract;

import io.neow3j.constants.NeoConstants;
import io.neow3j.crypto.Hash;
import io.neow3j.io.BinaryReader;
import io.neow3j.io.BinaryWriter;
import io.neow3j.io.NeoSerializable;
import io.neow3j.io.exceptions.DeserializationException;
import io.neow3j.utils.AddressUtils;
import io.neow3j.utils.ArrayUtils;
import io.neow3j.utils.Numeric;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

/**
 * A Hash160 is a 20 bytes long hash created from some data by first applying SHA-256 and then
 * RIPEMD-160. These hashes are mostly used for obtaining the script hash of a smart contract or an
 * account.
 */
public class Hash160 extends NeoSerializable implements Comparable<Hash160> {

    /**
     * The hash is stored as an unsigned integer in little-endian order.
     */
    private byte[] hash;

    /**
     * A zero-value hash.
     */
    public static final Hash160 ZERO = new Hash160("0000000000000000000000000000000000000000");

    /**
     * Constructs a new hash with 20 zero bytes.
     */
    public Hash160() {
        this.hash = new byte[NeoConstants.HASH160_SIZE];
    }

    /**
     * Constructs a new hash from the given byte array. The byte array must be in little-endian
     * order and 160 bits long.
     *
     * @param hash the hash in little-endian order.
     */
    public Hash160(byte[] hash) {
        checkAndThrowHashLength(hash);
        this.hash = hash;
    }

    /**
     * Constructs a new hash from the given hexadecimal string. The string must be in big-endian
     * order and 160 bits long.
     *
     * @param hash the hash in big-endian order.
     */
    public Hash160(String hash) {
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
            this.hash = reader.readBytes(NeoConstants.HASH160_SIZE);
        } catch (IOException e) {
            throw new DeserializationException(e);
        }
    }

    @Override
    public void serialize(BinaryWriter writer) throws IOException {
        writer.write(this.hash);
    }

    @Override
    public int getSize() {
        return NeoConstants.HASH160_SIZE;
    }

    /**
     * Gets the script hash as a byte array in little-endian order.
     *
     * @return the script hash byte array in little-endian order.
     */
    @Override
    public byte[] toArray() {
        return super.toArray();
    }

    /**
     * Gets the script hash as a hexadecimal string in big-endian order without the '0x' prefix.
     *
     * @return the script hash as hex string in big-endian order.
     */
    public String toString() {
        return Numeric.toHexStringNoPrefix(ArrayUtils.reverseArray(hash));
    }

    /**
     * Derives the address corresponding to this script hash.
     *
     * @return the address.
     */
    public String toAddress() {
        return AddressUtils.scriptHashToAddress(this.hash);
    }

    /**
     * Creates a script hash from the given address.
     *
     * @param address the address from which to derive the script hash.
     * @return the script hash.
     */
    public static Hash160 fromAddress(String address) {
        return new Hash160(AddressUtils.addressToScriptHash(address));
    }

    /**
     * Creates a script hash from the given script in byte array form.
     *
     * @param script the script to calculate the script hash for.
     * @return the script hash.
     */
    public static Hash160 fromScript(byte[] script) {
        // There is no need to reverse the hash. The hashing method returns the script hash in
        // little-endian format.
        return new Hash160(Hash.sha256AndThenRipemd160(script));
    }

    public static Hash160 fromPublicKey(byte[] encodedPublicKey) {
        return fromScript(ScriptBuilder.buildVerificationScript(encodedPublicKey));
    }

    public static Hash160 fromPublicKeys(List<byte[]> encodedPublicKeys, int signingThreshold) {
        return fromScript(ScriptBuilder.buildVerificationScript(encodedPublicKeys,
                signingThreshold));
    }

    /**
     * Creates a script hash from the given script in hexadecimal string form.
     *
     * @param script the script to calculate the script hash for.
     * @return the script hash.
     */
    public static Hash160 fromScript(String script) {
        return fromScript(Numeric.hexStringToByteArray(script));
    }

    private void checkAndThrowHashLength(byte[] hash) {
        if (hash.length != NeoConstants.HASH160_SIZE) {
            throw new IllegalArgumentException("Script hash must be " + NeoConstants.HASH160_SIZE +
                    " bytes long but was " + hash.length + " bytes.");
        }
    }

    @Override
    public int compareTo(Hash160 o) {
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
        Hash160 that = (Hash160) o;
        return Arrays.equals(hash, that.hash);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.hash);
    }

}
