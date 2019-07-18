package io.neow3j.wallet;

import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.MnemonicUtils;
import io.neow3j.crypto.SecureRandomUtils;

import static io.neow3j.crypto.Hash.sha256;

/**
 * Class encapsulating a BIP-39 compatible NEO account.
 */
public class Bip39Account extends Account {

    /**
     * Generated BIP-39 mnemonic for the account.
     */
    private String mnemonic;

    protected Bip39Account(Builder builder) {
        super(builder);
        this.mnemonic = builder.mnemonic;
    }

    /**
     * Generates a BIP-39 compatible NEO account. The private key for the wallet can
     * be calculated using following algorithm:
     * <pre>
     *     Key = SHA-256(BIP_39_SEED(mnemonic, password))
     * </pre>
     *
     * @param password Will be *only* used as passphrase for BIP-39 seed (i.e., used to recover the account).
     * @return A BIP-39 compatible NEO account.
     */
    public static Bip39Account createAccount(final String password) {
        byte[] initialEntropy = SecureRandomUtils.generateRandomBytes(16);

        String mnemonic = MnemonicUtils.generateMnemonic(initialEntropy);
        byte[] seed = MnemonicUtils.generateSeed(mnemonic, password);
        ECKeyPair keyPair = ECKeyPair.create(sha256(seed));

        return fromECKeyPair(keyPair)
                .isDefault(true)
                .mnemonic(mnemonic)
                .build();
    }

    /**
     * Recovers a key pair based on BIP-39 mnemonic and password.
     *
     * @param password passphrase given when the BIP-39 account was generated.
     * @param mnemonic the generated mnemonic with the given passphrase.
     * @return a Bip39Account builder.
     */
    public static Builder fromBip39Mnemonic(String password, String mnemonic) {
        byte[] seed = MnemonicUtils.generateSeed(mnemonic, password);
        ECKeyPair ecKeyPair = ECKeyPair.create(sha256(seed));
        return fromECKeyPair(ecKeyPair);
    }

    public static Builder fromECKeyPair(ECKeyPair ecKeyPair) {
        Builder b = new Builder();
        b.privateKey = ecKeyPair.getPrivateKey();
        b.publicKey = ecKeyPair.getPublicKey();
        b.address = ecKeyPair.getAddress();
        b.label = b.address;
        return b;
    }

    public String getMnemonic() {
        return mnemonic;
    }

    public static class Builder extends Account.Builder<Bip39Account, Builder> {

        String mnemonic;

        protected Builder() {
        }

        public Builder mnemonic(String mnemonic) {
            this.mnemonic = mnemonic;
            return this;
        }

        public Bip39Account build() {
            return new Bip39Account(this);
        }
    }

    @Override
    public String toString() {
        return "Bip39Account{" +
                "privateKey=" + getPrivateKey() +
                ", publicKey=" + getPublicKey() +
                ", address='" + getAddress() + '\'' +
                ", encryptedPrivateKey='" + getEncryptedPrivateKey() + '\'' +
                ", label='" + getLabel() + '\'' +
                ", isDefault=" + isDefault() +
                ", isLocked=" + isLocked() +
                ", contract=" + getContract() +
                ", balances=" + getBalances() +
                ", mnemonic='" + mnemonic + '\'' +
                '}';
    }
}
