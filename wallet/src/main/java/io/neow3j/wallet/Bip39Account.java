package io.neow3j.wallet;

import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.MnemonicUtils;
import io.neow3j.crypto.SecureRandomUtils;
import io.neow3j.crypto.exceptions.CipherException;

import java.security.SecureRandom;

import static io.neow3j.crypto.Hash.sha256;

/**
 * Data class encapsulating a BIP-39 compatible NEO account.
 */
public class Bip39Account extends Account {

    private static final SecureRandom secureRandom = SecureRandomUtils.secureRandom();

    /**
     * Generated BIP-39 mnemonic for the wallet.
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
     * @param password Will be *only* used as passphrase for BIP-39 seed.
     * @return A BIP-39 compatible NEO account.
     */
    public static Bip39Account createBip39Account(final String password) throws CipherException {
        byte[] initialEntropy = new byte[16];
        secureRandom.nextBytes(initialEntropy);

        String mnemonic = MnemonicUtils.generateMnemonic(initialEntropy);
        byte[] seed = MnemonicUtils.generateSeed(mnemonic, password);
        ECKeyPair keyPair = ECKeyPair.create(sha256(seed));

        Account.Builder accountBuilder = fromECKeyPair(keyPair).isDefault(true);
        Bip39Account bip39Account = new Builder()
                .accountBuilder(accountBuilder)
                .mnemonic(mnemonic)
                .build();
        return bip39Account;
    }

    public static Builder fromBip39Mnemonic(String password, String mnemonic) {
        byte[] seed = MnemonicUtils.generateSeed(mnemonic, password);
        ECKeyPair ecKeyPair = ECKeyPair.create(sha256(seed));
        Account.Builder accountBuilder = fromECKeyPair(ecKeyPair);
        return new Builder().accountBuilder(accountBuilder);
    }

    public String getMnemonic() {
        return mnemonic;
    }

    protected static class Builder extends Account.Builder<Bip39Account, Builder> {

        private String mnemonic;

        protected Builder() {
        }

        protected Builder accountBuilder(Account.Builder builder) {
            this.address = builder.address;
            this.contract = builder.contract;
            this.encryptedPrivateKey = builder.encryptedPrivateKey;
            this.isDefault = builder.isDefault;
            this.isLocked = builder.isLocked;
            this.label = builder.label;
            this.privateKey = builder.privateKey;
            this.publicKey = builder.publicKey;
            return this;
        }

        public Builder mnemonic(String mnemonic) {
            this.mnemonic = mnemonic;
            return this;
        }

        public Bip39Account build() {
            return new Bip39Account(this);
        }
    }

}
