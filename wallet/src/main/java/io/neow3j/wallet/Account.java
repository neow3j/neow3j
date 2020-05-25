package io.neow3j.wallet;

import io.neow3j.contract.ScriptHash;
import io.neow3j.crypto.Base64;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.crypto.NEP2;
import io.neow3j.crypto.ScryptParams;
import io.neow3j.crypto.WIF;
import io.neow3j.crypto.exceptions.CipherException;
import io.neow3j.crypto.exceptions.NEP2InvalidFormat;
import io.neow3j.crypto.exceptions.NEP2InvalidPassphrase;
import io.neow3j.model.types.ContractParameterType;
import io.neow3j.transaction.VerificationScript;
import io.neow3j.utils.Numeric;
import io.neow3j.wallet.exceptions.AccountStateException;
import io.neow3j.wallet.nep6.NEP6Account;
import io.neow3j.wallet.nep6.NEP6Contract;
import io.neow3j.wallet.nep6.NEP6Contract.NEP6Parameter;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@SuppressWarnings("unchecked")
public class Account {

    private ECKeyPair keyPair;
    private String address;
    private String encryptedPrivateKey;
    private String label;
    private boolean isDefault;
    private boolean isLocked;
    private VerificationScript verificationScript;

    private Account() {
    }

    protected Account(Builder b) {
        this.label = b.label;
        this.keyPair = b.keyPair;
        this.isDefault = b.isDefault;
        this.isLocked = b.isLocked;
        this.address = b.address;
        this.encryptedPrivateKey = b.encryptedPrivateKey;
        this.verificationScript = b.verificationScript;
    }

    public String getAddress() {
        return address;
    }

    public ScriptHash getScriptHash() {
        return ScriptHash.fromAddress(address);
    }

    /**
     * Gets this account's EC key pair if available.
     *
     * @return the key pair.
     */
    public ECKeyPair getECKeyPair() {
        return this.keyPair;
    }

    public ECKeyPair getKeyPair() {
        return this.keyPair;
    }

    public String getLabel() {
        return label;
    }

    public Boolean isDefault() {
        return isDefault;
    }

    // This method is required by the Wallet but must not be available to the developer because
    // it might bring a wallet into inconsistent state, i.e. having multiple default accounts.
    void setDefault() {
        this.isDefault = true;
    }

    // This method is required by the Wallet but must not be available to the developer because
    // it might bring a wallet into inconsistent state, i.e. having multiple default accounts.
    void unsetDefault() {
        this.isDefault = false;
    }

    public Boolean isLocked() {
        return isLocked;
    }

    public VerificationScript getVerificationScript() {
        return this.verificationScript;
    }

    public String getEncryptedPrivateKey() {
        return encryptedPrivateKey;
    }

    /**
     * Decrypts this account's private key, according to the NEP-2 standard, if not already
     * decrypted. Uses the default Scrypt parameters.
     *
     * @param password     The passphrase used to decrypt this account's private key.
     * @throws NEP2InvalidFormat     throws if the encrypted NEP2 has an invalid format.
     * @throws CipherException       throws if failed encrypt the created wallet.
     * @throws NEP2InvalidPassphrase throws if the passphrase is not valid.
     * @throws AccountStateException if
     *                               <ul>
     *                               <li>the account doesn't hold an encrypted private key
     *                               <li>the account does already hold a decrypted private key
     *                               <li>the public key derived from the decrypted private key is
     *                               not equal to the already set public key.
     *                               </ul>
     */
    public void decryptPrivateKey(String password)
            throws NEP2InvalidFormat, CipherException, NEP2InvalidPassphrase {
        decryptPrivateKey(password, NEP2.DEFAULT_SCRYPT_PARAMS);
    }
    /**
     * Decrypts this account's private key, according to the NEP-2 standard, if not already
     * decrypted.
     *
     * @param password     The passphrase used to decrypt this account's private key.
     * @param scryptParams The Scrypt parameters used for decryption.
     * @throws NEP2InvalidFormat     throws if the encrypted NEP2 has an invalid format.
     * @throws CipherException       throws if failed encrypt the created wallet.
     * @throws NEP2InvalidPassphrase throws if the passphrase is not valid.
     * @throws AccountStateException if
     *                               <ul>
     *                               <li>the account doesn't hold an encrypted private key
     *                               <li>the account does already hold a decrypted private key
     *                               <li>the public key derived from the decrypted private key is
     *                               not equal to the already set public key.
     *                               </ul>
     */
    public void decryptPrivateKey(String password, ScryptParams scryptParams)
            throws NEP2InvalidFormat, CipherException, NEP2InvalidPassphrase {

        if (this.keyPair != null) {
            return;
        }
        if (this.encryptedPrivateKey == null) {
            throw new AccountStateException("The account does not hold an encrypted private key.");
        }
        this.keyPair = NEP2.decrypt(password, this.encryptedPrivateKey, scryptParams);
    }

    /**
     * Encrypts this account's private key according to the NEP-2 standard using the default Scrypt
     * parameters.
     *
     * @param password The passphrase used to encrypt this account's private key.
     * @throws CipherException if failed encrypt the created wallet.
     */
    public void encryptPrivateKey(String password) throws CipherException {
        encryptPrivateKey(password, NEP2.DEFAULT_SCRYPT_PARAMS);
    }

