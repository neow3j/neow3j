package io.neow3j.utils;

import io.neow3j.constants.NeoConstants;
import io.neow3j.crypto.Base58;
import io.neow3j.crypto.Hash;
import io.neow3j.crypto.exceptions.AddressFormatException;

public class AddressUtils {

    /**
     * Checks whether the give address is valid or not. It uses the default address version {@link
     * NeoConstants#DEFAULT_ADDRESS_VERSION}.
     *
     * @param address The address to be checked.
     * @return whether the address is valid or not
     */
    public static boolean isValidAddress(String address) {
        return isValidAddress(address, NeoConstants.DEFAULT_ADDRESS_VERSION);
    }

    /**
     * Checks whether the give address is valid or not. It uses the default address version {@link
     * NeoConstants#DEFAULT_ADDRESS_VERSION}.
     *
     * @param address        The address to be checked.
     * @param addressVersion The address version.
     * @return whether the address is valid or not
     */
    public static boolean isValidAddress(String address, byte addressVersion) {
        byte[] data;
        try {
            data = Base58.decode(address);
        } catch (AddressFormatException e) {
            return false;
        }
        if (data.length != 25) {
            return false;
        }
        if (data[0] != addressVersion) {
            return false;
        }
        byte[] checksum = Hash.sha256(Hash.sha256(data, 0, 21));
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
     * @param address The address.
     * @return the script hash byte array in little-endian order.
     */
    public static byte[] addressToScriptHash(String address) {
        if (!isValidAddress(address)) {
            throw new IllegalArgumentException("Not a valid NEO address.");
        }
        byte[] data = Base58.decode(address);
        byte[] buffer = new byte[20];
        System.arraycopy(data, 1, buffer, 0, 20);
        return buffer;
    }

    /**
     * Derives the Neo address from the given script hash. It uses the default address version
     * {@link NeoConstants#DEFAULT_ADDRESS_VERSION}.
     * <p>
     * The script hash needs to be in little-endian order.
     *
     * @param scriptHash The script hash to get the address for.
     * @return the address
     */
    public static String scriptHashToAddress(byte[] scriptHash) {
        return scriptHashToAddress(scriptHash, NeoConstants.DEFAULT_ADDRESS_VERSION);
    }

    /**
     * Derives the Neo address from the given script hash, also specifying the address version. Use
     * {@link #scriptHashToAddress} if the default address version should be used.
     * <p>
     * The script hash needs to be in little-endian order.
     *
     * @param scriptHash The script hash to get the address for.
     * @return the address
     */
    public static String scriptHashToAddress(byte[] scriptHash, byte addressVersion) {
        byte[] script = ArrayUtils.concatenate(addressVersion, scriptHash);
        byte[] checksum = ArrayUtils.getFirstNBytes(Hash.sha256(Hash.sha256(script)), 4);
        return Base58.encode(ArrayUtils.concatenate(script, checksum));
    }
}
