package io.neow3j.utils;

import static io.neow3j.crypto.Hash.hash256;
import static io.neow3j.protocol.Neow3jConfig.getAddressVersion;
import static io.neow3j.utils.ArrayUtils.concatenate;
import static io.neow3j.utils.ArrayUtils.getFirstNBytes;
import static io.neow3j.utils.ArrayUtils.reverseArray;

import io.neow3j.crypto.Base58;
import io.neow3j.crypto.exceptions.AddressFormatException;
import io.neow3j.protocol.Neow3jConfig;

public class AddressUtils {

    /**
     * Checks whether the given string is a valid Neo address.
     * <p>
     * Make sure that the address version used in the address is the same as the one configured in
     * {@link Neow3jConfig#setAddressVersion(byte)}.
     *
     * @param address the address to be checked.
     * @return whether the address is valid or not.
     */
    public static boolean isValidAddress(String address) {
        byte[] data;
        try {
            data = Base58.decode(address);
        } catch (AddressFormatException e) {
            return false;
        }
        if (data.length != 25) {
            return false;
        }
        if (data[0] != getAddressVersion()) {
            return false;
        }
        byte[] checksum = hash256(data, 0, 21);
        for (int i = 0; i < 4; i++) {
            if (data[data.length - 4 + i] != checksum[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Transforms the given address into its script hash.
     *
     * @param address the address.
     * @return the script hash byte array in big-endian order.
     */
    public static byte[] addressToScriptHash(String address) {
        if (!isValidAddress(address)) {
            throw new IllegalArgumentException("Not a valid NEO address.");
        }
        byte[] data = Base58.decode(address);
        byte[] buffer = new byte[20];
        System.arraycopy(data, 1, buffer, 0, 20);
        return reverseArray(buffer);
    }

    /**
     * Derives the Neo address from the given script hash.
     * <p>
     * Make sure that the address version configured in {@link Neow3jConfig#getAddressVersion()} matches the
     * blockchain network you are intending the address for.
     *
     * @param scriptHash the script hash in big-endian order.
     * @return the address
     */
    public static String scriptHashToAddress(byte[] scriptHash) {
        byte[] script = concatenate(getAddressVersion(), reverseArray(scriptHash));
        byte[] checksum = getFirstNBytes(hash256(script), 4);
        return Base58.encode(concatenate(script, checksum));
    }

}
