package io.neow3j.wallet;

import io.neow3j.contract.ScriptHash;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.ECKeyPair.ECPrivateKey;
import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.crypto.NEP2;
import io.neow3j.crypto.ScryptParams;
import io.neow3j.crypto.WIF;
import io.neow3j.crypto.exceptions.CipherException;
import io.neow3j.crypto.exceptions.NEP2InvalidFormat;
import io.neow3j.crypto.exceptions.NEP2InvalidPassphrase;
import io.neow3j.model.types.ContractParameterType;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.exceptions.ErrorResponseException;
import io.neow3j.transaction.VerificationScript;
import io.neow3j.utils.Numeric;
import io.neow3j.wallet.exceptions.AccountStateException;
import io.neow3j.wallet.nep6.NEP6Account;
import io.neow3j.wallet.nep6.NEP6Contract;
import io.neow3j.wallet.nep6.NEP6Contract.NEP6Parameter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import org.java_websocket.util.Base64;

@SuppressWarnings("unchecked")
public class Account {

    // Private and public key are stored separately because the private key is not necessarily
    // available in every account instance.
    private ECPrivateKey privateKey;
    private ECPublicKey publicKey;
    private String address;
    private String encryptedPrivateKey;
    private String label;
    private boolean isDefault;
    private boolean isLocked;
    private VerificationScript verificationScript;
    private Map<ScriptHash, BigDecimal> balances;

    private Account() {
    }

    protected Account(Builder b) {
        this.label = b.label;
        this.privateKey = b.privateKey;
        this.publicKey = b.publicKey;
        this.isDefault = b.isDefault;
        this.isLocked = b.isLocked;
        this.address = b.address;
        this.encryptedPrivateKey = b.encryptedPrivateKey;
        this.verificationScript = b.verificationScript;
        this.balances = new HashMap<>();
    }

    public String getAddress() {
        return address;
    }

    public ScriptHash getScriptHash() {
        return ScriptHash.fromAddress(address);
    }

    public ECKeyPair getECKeyPair() {
        if (this.privateKey != null && this.publicKey != null) {
            return new ECKeyPair(this.privateKey, this.publicKey);
        } else if (privateKey != null) {
            return ECKeyPair.create(privateKey);
        } else {
            throw new AccountStateException("Account does not hold a decrypted private key.");
        }
    }

    /**
     * Gets this account's EC private key.
     *
     * @return The private key.
     */
    public ECPrivateKey getPrivateKey() {
        return this.privateKey;
    }

    /**
     * Gets this account's EC public key.
     *
     * @return The public key.
     */
    public ECPublicKey getPublicKey() {
        return this.publicKey;
    }

    public String getLabel() {
        return label;
    }

    public Boolean isDefault() {
        return isDefault;
    }

    void setIsDefault(boolean isDefault) {
        this.isDefault = isDefault;
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

    public Map<ScriptHash, BigDecimal> getBalances() {
        return balances;
    }

    public BigDecimal getBalance(ScriptHash token) {
        return balances.get(token);
    }

    public void updateAssetBalances(Neow3j neow3j) throws IOException, ErrorResponseException {
    }

    public void updateTokenBalances(Neow3j neow3j) throws IOException, ErrorResponseException {
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

        // TODO: Remove this check and just return without doing anything in this case.
        if (this.privateKey != null) {
            throw new AccountStateException(
                    "The account does already hold a decrypted private key.");
        }
        if (this.encryptedPrivateKey == null) {
            throw new AccountStateException("The account does not hold an encrypted private key.");
        }
        ECKeyPair ecKeyPair = NEP2.decrypt(password, this.encryptedPrivateKey, scryptParams);
        this.privateKey = ecKeyPair.getPrivateKey();
        if (this.publicKey != null && !this.publicKey.equals(ecKeyPair.getPublicKey())) {
            throw new AccountStateException(
                    "The public key derived from the decrypted private key does "
                            + "not equal the public key that was already set on the account.");
        }
        publicKey = ecKeyPair.getPublicKey();
    }

    /**
     * Encrypts this account's private key according to the NEP-2 standard and
     *
     * @param password     The passphrase used to encrypt this account's private key.
     * @param scryptParams The Scrypt parameters used for encryption.
     * @throws CipherException throws if failed encrypt the created wallet.
     */
    public void encryptPrivateKey(String password, ScryptParams scryptParams)
            throws CipherException {

        if (privateKey == null) {
            throw new AccountStateException("The account does not hold a decrypted private key.");
        }
        this.encryptedPrivateKey = NEP2.encrypt(password, getECKeyPair(), scryptParams);
        // TODO: 2019-07-14 Guil:
        // Is it the safest way of overwriting a variable on the JVM?
        // I don't think so. ;-)
        this.privateKey = null;
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
        if (encryptedPrivateKey == null) {
            throw new AccountStateException("Private key is not encrypted. Encrypt private key "
                    + "first.");
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
        String script = Base64.encodeBytes(this.verificationScript.getScript());
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
        b.privateKey = keyPair.getPrivateKey();
        b.publicKey = keyPair.getPublicKey();
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
        b.privateKey = ecKeyPair.getPrivateKey();
        b.publicKey = ecKeyPair.getPublicKey();
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
            try {
                byte[] script = Base64.decode(contr.getScript());
                b.verificationScript = new VerificationScript(script);
            } catch (IOException e) {
                // Will not happen because no I/O is going on.
            }
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
        ECPrivateKey privateKey;
        ECPublicKey publicKey;
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

        public B isDefault(boolean isDefault) {
            this.isDefault = isDefault;
            return (B) this;
        }

        public B isLocked(boolean isLocked) {
            this.isLocked = isLocked;
            return (B) this;
        }

        public T build() {
            return (T) new Account(this);
        }
    }
}
