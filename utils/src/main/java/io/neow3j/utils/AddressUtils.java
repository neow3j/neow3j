package io.neow3j.utils;

import io.neow3j.constants.NeoConstants;
import io.neow3j.crypto.Base58;
import io.neow3j.crypto.Hash;
import io.neow3j.crypto.exceptions.AddressFormatException;

public class AddressUtils {

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
        if (data[0] != NeoConstants.COIN_VERSION) {
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
     * Derives the Neo address from the given script hash.
     * <p>
     * The script hash needs to be in little-endian order.
     *
     * @param scriptHash The script hash to get the address for.
     * @return the address
     */
    public static String scriptHashToAddress(byte[] scriptHash) {
        byte[] data = new byte[1];
        data[0] = NeoConstants.COIN_VERSION;
        byte[] dataAndScriptHash = ArrayUtils.concatenate(data, scriptHash);
        byte[] checksum = Hash.sha256(Hash.sha256(dataAndScriptHash));
        byte[] first4BytesCheckSum = new byte[4];
        System.arraycopy(checksum, 0, first4BytesCheckSum, 0, 4);
        byte[] dataToEncode = ArrayUtils.concatenate(dataAndScriptHash, first4BytesCheckSum);
        return Base58.encode(dataToEncode);
    }
}
