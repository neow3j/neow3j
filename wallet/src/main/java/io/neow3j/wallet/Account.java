package io.neow3j.wallet;

import io.neow3j.constants.OpCode;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.Keys;
import io.neow3j.crypto.NEP2;
import io.neow3j.crypto.ScryptParams;
import io.neow3j.crypto.Sign;
import io.neow3j.crypto.WIF;
import io.neow3j.crypto.exceptions.CipherException;
import io.neow3j.crypto.exceptions.NEP2InvalidFormat;
import io.neow3j.crypto.exceptions.NEP2InvalidPassphrase;
import io.neow3j.crypto.transaction.RawVerificationScript;
import io.neow3j.model.types.ContractParameterType;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.methods.response.NeoGetNep5Balances;
import io.neow3j.protocol.core.methods.response.NeoGetUnspents;
import io.neow3j.protocol.exceptions.ErrorResponseException;
import io.neow3j.utils.Numeric;
import io.neow3j.wallet.nep6.NEP6Account;
import io.neow3j.wallet.nep6.NEP6Contract;
import io.neow3j.wallet.nep6.NEP6Contract.NEP6Parameter;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static io.neow3j.constants.OpCode.CHECKMULTISIG;

public class Account {

    private BigInteger privateKey;
    private BigInteger publicKey;
    private String address;
    private String encryptedPrivateKey;
    private String label;
    private boolean isDefault;
    private boolean isLocked;
    private NEP6Contract contract;
    private Balances balances;

    private Account() {}

    private Account(Builder b) {
        this.label = b.label;
        this.privateKey = b.privateKey;
        this.publicKey = b.publicKey;
        this.isDefault = b.isDefault;
        this.isLocked = b.isLocked;
        this.address = b.address;
        this.encryptedPrivateKey = b.encryptedPrivateKey;
        this.contract = b.contract;
        this.balances = new Balances(this);
        this.tryAddVerificationScriptContract();
    }

    public String getAddress() {
        return address;
    }

    public ECKeyPair getECKeyPair() {
        if (privateKey != null && publicKey != null) {
            return new ECKeyPair(privateKey, publicKey);
        } else if (privateKey != null) {
            return ECKeyPair.create(privateKey);
        } else {
            return null;
        }
    }

    public BigInteger getPrivateKey() {
        return privateKey;
    }

