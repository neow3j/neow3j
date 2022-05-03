package io.neow3j.wallet;

import static io.neow3j.crypto.Hash.sha256;

import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.MnemonicUtils;
import io.neow3j.crypto.SecureRandomUtils;

/**
 * Class encapsulating a BIP-39 compatible NEO account.
 */
public class Bip39Account extends Account {

    /**
     * Generated BIP-39 mnemonic for the account.
     */
    private String mnemonic;

    private Bip39Account() {
    }

    protected Bip39Account(String mnemonic) {
        super();
        this.mnemonic = mnemonic;
    }

    public Bip39Account(ECKeyPair ecKeyPair) {
        super(ecKeyPair);
    }

    private Bip39Account mnemonic(String mnemonic) {
        this.mnemonic = mnemonic;
        return this;
    }

    /**
     * Generates a BIP-39 compatible NEO account. The private key for the wallet can be calculated using following
     * algorithm:
     * <pre>
     *     Key = SHA-256(BIP_39_SEED(mnemonic, password))
     * </pre>
     * <p>
     * The password will *only* be used as passphrase for BIP-39 seed (i.e., used to recover the account).
     *
     * @param password the passphrase with which to encrypt the private key.
     * @return a BIP-39 compatible Neo account.
     */
    public static Bip39Account create(final String password) {
        byte[] initialEntropy = SecureRandomUtils.generateRandomBytes(16);

        String mnemonic = MnemonicUtils.generateMnemonic(initialEntropy);
        byte[] seed = MnemonicUtils.generateSeed(mnemonic, password);
        ECKeyPair keyPair = ECKeyPair.create(sha256(seed));

        return new Bip39Account(keyPair).mnemonic(mnemonic);
    }

    /**
     * Recovers a key pair based on BIP-39 mnemonic and password.
     *
     * @param password the passphrase given when the BIP-39 account was generated.
     * @param mnemonic the generated mnemonic with the given passphrase.
     * @return a Bip39Account builder.
     */
    public static Bip39Account fromBip39Mnemonic(String password, String mnemonic) {
        byte[] seed = MnemonicUtils.generateSeed(mnemonic, password);
        ECKeyPair ecKeyPair = ECKeyPair.create(sha256(seed));
        return new Bip39Account(ecKeyPair).mnemonic(mnemonic);
    }

    public String getMnemonic() {
        return mnemonic;
    }

}
