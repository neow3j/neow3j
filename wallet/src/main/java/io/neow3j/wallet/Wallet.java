package io.neow3j.wallet;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.neow3j.crypto.NEP2;
import io.neow3j.crypto.ScryptParams;
import io.neow3j.crypto.exceptions.CipherException;
import io.neow3j.crypto.exceptions.NEP2InvalidFormat;
import io.neow3j.crypto.exceptions.NEP2InvalidPassphrase;
import io.neow3j.wallet.nep6.NEP6Account;
import io.neow3j.wallet.nep6.NEP6Wallet;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
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

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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

    /**
     * Sets the account at the given index to be the default account.
     * The previous default account is unset.
     * @param index the index of the new default account.
     */
    public void setDefaultAccount(int index) {
        for (int i = 0; i < accounts.size(); i++) {
            accounts.get(i).setIsDefault(i == index);
        }
    }

    public ScryptParams getScryptParams() {
        return scryptParams;
    }

    public Account getDefaultAccount() {
        return this.accounts.stream().filter(Account::isDefault)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No default account found."));
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

    public static Builder fromNEP6Wallet(String nep6WalletFileName) throws IOException {
        return fromNEP6Wallet(Wallet.class.getClassLoader().getResourceAsStream(nep6WalletFileName));
    }

    public static Builder fromNEP6Wallet(URI nep6WalletFileUri) throws IOException {
        return fromNEP6Wallet(nep6WalletFileUri.toURL().openStream());
    }

    public static Builder fromNEP6Wallet(InputStream nep6WalletFileInputStream) throws IOException {
        NEP6Wallet nep6Wallet = OBJECT_MAPPER.readValue(nep6WalletFileInputStream, NEP6Wallet.class);
        return fromNEP6Wallet(nep6Wallet);
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

    /**
     * Creates a new wallet with one account that is set as the default account.
     * @return the new wallet.
     */
    public static Wallet createGenericWallet() {
        Account a = Account.fromNewECKeyPair().isDefault(true).build();
        return new Builder().account(a).build();
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
