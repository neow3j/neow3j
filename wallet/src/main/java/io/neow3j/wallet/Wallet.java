package io.neow3j.wallet;

import io.neow3j.crypto.NEP2;
import io.neow3j.crypto.ScryptParams;
import io.neow3j.crypto.exceptions.CipherException;
import io.neow3j.crypto.exceptions.NEP2InvalidFormat;
import io.neow3j.crypto.exceptions.NEP2InvalidPassphrase;
import io.neow3j.wallet.nep6.NEP6Account;
import io.neow3j.wallet.nep6.NEP6Wallet;

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

    protected Wallet(Builder builder) {
        this.name = builder.name;
        this.version = builder.version;
        this.scryptParams = builder.scryptParams;
        this.accounts = builder.accounts;
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

    public static Builder fromNEP6Wallet(NEP6Wallet nep6Wallet) {
        Builder b = new Builder();
        b.name = nep6Wallet.getName();
        b.version = nep6Wallet.getVersion();
        b.scryptParams = nep6Wallet.getScrypt();
        for (NEP6Account nep6Acct : nep6Wallet.getAccounts()) {
            b.accounts.add(Account.fromNEP6Account(nep6Acct).build());
        }
        return b;
    }

    public static class Builder {

        String name;
        String version;
        List<Account> accounts;
        ScryptParams scryptParams;

        public Builder() {
            this.name = DEFAULT_WALLET_NAME;
            this.version = CURRENT_VERSION;
            this.accounts = new ArrayList<>();
            this.scryptParams = NEP2.DEFAULT_SCRYPT_PARAMS;
        }

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

        public Builder scryptParams(ScryptParams scryptParams) {
            this.scryptParams = scryptParams; return this;
        }

        public Wallet build() {
            return new Wallet(this);
        }
    }
}
