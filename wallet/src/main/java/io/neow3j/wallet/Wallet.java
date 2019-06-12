package io.neow3j.wallet;

import io.neow3j.crypto.NEP2;
import io.neow3j.crypto.ScryptParams;
import io.neow3j.crypto.exceptions.CipherException;
import io.neow3j.crypto.exceptions.NEP2InvalidFormat;
import io.neow3j.crypto.exceptions.NEP2InvalidPassphrase;
import io.neow3j.wallet.nep6.NEP6Account;
import io.neow3j.wallet.nep6.NEP6Wallet;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static io.neow3j.crypto.SecurityProviderChecker.addBouncyCastle;

/**
 * <p>NEO wallet file management. For reference, refer to
 * <a href="https://github.com/neo-project/proposals/blob/master/nep-6.mediawiki">
 * Wallet Standards (NEP-6)</a> or the
 * <a href="https://github.com/neo-project/proposals/blob/master/nep-2.mediawiki">
 * Passphrase-protected Private Key Standard (NEP-2)</a>.</p>
 */
public class Wallet {

    private static final String DEFAULT_WALLET_NAME = "neow3jWallet";

    public static final String CURRENT_VERSION = "1.0";

    private String name;

    private String version;

    private List<Account> accounts = new ArrayList<>();

    private ScryptParams scryptParams;

    static {
        addBouncyCastle();
    }

    private Wallet() {}

    public static Builder with() {
        return new Builder();
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public ScryptParams getScryptParams() {
        return scryptParams;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Adds the given account to this wallet.
     * @param account The account to add.
     * @return true if the account was added, false if an account with that address was already in
     * the wallet.
     */
    public boolean addAccount(Account account) {
        if (accounts.stream().anyMatch(acc -> acc.getAddress().equals(account.getAddress()))) {
            return false;
        }
        accounts.add(account);
        return true;
    }

    /**
     * Removes the account with the given address from this wallet.
     * @param address The address of the account to be removed.
     * @return true if an account was removed, false if no account with the given address was found.
     */
    public boolean removeAccount(String address) {
        return accounts.removeIf(acc -> acc.getAddress().equals(address));
    }

    public void decryptAllAccounts(String password) throws NEP2InvalidFormat, CipherException,
            NEP2InvalidPassphrase {

        for (Account acct : accounts) {
            acct.decryptPrivateKey(password, scryptParams);
        }
    }

    public void encryptAllAccounts(String password) throws CipherException {

        for (Account acct : accounts) {
            acct.encryptPrivateKey(password, scryptParams);
        }
    }

    public NEP6Wallet toNEP6Wallet() {
        List<NEP6Account> accts = accounts.stream().map(
                a -> a.toNEP6Account()).collect(Collectors.toList());
        return new NEP6Wallet(name, version, scryptParams, accts, null);
    }

    public static class Builder {

        String name;
        String version;
        List<Account> accounts = new ArrayList<>();
        ScryptParams scryptParams;
        NEP6Wallet nep6Wallet;

        /**
         * Constructs an empty Wallet Builder.
         */
        public Builder() {}

        public Builder name(String name) {
            this.name = name; return this;
        }

        public Builder version(String version) {
            this.version = version; return this;
        }

        public Builder accounts(List<Account> accounts) {
            this.accounts.addAll(accounts); return this;
        }

        public Builder account(Account account) {
            this.accounts.add(account); return this;
        }

        public Builder genericAccount() throws InvalidAlgorithmParameterException,
                NoSuchAlgorithmException, NoSuchProviderException {

            return this.account(Account.with().freshKeyPair().build());
        }

        public Builder scryptParams(ScryptParams scryptParams) {
            if (this.nep6Wallet != null) {
                throw new IllegalStateException("Can't specify new Scrypt parameters if " +
                        "wallet is build from a NEP-6 wallet file.");
            }
            this.scryptParams = scryptParams; return this;
        }

        public Builder nep6Wallet(NEP6Wallet nep6Wallet) {
            if (this.scryptParams != null) {
                throw new IllegalStateException("You already specified Scrypt parameters which " +
                        "would be overwritten by the parameters given in the NEP-6 wallet file.");
            }
            this.nep6Wallet = nep6Wallet; return this;
        }

        // This method is for semantic convenience only. One could also call
        // Account.with().build() and get the same result.
        public Builder defaultValues() {
            this.name = DEFAULT_WALLET_NAME;
            this.version = CURRENT_VERSION;
            this.scryptParams = NEP2.DEFAULT_SCRYPT_PARAMS;
            return this;
        }

        public Wallet build() {
            Wallet wallet = new Wallet();

            if (this.nep6Wallet != null) {
                fillWallet(wallet, this.nep6Wallet);
            }
            if (!accounts.isEmpty()) {
                accounts.forEach(wallet::addAccount);
            }
            if (this.name != null) wallet.name = this.name;
            if (this.version != null) wallet.version = this.version;
            if (this.scryptParams != null) wallet.scryptParams = this.scryptParams;
            // Set default values if nothing has been set Till this points.
            if (wallet.name == null) wallet.name = DEFAULT_WALLET_NAME;
            if (wallet.version == null) wallet.version = CURRENT_VERSION;
            if (wallet.scryptParams == null) wallet.scryptParams = NEP2.DEFAULT_SCRYPT_PARAMS;

            return wallet;
        }

        protected static void fillWallet(Wallet wallet, NEP6Wallet nep6Wallet) {
            wallet.name = nep6Wallet.getName();
            wallet.version = nep6Wallet.getVersion();
            wallet.scryptParams = nep6Wallet.getScrypt();
            for (NEP6Account nep6Acct : nep6Wallet.getAccounts()) {
                wallet.accounts.add(Account.with().nep6Account(nep6Acct).build());
            }
        }
    }
}