    /**
     * Encrypts this account's private key according to the NEP-2 standard and
     *
     * @param password     The passphrase used to encrypt this account's private key.
     * @param scryptParams The Scrypt parameters used for encryption.
     * @throws CipherException if failed encrypt the created wallet.
     */
    public void encryptPrivateKey(String password, ScryptParams scryptParams)
            throws CipherException {

        if (this.keyPair == null) {
            throw new AccountStateException("The account does not hold a decrypted private key.");
        }
        this.encryptedPrivateKey = NEP2.encrypt(password, this.keyPair, scryptParams);
        // TODO 25.05.20 claude: Clarify if it is necessary to destroy the private key in a
        //  safer way than we are doing here.
        this.keyPair = null;
    }

    public boolean isMultiSig() {
        if (this.verificationScript == null) {
            throw new AccountStateException(
                    "This account does not have a verification script, which is "
                            + "needed to determine if it is a multi-sig account.");
        }
        return this.verificationScript.isMultiSigScript();
    }

    public NEP6Account toNEP6Account() {
        if (this.keyPair != null && this.encryptedPrivateKey == null) {
            throw new AccountStateException("Account private key is available but not encrypted.");
        }
        if (this.verificationScript == null) {
            return new NEP6Account(this.address, this.label, this.isDefault, this.isLocked,
                    this.encryptedPrivateKey, null, null);
        }
        List<NEP6Parameter> parameters = new ArrayList<>();
        if (this.verificationScript.isMultiSigScript()) {
            IntStream.range(0, this.verificationScript.getNrOfAccounts()).forEachOrdered(i -> {
                parameters.add(new NEP6Parameter("signature" + i, ContractParameterType.SIGNATURE));
            });
        } else if (this.verificationScript.isSingleSigScript()) {
            parameters.add(new NEP6Parameter("signature", ContractParameterType.SIGNATURE));
        }
        String script = Base64.encode(this.verificationScript.getScript());
        NEP6Contract contract = new NEP6Contract(script, parameters, false);
        return new NEP6Account(this.address, this.label, this.isDefault, this.isLocked,
                this.encryptedPrivateKey, contract, null);
    }

    /**
     * Creates a multi-sig account builder from the given public keys. Mind that the ordering of the
     * keys is important for later usage of the account.
     *
     * @param publicKeys         The public keys from which to derive the multi-sig account.
     * @param signatureThreshold The number of signatures needed when using this account for signing
     *                           transactions.
     * @return the multi-sig account builder;
     */
    public static Builder fromMultiSigKeys(List<ECPublicKey> publicKeys, int signatureThreshold) {
        VerificationScript script = new VerificationScript(publicKeys, signatureThreshold);
        String address = ScriptHash.fromScript(script.getScript()).toAddress();
        Builder b = new Builder();
        b.address = address;
        b.label = address;
        b.verificationScript = script;
        return b;
    }

    public static Builder fromWIF(String wif) {
        BigInteger privateKey = Numeric.toBigInt(WIF.getPrivateKeyFromWIF(wif));
        ECKeyPair keyPair = ECKeyPair.create(privateKey);
        Builder b = new Builder();
        b.keyPair = keyPair;
        b.address = keyPair.getAddress();
        b.label = keyPair.getAddress();
        b.verificationScript = new VerificationScript(keyPair.getPublicKey());
        return b;
    }

    public static Builder fromNewECKeyPair() {
        try {
            return fromECKeyPair(ECKeyPair.createEcKeyPair());
        } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException("Failed to create a new EC key pair.", e);
        }
    }

    public static Builder fromECKeyPair(ECKeyPair ecKeyPair) {
        Builder b = new Builder();
        b.keyPair = ecKeyPair;
        b.address = ecKeyPair.getAddress();
        b.label = b.address;
        b.verificationScript = new VerificationScript(ecKeyPair.getPublicKey());
        return b;
    }

    public static Builder fromNEP6Account(NEP6Account nep6Acct) {
        Builder b = new Builder();
        b.address = nep6Acct.getAddress();
        b.label = nep6Acct.getLabel();
        b.encryptedPrivateKey = nep6Acct.getKey();
        b.isLocked = nep6Acct.getLock();
        b.isDefault = nep6Acct.getDefault();
        NEP6Contract contr = nep6Acct.getContract();
        if (contr != null && contr.getScript() != null && !contr.getScript().isEmpty()) {
            byte[] script = Base64.decode(contr.getScript());
            b.verificationScript = new VerificationScript(script);
        }
        return b;
    }

    public static Builder fromAddress(String address) {
        Builder b = new Builder();
        b.address = address;
        b.label = address;
        return b;
    }

    /**
     * Creates a new generic account with a fresh key pair.
     *
     * @return the new account.
     */
    public static Account createAccount() {
        return fromNewECKeyPair().build();
    }

    public static class Builder<T extends Account, B extends Builder<T, B>> {

        VerificationScript verificationScript;
        String label;
        ECKeyPair keyPair;
        boolean isDefault;
        boolean isLocked;
        String address;
        String encryptedPrivateKey;

        protected Builder() {
            isDefault = false;
            isLocked = false;
        }

        public B label(String label) {
            this.label = label;
            return (B) this;
        }

        public B isDefault() {
            this.isDefault = true;
            return (B) this;
        }

        public B isLocked() {
            this.isLocked = true;
            return (B) this;
        }

        public T build() {
            return (T) new Account(this);
        }
    }
}
