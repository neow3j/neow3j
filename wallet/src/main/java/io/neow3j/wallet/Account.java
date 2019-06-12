package io.neow3j.wallet;

import io.neow3j.constants.OpCode;
import io.neow3j.crypto.Credentials;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.Keys;
import io.neow3j.crypto.NEP2;
import io.neow3j.crypto.ScryptParams;
import io.neow3j.crypto.exceptions.CipherException;
import io.neow3j.crypto.exceptions.NEP2InvalidFormat;
import io.neow3j.crypto.exceptions.NEP2InvalidPassphrase;
import io.neow3j.model.types.ContractParameterType;
import io.neow3j.utils.Numeric;
import io.neow3j.wallet.nep6.NEP6Account;
import io.neow3j.wallet.nep6.NEP6Contract;
import io.neow3j.wallet.nep6.NEP6Contract.NEP6Parameter;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Collections;

import static io.neow3j.constants.OpCode.CHECKMULTISIG;

public class Account {

    private Credentials credentials;

    private String encryptedPrivateKey;

    private String label;

    private boolean isDefault;

    private boolean isLocked;

    private NEP6Contract contract;

    private Account() {}

    public static Builder with() {
        return new Builder();
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public String getAddress() {
        return credentials == null ? null : credentials.getAddress();
    }

    public ECKeyPair getEcKeyPair() {
        return credentials == null ? null : credentials.getEcKeyPair();
    }

    public String getLabel() {
        return label;
    }

    public Boolean isDefault() {
        return isDefault;
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


    /**
     * Decrypts this account's private key, according to the NEP-2 standard, if not already decrypted.
     * @param password The passphrase used to decrypt this account's private key.
     * @param scryptParams The Scrypt parameters used for decryption.
     */
    public void decryptPrivateKey(String password, ScryptParams scryptParams)
            throws NEP2InvalidFormat, CipherException, NEP2InvalidPassphrase {

        if (credentials == null || credentials.getEcKeyPair() == null) {
            // No null check done for or the encrypted private key because an Account is either
            // constructed with a key pair or with an encrypted private key.
            ECKeyPair keyPair = NEP2.decrypt(password, encryptedPrivateKey, scryptParams);
            credentials = new Credentials(keyPair);
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
            // No null checks done for credentials or key pair because an Account is either
            // constructed with a key pair or with an encrypted private key.
            this.encryptedPrivateKey= NEP2.encrypt(password, credentials.getEcKeyPair(), scryptParams);
        }
    }

    public boolean isMultiSig() {
        // TODO Claude 11.06.19
        // Multi-sig accounts
        String script = contract.getScript();
        return contract != null &&
                script != null &&
                script.substring(script.length() - 2).equals(OpCode.toHexString(CHECKMULTISIG));
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
            if (getEcKeyPair() != null) {
                byte[] scriptBytes = credentials.getEcKeyPair().getVerificationScriptAsArrayFromPublicKey();
                String scriptHex = Numeric.toHexStringNoPrefix(scriptBytes);
                NEP6Parameter param = new NEP6Parameter("signature", ContractParameterType.SIGNATURE);
                contract = new NEP6Contract(scriptHex, Collections.singletonList(param), false);
            }
        }
    }

    public static class Builder {

        String label;
        ECKeyPair ecKeyPair;
        Boolean isDefault;
        Boolean isLocked;
        NEP6Account nep6Account;

        /**
         * Constructs an empty Account Builder.
         */
        public Builder() {}

        public Builder label(String label) {
            this.label = label; return this;
        }

        public Builder ecKeyPair(ECKeyPair ecKeyPair) {
            if (nep6Account != null) {
                throw new IllegalStateException("Build an account either from a NEP-6 account or a " +
                        "key pair, but not both.");
            }
            this.ecKeyPair = ecKeyPair; return this;
        }

        public Builder freshKeyPair() throws InvalidAlgorithmParameterException,
                NoSuchAlgorithmException, NoSuchProviderException {

            if (nep6Account != null) {
                throw new IllegalStateException("Build an account either from a NEP-6 account or a " +
                        "key pair, but not both.");
            }
            this.ecKeyPair = Keys.createEcKeyPair(); return this;
        }

        public Builder isDefault(boolean isDefault) {
            this.isDefault = isDefault; return this;
        }

        public Builder isLocked(boolean isLocked) {
            this.isLocked = isLocked; return this;
        }

        public Builder nep6Account(NEP6Account nep6Account) {
            if (ecKeyPair != null) {
                throw new IllegalStateException("Build an account either from a NEP-6 account or a " +
                        "key pair, but not both.");
            }
            this.nep6Account = nep6Account; return this;
        }

        public Account build() {
            if (nep6Account == null && ecKeyPair == null) {
                throw new IllegalStateException("Creation of an account needs either a NEP-6 " +
                        "account or a EC key pair as input.");
            }
            Account acct = new Account();
            if (nep6Account != null) {
                fillAccount(acct, nep6Account);
            } else if (ecKeyPair != null) {
                Credentials cred = new Credentials(ecKeyPair);
                acct.credentials = cred;
                acct.label = cred.getAddress();
            }

            if (this.label != null) acct.label = this.label;
            if (this.isDefault != null) acct.isDefault = this.isDefault;
            if (this.isLocked != null) acct.isLocked = this.isLocked;

            acct.tryAddVerificationScriptContract();
            return acct;
        }

        protected static void fillAccount(Account acct, NEP6Account nep6Acct) {
            acct.credentials = new Credentials(nep6Acct.getAddress());
            acct.label = nep6Acct.getLabel();
            acct.encryptedPrivateKey = nep6Acct.getKey();
            acct.isLocked = nep6Acct.getLock();
            acct.isDefault = nep6Acct.getDefault();
            acct.contract = nep6Acct.getContract();
        }
    }
}
