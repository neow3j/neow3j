package io.neow3j.crypto;

import java.util.Arrays;

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
            throw new IllegalArgumentException();
        }

        byte[] checksum = Hash.sha256(Hash.sha256(data, 0, data.length - 4));

        for (int i = 0; i < 4; i++) {
            if (data[data.length - 4 + i] != checksum[i]) {
                throw new IllegalArgumentException();
            }
        }

        byte[] privateKey = new byte[32];
        System.arraycopy(data, 1, privateKey, 0, privateKey.length);
        Arrays.fill(data, (byte) 0);
        return privateKey;
    }

}
