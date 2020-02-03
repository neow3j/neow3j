package io.neow3j.wallet;

import io.neow3j.contract.ScriptHash;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.crypto.NEP2;
import io.neow3j.crypto.ScryptParams;
import io.neow3j.crypto.WIF;
import io.neow3j.crypto.exceptions.CipherException;
import io.neow3j.crypto.exceptions.NEP2InvalidFormat;
import io.neow3j.crypto.exceptions.NEP2InvalidPassphrase;
import io.neow3j.model.types.ContractParameterType;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.methods.response.NeoGetNep5Balances;
import io.neow3j.protocol.core.methods.response.NeoGetUnspents;
import io.neow3j.protocol.exceptions.ErrorResponseException;
import io.neow3j.transaction.VerificationScript;
import io.neow3j.utils.Numeric;
import io.neow3j.wallet.Balances.AssetBalance;
import io.neow3j.wallet.exceptions.AccountException;
import io.neow3j.wallet.exceptions.InsufficientFundsException;
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

@SuppressWarnings("unchecked")
public class Account {

    // Private and public key are stored separately because the private key is not necessarily
    // available in every account instance.
    private BigInteger privateKey;
    private ECPublicKey publicKey;
    private String address;
    private String encryptedPrivateKey;
    private String label;
    private boolean isDefault;
    private boolean isLocked;
    private VerificationScript verificationScript;
    private Balances balances;

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
        this.balances = new Balances(this);
    }

    public String getAddress() {
        return address;
    }

    public ScriptHash getScriptHash() {
        return ScriptHash.fromAddress(address);
    }

    public ECKeyPair getECKeyPair() {
        if (privateKey != null && publicKey != null) {
            return new ECKeyPair(privateKey, publicKey);
        } else if (privateKey != null) {
            return ECKeyPair.create(privateKey);
        } else {
            throw new AccountException("Account does not hold a decrypted private key.");
        }
    }

    public BigInteger getPrivateKey() {
        return this.privateKey;
    }

    // TODO: Remove this method and replace all occurrences with the method below.
    public BigInteger getPublicKey() {
        return Numeric.toBigInt(this.publicKey.getEncoded(true));
    }

    // TODO: Rename as soon as the above method has been removed.
    public ECPublicKey getPublicKey2() {
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

    public Balances getBalances() {
        return balances;
    }

    /**
     * <p>Gets the balance (the amount and a set of UTXOs) for the given asset id.</p>
     * <br>
     * <p>Note that updating the balance information via a call to a RPC node is left to the
     * library user. Call {@link Account#updateAssetBalances(Neow3j)} to have the most recent
     * balance information</p>
     *
     * @param assetId The id/hash of the asset.
     * @return the asset balance of this account.
     */
    public AssetBalance getAssetBalance(String assetId) {
        return this.balances.getAssetBalance(assetId);
    }

    public void updateAssetBalances(Neow3j neow3j) throws IOException, ErrorResponseException {
        NeoGetUnspents response = neow3j.getUnspents(getAddress()).send();
        response.throwOnError();
        balances.updateAssetBalances(response.getUnspents());
    }

    public void updateTokenBalances(Neow3j neow3j) throws IOException, ErrorResponseException {
        NeoGetNep5Balances response = neow3j.getNep5Balances(getAddress()).send();
        response.throwOnError();
        balances.updateTokenBalances(response.getBalances());
    }

    /**
     * <p>Fetches a set of UTXOs from this account that fulfill the required asset amount.</p>
     * <br>
     * <p>Usually the UTXOs will not cover the amount exactly but cover a larger amount. Therefore
     * it is important to calculate the necessary change before using the UTXOs in a
     * transaction.</p>
     *
     * @param assetId  The asset needed.
     * @param amount   The amount needed.
     * @param strategy The strategy with which to choose the UTXOs available on this account.
     * @return the list of UTXOs covering the required amount.
     * @throws IllegalStateException      if this account does not have any balances, e.g. because
     *                                    they have not been updated before.
     * @throws InsufficientFundsException if this account does does not possess enough UTXOs to
     *                                    fulfill the required amount.
     */
    public List<Utxo> getUtxosForAssetAmount(String assetId, BigDecimal amount,
            InputCalculationStrategy strategy) {

        if (getBalances() == null) {
            throw new IllegalStateException("Account does not have any asset balances. " +
                    "Update account's asset balances first.");
        }
        if (!getBalances().hasAsset(assetId)) {
            throw new InsufficientFundsException("Account balance does not contain the asset " +
                    "with ID " + assetId);
        }
        AssetBalance balance = getBalances().getAssetBalance(assetId);
        if (balance.getAmount().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Needed " + amount + " but only found " +
                    balance.getAmount() + " for asset with ID " + assetId);
        }
        return strategy.calculateInputs(balance.getUtxos(), amount);
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
     * @throws AccountException      if
     *                               <li>the account doesn't hold an encrypted private key
     *                               <li>the account does already hold a decrypted private key
     *                               <li>the public key derived from the decrypted private key is
     *                               not equal to the already set public key.
     */
    public void decryptPrivateKey(String password, ScryptParams scryptParams)
            throws NEP2InvalidFormat, CipherException, NEP2InvalidPassphrase {

        if (this.privateKey != null) {
            throw new AccountException("The account does already hold a decrypted private key.");
        }
        if (this.encryptedPrivateKey == null) {
            throw new AccountException("The account does not hold an encrypted private key.");
        }
        ECKeyPair ecKeyPair = NEP2.decrypt(password, this.encryptedPrivateKey, scryptParams);
        this.privateKey = ecKeyPair.getPrivateKey();
        if (this.publicKey != null && !this.publicKey.equals(ecKeyPair.getPublicKey2())) {
            throw new AccountException("The public key derived from the decrypted private key does "
                    + "not equal the public key that was already set on the account.");
        }
        publicKey = ecKeyPair.getPublicKey2();
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
            throw new AccountException("The account does not hold a decrypted private key.");
        }
        this.encryptedPrivateKey = NEP2.encrypt(password, getECKeyPair(), scryptParams);
        // TODO: 2019-07-14 Guil:
        // Is it the safest way of overwriting a variable on the JVM?
        // I don't think so. ;-)
        this.privateKey = null;
    }

    public boolean isMultiSig() {
        if (this.verificationScript == null) {
            throw new AccountException("This account does not have a verification script, which is "
                    + "needed to determine if it is a multi-sig account.");
        }
        return this.verificationScript.isMultiSigScript();
    }

    public NEP6Account toNEP6Account() {
        if (encryptedPrivateKey == null) {
            throw new AccountException("Private key is not encrypted. Encrypt private key first.");
        }

        if (this.verificationScript == null) {
            return new NEP6Account(this.address, this.label, this.isDefault, this.isLocked,
                    this.encryptedPrivateKey, null, null);
        }
        NEP6Contract contract = null;
        if (this.verificationScript.isMultiSigScript()) {
            int nrOfAccs = this.verificationScript.getNrOfAccounts();
            List<NEP6Parameter> parameters = new ArrayList<>();
            IntStream.range(0, nrOfAccs).forEachOrdered(i -> {
                parameters.add(new NEP6Parameter("signature" + i, ContractParameterType.SIGNATURE));
            });
            String script = Numeric.toHexStringNoPrefix(this.verificationScript.getScript());
            contract = new NEP6Contract(script, parameters, false);
        } else if (this.verificationScript.isSingleSigScript()) {
            String script = Numeric.toHexStringNoPrefix(this.verificationScript.getScript());
            NEP6Parameter param = new NEP6Parameter("signature", ContractParameterType.SIGNATURE);
            contract = new NEP6Contract(script, Arrays.asList(param), false);
        }
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
        b.privateKey = privateKey;
        b.publicKey = keyPair.getPublicKey2();
        b.address = keyPair.getAddress();
        b.label = keyPair.getAddress();
        b.verificationScript = new VerificationScript(keyPair.getPublicKey2());
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
        b.publicKey = ecKeyPair.getPublicKey2();
        b.address = ecKeyPair.getAddress();
        b.label = b.address;
        b.verificationScript = new VerificationScript(ecKeyPair.getPublicKey2());
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
            byte[] script = Numeric.hexStringToByteArray(contr.getScript());
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
        BigInteger privateKey;
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
