package io.neow3j.wallet;

import io.neow3j.crypto.Base64;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.crypto.NEP2;
import io.neow3j.crypto.ScryptParams;
import io.neow3j.crypto.WIF;
import io.neow3j.crypto.exceptions.CipherException;
import io.neow3j.crypto.exceptions.NEP2InvalidFormat;
import io.neow3j.crypto.exceptions.NEP2InvalidPassphrase;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.Neow3jConfig;
import io.neow3j.protocol.core.response.NeoGetNep17Balances;
import io.neow3j.script.VerificationScript;
import io.neow3j.types.ContractParameterType;
import io.neow3j.types.Hash160;
import io.neow3j.utils.Numeric;
import io.neow3j.wallet.exceptions.AccountStateException;
import io.neow3j.wallet.nep6.NEP6Account;
import io.neow3j.wallet.nep6.NEP6Contract;
import io.neow3j.wallet.nep6.NEP6Contract.NEP6Parameter;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static io.neow3j.utils.AddressUtils.isValidAddress;
import static java.lang.String.format;

/**
 * Represents a Neo account.
 * <p>
 * An account can be a single-signature or multi-signature account. The latter does not contain EC key material
 * because it is based on multiple EC key pairs.
 */
@SuppressWarnings("unchecked")
public class Account {

    private ECKeyPair keyPair;
    private String address;
    private String encryptedPrivateKey;
    private String label;
    private boolean isLocked;
    private VerificationScript verificationScript;
    private Wallet wallet;
    // The signing threshold is null if the account is single-sig.
    private Integer signingThreshold;
    // The nr of involved keys is null if the account is single-sig.
    private Integer nrOfParticipants;

    protected Account() {
    }

    /**
     * Constructs a new account with the given EC key pair.
     *
     * @param ecKeyPair the key pair of the account.
     */
    public Account(ECKeyPair ecKeyPair) {
        this.keyPair = ecKeyPair;
        this.address = ecKeyPair.getAddress();
        this.label = this.address;
        this.verificationScript = new VerificationScript(ecKeyPair.getPublicKey());
    }

    public String getAddress() {
        return address;
    }

    public Hash160 getScriptHash() {
        return Hash160.fromAddress(address);
    }

    /**
     * Gets this account's EC key pair if available.
     * <p>
     * The key pair is not available, e.g., if this account was encrypted.
     *
     * @return the key pair or null if it is not available.
     */
    public ECKeyPair getECKeyPair() {
        return keyPair;
    }

    public String getLabel() {
        return label;
    }

    public Account label(String label) {
        this.label = label;
        return this;
    }

    public Wallet getWallet() {
        return this.wallet;
    }

    /**
     * Checks if the account is default in the linked wallet.
     *
     * @return whether the account is default.
     */
    public Boolean isDefault() {
        if (this.wallet == null) {
            return false;
        }
        return this.wallet.isDefault(this.getScriptHash());
    }

    public Boolean isLocked() {
        return isLocked;
    }

    public Account lock() {
        this.isLocked = true;
        return this;
    }

    public void unlock() {
        this.isLocked = false;
    }

