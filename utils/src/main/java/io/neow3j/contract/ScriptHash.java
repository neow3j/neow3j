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
 * A script hash is as its name says the hash of a executable NeoVM script. It is always 20 bytes
 * long and is created by hashing a script with SHA256 and then RIPEMD160
 */
public class ScriptHash extends NeoSerializable implements Comparable<ScriptHash> {

    /**
     * The script hash is stored as an unsigned integer in little-endian order.
     */
    private byte[] scriptHash;

    /**
     * Constructs a new script hash with 20 zero bytes.
     */
    public ScriptHash() {
        this.scriptHash = new byte[NeoConstants.SCRIPTHASH_LENGHT_BYTES];
    }

    /**
     * Constructs a new script hash from the given byte array. The array must represent the script
     * hash in little-endian order and can be 160 (contract script hash) or 256 (global asset id)
     * bits long.
     *
     * @param scriptHash The script hash in little-endian order.
     */
    public ScriptHash(byte[] scriptHash) {
        checkAndThrowHashLength(scriptHash);
        this.scriptHash = scriptHash;
    }

    /**
     * Constructs a new script hash from the given hexadecimal string. The string must represent the
     * script hash in big-endian order and can be 160 (contract script hash) or 256 (global asset
     * id) bits long.
     *
     * @param scriptHash The script hash in big-endian order.
     */
    public ScriptHash(String scriptHash) {
        if (Numeric.isValidHexString(scriptHash)) {
            this.scriptHash = ArrayUtils.reverseArray(Numeric.hexStringToByteArray(scriptHash));
            checkAndThrowHashLength(this.scriptHash);
        } else {
            throw new IllegalArgumentException("String argument is not hexadecimal.");
        }
    }

    @Override
    public void deserialize(BinaryReader reader) throws DeserializationException {
        try {
            this.scriptHash = reader.readBytes(NeoConstants.SCRIPTHASH_LENGHT_BYTES);
        } catch (IOException e) {
            throw new DeserializationException(e);
        }
    }

    @Override
    public void serialize(BinaryWriter writer) throws IOException {
        writer.write(this.scriptHash);
    }

    @Override
    public int getSize() {
        return NeoConstants.SCRIPTHASH_LENGHT_BYTES;
    }

    /**
     * Gets the script hash as a byte array in little-endian order.
     *
     * @return the script hash byte array in little-endian order.
     */
    public byte[] toArray() {
        return super.toArray();
    }

    /**
     * Gets the script hash as a hexadecimal string in big-endian order without the '0x' prefix.
     *
     * @return the script hash as hex string in big-endian order.
     */
    public String toString() {
        return Numeric.toHexStringNoPrefix(ArrayUtils.reverseArray(scriptHash));
    }

    /**
     * Derives the address corresponding to this script hash, specifying the address version.
     *
     * @return the address.
     */
    public String toAddress(byte addressVersion) {
        return AddressUtils.scriptHashToAddress(this.scriptHash, addressVersion);
    }

    /**
     * Derives the address corresponding to this script hash. It uses the default address version
     * {@link NeoConstants#DEFAULT_ADDRESS_VERSION}
     *
     * @return the address.
     */
    public String toAddress() {
        return toAddress(NeoConstants.DEFAULT_ADDRESS_VERSION);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ScriptHash that = (ScriptHash) o;
        return Arrays.equals(scriptHash, that.scriptHash);
    }

    /**
     * Creates a script hash from the given address.
     *
     * @param address The address from which to derive the script hash.
     * @return the script hash.
     */
    public static ScriptHash fromAddress(String address) {
        return new ScriptHash(AddressUtils.addressToScriptHash(address));
    }

    /**
     * Creates a script hash from the given script in byte array form.
     *
     * @param script The script to calculate the script hash for.
     * @return the script hash.
     */
    public static ScriptHash fromScript(byte[] script) {
        // There is no need to reverse the hash. The hashing method returns the script hash in
        // little-endian format.
        return new ScriptHash(Hash.sha256AndThenRipemd160(script));
    }

    public static ScriptHash fromPublicKey(byte[] encodedPublicKey) {
        return fromScript(ScriptBuilder.buildVerificationScript(encodedPublicKey));
    }

    public static ScriptHash fromPublicKeys(List<byte[]> encodedPublicKeys, int signingThreshold) {
        return fromScript(ScriptBuilder.buildVerificationScript(encodedPublicKeys, signingThreshold));
    }

    /**
     * Creates a script hash from the given script in hexadecimal string form.
     *
     * @param script The script to calculate the script hash for.
     * @return the script hash.
     */
    public static ScriptHash fromScript(String script) {
        return fromScript(Numeric.hexStringToByteArray(script));
    }

    private void checkAndThrowHashLength(byte[] scriptHash) {
        if (scriptHash.length != NeoConstants.SCRIPTHASH_LENGHT_BYTES) {
            throw new IllegalArgumentException("Script hash must be " +
                NeoConstants.SCRIPTHASH_LENGHT_BYTES + " bytes long but was " + scriptHash.length +
                " bytes.");
        }
    }

    @Override
    public int compareTo(ScriptHash o) {
        return new BigInteger(1, ArrayUtils.reverseArray(scriptHash))
            .compareTo(new BigInteger(1, ArrayUtils.reverseArray(o.toArray())));
    }
}
