package io.neow3j.wallet;

/**
 * Data class encapsulating a BIP-39 compatible NEO wallet.
 */
public class Bip39Wallet {

    /**
     * Path to wallet file.
     */
    private final String filename;

    /**
     * Generated BIP-39 mnemonic for the wallet.
     */
    private final String mnemonic;

    public Bip39Wallet(String filename, String mnemonic) {
        this.filename = filename;
        this.mnemonic = mnemonic;
    }

    public String getFilename() {
        return filename;
    }

    public String getMnemonic() {
        return mnemonic;
    }

    @Override
    public String toString() {
        return "Bip39Wallet{"
                + "filename='" + filename + '\''
                + ", mnemonic='" + mnemonic + '\''
                + '}';
    }
}
