package io.neow3j.contract;

import io.neow3j.constants.NeoConstants;
import io.neow3j.crypto.Base58;
import io.neow3j.crypto.Hash;
import io.neow3j.utils.ArrayUtils;
import io.neow3j.utils.Keys;
import io.neow3j.utils.Numeric;

import java.math.BigInteger;
import java.util.Arrays;

public class ScriptHash implements Comparable<ScriptHash> {

    /**
     * The script hash is stored as an unsigned integer in little-endian order.
     */
    private byte[] scriptHash;

    /**
     * Constructs a new script hash from the given byte array. The array must represent the
     * script hash in little-endian order and can be 160 (contract script hash) or 256
     * (global asset id) bits long.
     *
     * @param scriptHash The script hash in little-endian order.
     */
    public ScriptHash(byte[] scriptHash) {
        checkAndThrowHashLength(scriptHash);
        this.scriptHash = scriptHash;
    }

    /**
     * Constructs a new script hash from the given hexadecimal string. The string must
     * represent the script hash in big-endian order and can be 160 (contract script hash) or 256
     * (global asset id) bits long.
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

    /**
     * Gets the length of the script hash byte array.
     *
     * @return the length.
     */
    public int length() {
        return scriptHash.length;
    }

    /**
     * Gets the script hash as a byte array in little-endian order.
     *
     * @return the script hash byte array in little-endian order.
     */
    public byte[] toArray() {
        return this.scriptHash;
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
     * Derives the address corresponding to this script hash.
     *
     * @return the address.
     */
    public String toAddress() {
        byte[] data = new byte[1];
        data[0] = NeoConstants.COIN_VERSION;
        byte[] dataAndScriptHash = ArrayUtils.concatenate(data, scriptHash);
        byte[] checksum = Hash.sha256(Hash.sha256(dataAndScriptHash));
        byte[] first4BytesCheckSum = new byte[4];
        System.arraycopy(checksum, 0, first4BytesCheckSum, 0, 4);
        byte[] dataToEncode = ArrayUtils.concatenate(dataAndScriptHash, first4BytesCheckSum);
        return Base58.encode(dataToEncode);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
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
        if (!Keys.isValidAddress(address)) {
            throw new IllegalArgumentException("Not a valid NEO address.");
        }
        byte[] buffer = new byte[20];
        System.arraycopy(Base58.decode(address), 1, buffer, 0, 20);
        return new ScriptHash(buffer);
    }

    /**
     * Creates a script hash from the given public key.
     * <p>
     * TODO 29.07.19 claude: What form does the public key need to have?
     *
     * @param publicKey The key to calculate the script hash for.
     * @return the script hash.
     */
    public static ScriptHash fromPublicKey(byte[] publicKey) {
        return fromScript(Keys.getVerificationScriptFromPublicKey(publicKey));
    }

    /**
     * <p>Creates a script hash from the given public keys and signing threshold.</p>
     * <br>
     * <p>The signing threshold is the number of signatures needed for a valid transaction created
     * with the public keys. It is needed to create the proper verification script.</p>
     *
     * @param signingThreshold The signing threshold.
     * @param publicKeys The public keys.
     * @return the script hash.
     */
    public static ScriptHash fromPublicKeys(int signingThreshold, byte[]... publicKeys) {
        byte[] verificationScript = Keys.getVerificationScriptFromPublicKeys(signingThreshold, publicKeys);
        return fromScript(verificationScript);
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
        if (scriptHash.length != NeoConstants.SCRIPTHASH_LENGHT_BYTES &&
                scriptHash.length != NeoConstants.ASSET_ID_LENGHT_BYTES) {

            throw new IllegalArgumentException("Script hash must be either " +
                    NeoConstants.SCRIPTHASH_LENGHT_BYTES + " or " + NeoConstants.ASSET_ID_LENGHT_BYTES +
                    " bytes long, but was " + scriptHash.length);
        }
    }

    @Override
    public int compareTo(ScriptHash o) {
        return new BigInteger(1, ArrayUtils.reverseArray(scriptHash))
                .compareTo(new BigInteger(1, ArrayUtils.reverseArray(o.toArray())));
    }
}
