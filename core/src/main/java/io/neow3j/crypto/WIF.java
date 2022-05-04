package io.neow3j.crypto;

import io.neow3j.constants.NeoConstants;
import io.neow3j.utils.ArrayUtils;

import java.util.Arrays;

import static io.neow3j.crypto.Hash.hash256;
import static java.lang.String.format;

/**
 * Based on the <a href="https://en.bitcoin.it/wiki/Wallet_import_format">Bitcoin documentation</a>.
 */
public class WIF {

    public static byte[] getPrivateKeyFromWIF(String wif) {
        if (wif == null) {
            throw new NullPointerException();
        }

        byte[] data = Base58.decode(wif);

        if (data.length != 38 || data[0] != (byte) 0x80 || data[33] != 0x01) {
            throw new IllegalArgumentException("Incorrect WIF format.");
        }

        byte[] checksum = hash256(data, 0, data.length - 4);

        for (int i = 0; i < 4; i++) {
            if (data[data.length - 4 + i] != checksum[i]) {
                throw new IllegalArgumentException("Incorrect WIF checksum.");
            }
        }

        byte[] privateKey = new byte[32];
        System.arraycopy(data, 1, privateKey, 0, privateKey.length);
        Arrays.fill(data, (byte) 0);
        return privateKey;
    }

    public static String getWIFFromPrivateKey(byte[] key) {
        if (key.length != NeoConstants.PRIVATE_KEY_SIZE) {
            throw new IllegalArgumentException(
                    format("Given key is not of expected length (%s bytes).", NeoConstants.PRIVATE_KEY_SIZE));
        }

        byte[] extendenKey = ArrayUtils.concatenate(ArrayUtils.concatenate((byte) 0x80, key), (byte) 0x01);
        byte[] hash = hash256(extendenKey);
        byte[] checksum = ArrayUtils.getFirstNBytes(hash, 4);
        return Base58.encode(ArrayUtils.concatenate(extendenKey, checksum));
    }

}