    void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }

    public VerificationScript getVerificationScript() {
        return this.verificationScript;
    }

    public String getEncryptedPrivateKey() {
        return encryptedPrivateKey;
    }

    /**
     * Decrypts this account's private key, according to the NEP-2 standard, if not already decrypted. Uses the
     * default Scrypt parameters.
     *
     * @param password the passphrase used to decrypt this account's private key.
     * @throws NEP2InvalidFormat     if the encrypted NEP2 has an invalid format.
     * @throws CipherException       if failed encrypt the created wallet.
     * @throws NEP2InvalidPassphrase if the passphrase is not valid.
     * @throws AccountStateException if
     *                               <ul>
     *                               <li>the account doesn't hold an encrypted private key.
     *                               <li>the account does already hold a decrypted private key.
     *                               <li>the public key derived from the decrypted private key is not equal to the
     *                               already set public key.
     *                               </ul>
     */
    public void decryptPrivateKey(String password) throws NEP2InvalidFormat, CipherException, NEP2InvalidPassphrase {
        decryptPrivateKey(password, NEP2.DEFAULT_SCRYPT_PARAMS);
    }

    /**
     * Decrypts this account's private key, according to the NEP-2 standard, if not already decrypted.
     *
     * @param password     the passphrase used to decrypt this account's private key.
     * @param scryptParams the Scrypt parameters used for decryption.
     * @throws NEP2InvalidFormat     if the encrypted NEP2 has an invalid format.
     * @throws CipherException       if failed encrypt the created wallet.
     * @throws NEP2InvalidPassphrase if the passphrase is not valid.
     * @throws AccountStateException if
     *                               <ul>
     *                               <li>the account doesn't hold an encrypted private key.
     *                               <li>the account does already hold a decrypted private key.
     *                               <li>the public key derived from the decrypted private key is not equal to the
     *                               already set public key.
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
     * Encrypts this account's private key according to the NEP-2 standard using the default Scrypt parameters.
     *
     * @param password the passphrase used to encrypt this account's private key.
     * @throws CipherException if the encryption of the created wallet failed.
     */
    public void encryptPrivateKey(String password) throws CipherException {
        encryptPrivateKey(password, NEP2.DEFAULT_SCRYPT_PARAMS);
    }

    /**
     * Encrypts this account's private key according to the NEP-2 standard.
     *
     * @param password     the passphrase used to encrypt this account's private key.
     * @param scryptParams the Scrypt parameters used for encryption.
     * @throws CipherException if failed encrypt the created wallet.
     */
    public void encryptPrivateKey(String password, ScryptParams scryptParams) throws CipherException {
        if (this.keyPair == null) {
            throw new AccountStateException("The account does not hold a decrypted private key.");
        }
        this.encryptedPrivateKey = NEP2.encrypt(password, this.keyPair, scryptParams);
        this.keyPair.getPrivateKey().erase();
        this.keyPair = null;
    }

    /**
     * Checks if this account is a multi-sig account.
     *
     * @return true if this account is a multi-sig account. False, otherwise.
     */
    public boolean isMultiSig() {
        return signingThreshold != null && nrOfParticipants != null;
    }

    /**
     * @return the signing threshold if this is a multi-sig account.
     */
    public Integer getSigningThreshold() {
        if (!isMultiSig()) {
            throw new AccountStateException(format("Cannot get signing threshold from account %s, because it is not " +
                    "multi-sig.", this.getAddress()));
        }
        return signingThreshold;
    }

    /**
     * Returns the number of involved keys if this is a multi-sig account.
     *
     * @return the number of involved keys.
     */
    public Integer getNrOfParticipants() {
        if (!isMultiSig()) {
            throw new AccountStateException(format("Cannot get number of participants from account %s, because it is " +
                    "not multi-sig.", this.getAddress()));
        }
        return nrOfParticipants;
    }

    /**
     * Gets the balances of all NEP-17 tokens that this account owns.
     * <p>
     * The token amounts are returned in token fractions. E.g., an amount of 1 GAS is returned as 1*10^8 GAS fractions.
     * <p>
     * Requires on a neo-node with the RpcNep17Tracker plugin installed. The balances are not cached locally. Every
     * time this method is called a request is send to the neo-node.
     *
     * @param neow3j the {@link Neow3j} object used to call a Neo node.
     * @return the map of token script hashes to token amounts.
     * @throws IOException if something goes wrong when communicating with the Neo node.
     */
    public Map<Hash160, BigInteger> getNep17Balances(Neow3j neow3j) throws IOException {
        NeoGetNep17Balances result = neow3j.getNep17Balances(getScriptHash()).send();
        Map<Hash160, BigInteger> balances = new HashMap<>();
        result.getBalances().getBalances().forEach(b -> balances.put(b.getAssetHash(), new BigInteger(b.getAmount())));
        return balances;
    }

    public NEP6Account toNEP6Account() {
        if (this.keyPair != null && this.encryptedPrivateKey == null) {
            throw new AccountStateException("Account private key is available but not encrypted.");
        }
        if (this.verificationScript == null) {
            return new NEP6Account(this.address, this.label, this.isDefault(), this.isLocked,
                    this.encryptedPrivateKey, null, null);
        }
        List<NEP6Parameter> parameters = new ArrayList<>();
        if (this.verificationScript.isMultiSigScript()) {
            IntStream.range(0, this.verificationScript.getNrOfAccounts()).forEachOrdered(i ->
                    parameters.add(new NEP6Parameter("signature" + i, ContractParameterType.SIGNATURE))
            );
        } else if (this.verificationScript.isSingleSigScript()) {
            parameters.add(new NEP6Parameter("signature", ContractParameterType.SIGNATURE));
        }
        String script = Base64.encode(this.verificationScript.getScript());
        NEP6Contract contract = new NEP6Contract(script, parameters, false);
        return new NEP6Account(this.address, this.label, this.isDefault(), this.isLocked, this.encryptedPrivateKey,
                contract, null);
    }

    /**
     * Creates an account from the given verification script.
     *
     * @param script the verification script.
     * @return the account with a verification script.
     */
    public static Account fromVerificationScript(VerificationScript script) {
        String address = Hash160.fromScript(script.getScript()).toAddress();
        Account account = new Account();
        account.address = address;
        account.label = address;
        account.verificationScript = script;
        if (script.isMultiSigScript()) {
            account.signingThreshold = script.getSigningThreshold();
            account.nrOfParticipants = script.getNrOfAccounts();
        }
        return account;
    }

    /**
     * Creates an account from the given public key.
     * <p>
     * Derives the verification script from the public key, which is needed to calculate the network fee of a
     * transaction.
     *
     * @param publicKey the public key.
     * @return the account with a verification script.
     */
    public static Account fromPublicKey(ECPublicKey publicKey) {
        VerificationScript script = new VerificationScript(publicKey);
        String address = Hash160.fromScript(script.getScript()).toAddress();
        Account account = new Account();
        account.address = address;
        account.label = address;
        account.verificationScript = script;
        return account;
    }

    /**
     * Creates a multi-sig account from the given public keys. Mind that the ordering of the keys is important for
     * later usage of the account.
     *
     * @param publicKeys       the public keys from which to derive the multi-sig account.
     * @param signingThreshold the number of signatures needed when using this account for signing transactions.
     * @return the multi-sig account.
     */
    public static Account createMultiSigAccount(List<ECPublicKey> publicKeys, int signingThreshold) {
        VerificationScript script = new VerificationScript(publicKeys, signingThreshold);
        String address = Hash160.fromScript(script.getScript()).toAddress();
        Account account = new Account();
        account.address = address;
        account.label = address;
        account.verificationScript = script;
        account.signingThreshold = signingThreshold;
        account.nrOfParticipants = publicKeys.size();
        return account;
    }

    /**
     * Creates a multi-sig account holding the given address.
     *
     * @param address          the address of the multi-sig account.
     * @param signingThreshold the number of signatures needed when using this account for signing transactions.
     * @param nrOfParticipants the number of participating accounts.
     * @return the multi-sig account.
     */
    public static Account createMultiSigAccount(String address, int signingThreshold, int nrOfParticipants) {
        Account account = new Account();
        account.address = address;
        account.label = address;
        account.signingThreshold = signingThreshold;
        account.nrOfParticipants = nrOfParticipants;
        return account;
    }

    /**
     * Creates an account from the given WIF.
     *
     * @param wif the WIF of the account.
     * @return the account.
     */
    public static Account fromWIF(String wif) {
        BigInteger privateKey = Numeric.toBigInt(WIF.getPrivateKeyFromWIF(wif));
        ECKeyPair keyPair = ECKeyPair.create(privateKey);
        Account account = new Account();
        account.keyPair = keyPair;
        account.address = keyPair.getAddress();
        account.label = keyPair.getAddress();
        account.verificationScript = new VerificationScript(keyPair.getPublicKey());
        return account;
    }

    /**
     * Creates an account from the provided NEP-6 account.
     *
     * @param nep6Acct the account in NEP-6 format.
     * @return the account.
     */
    public static Account fromNEP6Account(NEP6Account nep6Acct) {
        Account account = new Account();
        account.address = nep6Acct.getAddress();
        account.label = nep6Acct.getLabel();
        account.encryptedPrivateKey = nep6Acct.getKey();
        account.isLocked = nep6Acct.getLock();
        NEP6Contract contr = nep6Acct.getContract();
        if (contr != null && contr.getScript() != null && !contr.getScript().isEmpty()) {
            byte[] script = Base64.decode(contr.getScript());
            account.verificationScript = new VerificationScript(script);
            if (account.verificationScript.isMultiSigScript()) {
                account.signingThreshold = account.verificationScript.getSigningThreshold();
                account.nrOfParticipants = account.verificationScript.getNrOfAccounts();
            }
        }
        return account;
    }

    /**
     * Creates an account from the given address.
     * <p>
     * Note that an account created with this method does not contain a verification script nor an EC key pair.
     * Therefore, it cannot be used for transaction signing.
     * <p>
     * Don't use this method for creating a multi-sig account from an address. Use
     * {@link Account#createMultiSigAccount(String, int, int)} instead.
     *
     * @param address the address of the account. Must be a valid Neo address. Make sure that the address version
     *                used in this address is the same as the one configured in
     *                {@link Neow3jConfig#getAddressVersion()}.
     * @return the account.
     */
    public static Account fromAddress(String address) {
        if (!isValidAddress(address)) {
            throw new IllegalArgumentException("Invalid address.");
        }
        Account account = new Account();
        account.address = address;
        account.label = address;
        return account;
    }

    /**
     * Creates an account from the given script hash.
     * <p>
     * Note that an account created with this method does not contain a verification script nor an EC key pair.
     * Therefore, it cannot be used for transaction signing.
     *
     * @param scriptHash the script hash of the account.
     * @return the account.
     */
    public static Account fromScriptHash(Hash160 scriptHash) {
        return fromAddress(scriptHash.toAddress());
    }

    /**
     * Creates a new account with a fresh key pair.
     *
     * @return the new account.
     */
    public static Account create() {
        try {
            return new Account(ECKeyPair.createEcKeyPair());
        } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException("Failed to create a new EC key pair.", e);
        }
    }

}