    public BigInteger getPublicKey() {
        return publicKey;
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

    public NEP6Contract getContract() {
        return contract;
    }

    public String getEncryptedPrivateKey() {
        return encryptedPrivateKey;
    }

    public Balances getBalances() {
        return balances;
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
     * Decrypts this account's private key, according to the NEP-2 standard, if not already decrypted.
     * @param password The passphrase used to decrypt this account's private key.
     * @param scryptParams The Scrypt parameters used for decryption.
     */
    public void decryptPrivateKey(String password, ScryptParams scryptParams)
            throws NEP2InvalidFormat, CipherException, NEP2InvalidPassphrase {

        if (privateKey == null) {
            if (encryptedPrivateKey == null) {
                throw new IllegalStateException("The account does not hold an encrypted private key.");
            }
            ECKeyPair ecKeyPair = NEP2.decrypt(password, encryptedPrivateKey, scryptParams);
            privateKey = ecKeyPair.getPrivateKey();
            publicKey = ecKeyPair.getPublicKey();
            tryAddVerificationScriptContract();
        }
    }

    /**
     * Encrypts this account's private key, according to the NEP-2 standard, if not already encrypted.
     * @param password The passphrase used to encrypt this account's private key.
     * @param scryptParams The Scrypt parameters used for encryption.
     */
    public void encryptPrivateKey(String password, ScryptParams scryptParams) throws CipherException {

        if (encryptedPrivateKey == null) {
            if (privateKey == null) {
                throw new IllegalStateException("The account does not hold a private key.");
            }
            this.encryptedPrivateKey= NEP2.encrypt(password, getECKeyPair(), scryptParams);
        }
    }

    public boolean isMultiSig() {
        // TODO Claude 20.06.19:
        // Even if the contract script is not empty this might be a multi-sig account. Additionally,
        // the script in the contract could be something else than a verification script.
        // Clarify if it makes sense to enforce that the contract's script must be a verification
        // script and that it must be available (especially for mutli-sig accounts).
        if (contract != null && contract.getScript() != null && contract.getScript().length() >= 2) {
            String script = contract.getScript();
            return script.substring(script.length() - 2).equals(OpCode.toHexString(CHECKMULTISIG));
        }
        return false;
    }

    public NEP6Account toNEP6Account() {
        if (encryptedPrivateKey == null) {
            throw new IllegalStateException("Private key is not encrypted. Encrypt private key first.");
        }
        return new NEP6Account(getAddress(), label, isDefault, isLocked, encryptedPrivateKey,
                contract, null);
    }

    private void tryAddVerificationScriptContract() {
        if (contract == null || contract.getScript() == null) {
            if (publicKey != null) {
                byte[] scriptBytes = RawVerificationScript.fromPublicKey(publicKey).getScript();
                String scriptHex = Numeric.toHexStringNoPrefix(scriptBytes);
                NEP6Parameter param = new NEP6Parameter("signature", ContractParameterType.SIGNATURE);
                contract = new NEP6Contract(scriptHex, Collections.singletonList(param), false);
            }
        }
    }

    /**
     * Creates a multi-sig account builder from the given public keys.
     * Mind that the ordering of the keys is important for later usage of the account.
     *
     * @param publicKeys The public keys from which to derive the multi-sig account.
     * @param signatureThreshold The number of signatures needed when using this account for signing
     *                           transactions.
     * @return the multi-sig account builder;
     */
    public static Builder fromMultiSigKeys(List<BigInteger> publicKeys, int signatureThreshold) {
        Builder b = new Builder();
        b.publicKey = Sign.publicKeyFromPrivate(b.privateKey);
        b.address = Keys.getMultiSigAddress(signatureThreshold, publicKeys);
        b.label = b.address;

        byte[] script = RawVerificationScript.fromPublicKeys(signatureThreshold, publicKeys).getScript();
        String scriptHexString = Numeric.toHexStringNoPrefix(script);

        List<NEP6Parameter> parameters = new ArrayList<>();
        IntStream.range(0, publicKeys.size()).forEachOrdered(i ->
                parameters.add(new NEP6Parameter("signature" + i, ContractParameterType.SIGNATURE)));

        b.contract = new NEP6Contract(scriptHexString, parameters, false);
        return b;
    }

    public static Builder fromWIF(String wif) {
        Builder b = new Builder();
        b.privateKey = Numeric.toBigInt(WIF.getPrivateKeyFromWIF(wif));
        b.publicKey = Sign.publicKeyFromPrivate(b.privateKey);
        b.address = Keys.getAddress(b.publicKey);
        b.label = b.address;
        return b;
    }

    public static Builder fromNewECKeyPair() {
        try {
            return fromECKeyPair(Keys.createEcKeyPair());
        } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException("Failed to create a new EC key pair.", e);
        }
    }

    public static Builder fromECKeyPair(ECKeyPair ecKeyPair) {
        Builder b = new Builder();
        b.privateKey = ecKeyPair.getPrivateKey();
        b.publicKey = ecKeyPair.getPublicKey();
        b.address = Keys.getAddress(ecKeyPair);
        b.label = b.address;
        return b;
    }

    public static Builder fromNEP6Account(NEP6Account nep6Acct) {
        Builder b = new Builder();
        b.address = nep6Acct.getAddress();
        b.label = nep6Acct.getLabel();
        b.encryptedPrivateKey = nep6Acct.getKey();
        b.isLocked = nep6Acct.getLock();
        b.isDefault = nep6Acct.getDefault();
        b.contract = nep6Acct.getContract();
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
     * @return the new account.
     */
    public static Account createGenericAccount() {
        return fromNewECKeyPair().build();
    }

    public static class Builder {

        String label;
        BigInteger privateKey;
        BigInteger publicKey;
        boolean isDefault;
        boolean isLocked;
        String address;
        String encryptedPrivateKey;
        NEP6Contract contract;

        private Builder() {
            isDefault = false;
            isLocked = false;
        }

        public Builder label(String label) {
            this.label = label; return this;
        }

        public Builder isDefault(boolean isDefault) {
            this.isDefault = isDefault; return this;
        }

        public Builder isLocked(boolean isLocked) {
            this.isLocked = isLocked; return this;
        }

        public Account build() {
            return new Account(this);
        }
    }
}
