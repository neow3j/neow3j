package io.neow3j.wallet;

import static io.neow3j.crypto.SecurityProviderChecker.addBouncyCastle;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.neow3j.contract.ScriptHash;
import io.neow3j.crypto.NEP2;
import io.neow3j.crypto.ScryptParams;
import io.neow3j.crypto.exceptions.CipherException;
import io.neow3j.crypto.exceptions.NEP2InvalidFormat;
import io.neow3j.crypto.exceptions.NEP2InvalidPassphrase;
import io.neow3j.wallet.exceptions.AccountStateException;
import io.neow3j.wallet.nep6.NEP6Account;
import io.neow3j.wallet.nep6.NEP6Wallet;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * // TODO: Update class docs.
 * <p>NEO wallet file management. For reference, refer to
 * <a href="https://github.com/neo-project/proposals/blob/master/nep-6.mediawiki">
 * Wallet Standards (NEP-6)</a> or the
 * <a href="https://github.com/neo-project/proposals/blob/master/nep-2.mediawiki">
 * Passphrase-protected Private Key Standard (NEP-2)</a>.</p>
 */
public class Wallet {

    private static final String DEFAULT_WALLET_NAME = "neow3jWallet";
    public static final String CURRENT_VERSION = "3.0";
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private String name;
    private String version;
    private Map<ScriptHash, Account> accounts = new HashMap<>();
    private ScriptHash defaultAccount;
    private ScryptParams scryptParams;

    static {
        addBouncyCastle();
    }

    private Wallet() {
    }

    protected Wallet(Builder builder) {
        this.name = builder.name;
        this.version = builder.version;
        this.scryptParams = builder.scryptParams;
        this.accounts = builder.accounts.stream().collect(
                Collectors.toMap(Account::getScriptHash, Function.identity()));
        this.defaultAccount = builder.defaultAccount;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public List<Account> getAccounts() {
        return accounts.entrySet().stream()
                .sorted(Entry.comparingByKey())
                .map(Entry::getValue)
                .collect(Collectors.toList());
    }

    /**
     * Sets the account with the given script hash to the default account of this wallet.
     *
     * @param accountScriptHash The new default account.
     * @throws AccountStateException if the given account is not in this wallet.
     */
    public void setDefaultAccount(ScriptHash accountScriptHash) throws AccountStateException {
        if (!this.accounts.containsKey(accountScriptHash)) {
            throw new AccountStateException("Wallet did not contain the account with script hash "
                    + accountScriptHash.toString() + ".");
        }
        this.defaultAccount = accountScriptHash;
    }

    public ScryptParams getScryptParams() {
        return scryptParams;
    }

    public Account getDefaultAccount() {
        if (this.defaultAccount == null) {
            throw new AccountStateException("No default account was set. Make sure that the wallet "
                    + "has a default account.");
        }
        return this.accounts.get(defaultAccount);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Adds the given account to this wallet if it doesn't contain an account with the same
     * address.
     *
     * @param account The account to add.
     * @return true if the account was added, false if an account with the same address was already
     * in the wallet.
     */
    public boolean addAccount(Account account) {
        if (this.accounts.containsKey(account.getScriptHash())) {
            return false;
        }
        this.accounts.put(account.getScriptHash(), account);
        return true;
    }

    /**
     * Removes the account with the given address from this wallet.
     *
     * @param address The address of the account to be removed.
     * @return true if an account was removed, false if no account with the given address was found.
     */
    public boolean removeAccount(String address) {
        return accounts.remove(ScriptHash.fromAddress(address)) != null;
    }

    public void decryptAllAccounts(String password)
            throws NEP2InvalidFormat, CipherException, NEP2InvalidPassphrase {

        for (Entry<ScriptHash, Account> e : accounts.entrySet()) {
            e.getValue().decryptPrivateKey(password, scryptParams);
        }
    }

    public void encryptAllAccounts(String password) throws CipherException {
        for (Entry<ScriptHash, Account> e : accounts.entrySet()) {
            e.getValue().encryptPrivateKey(password, scryptParams);
        }
    }

    public NEP6Wallet toNEP6Wallet() {
        List<NEP6Account> accts = this.accounts.values().stream()
                .map(Account::toNEP6Account)
                .collect(Collectors.toList());
        return new NEP6Wallet(name, version, scryptParams, accts, null);
    }

    public static Builder fromNEP6Wallet(String nep6WalletFileName) throws IOException {
        return fromNEP6Wallet(
                Wallet.class.getClassLoader().getResourceAsStream(nep6WalletFileName));
    }

    public static Builder fromNEP6Wallet(URI nep6WalletFileUri) throws IOException {
        return fromNEP6Wallet(nep6WalletFileUri.toURL().openStream());
    }

    public static Builder fromNEP6Wallet(File nep6WalletFile) throws IOException {
        return fromNEP6Wallet(new FileInputStream(nep6WalletFile));
    }

    public static Builder fromNEP6Wallet(InputStream nep6WalletFileInputStream) throws IOException {
        NEP6Wallet nep6Wallet = OBJECT_MAPPER
                .readValue(nep6WalletFileInputStream, NEP6Wallet.class);
        return fromNEP6Wallet(nep6Wallet);
    }

    public static Builder fromNEP6Wallet(NEP6Wallet nep6Wallet) {
        Account[] accs = nep6Wallet.getAccounts().stream()
                .map(nep6Acc -> Account.fromNEP6Account(nep6Acc).build())
                .toArray(Account[]::new);

        Builder b = new Builder()
                .name(nep6Wallet.getName())
                .version(nep6Wallet.getVersion())
                .scryptParams(nep6Wallet.getScrypt())
                .accounts(accs);

        // Set the default account if available
        nep6Wallet.getAccounts().stream()
                .filter(NEP6Account::getDefault).findFirst()
                .ifPresent(a -> b.defaultAccount(ScriptHash.fromAddress(a.getAddress())));

        return b;
    }

    /**
     * Creates a NEP6 compatible wallet file.
     *
     * @param destination the file that the wallet file should be saved.
     * @return the new wallet.
     * @throws IOException throws if failed to create the wallet on disk.
     */
    public Wallet saveNEP6Wallet(File destination) throws IOException {
        if (destination == null) {
            throw new IllegalArgumentException("Destination file cannot be null");
        }
        NEP6Wallet nep6Wallet = toNEP6Wallet();
        OBJECT_MAPPER.writeValue(destination, nep6Wallet);
        return this;
    }

    /**
     * Creates a new wallet with one account that is set as the default account.
     *
     * @return the new wallet.
     */
    public static Wallet createWallet() {
        Account a = Account.fromNewECKeyPair().isDefault(true).build();
        return new Builder().accounts(a).build();
    }

    /**
     * Creates a new wallet with one account that is set as the default account. Encrypts such
     * account with the password.
     *
     * @param password password used to encrypt the account.
     * @return the new wallet.
     * @throws CipherException throws if failed encrypt the created wallet.
     */
    public static Wallet createWallet(final String password)
            throws CipherException {
        Account a = Account.fromNewECKeyPair().isDefault(true).build();
        Wallet wallet = new Builder().accounts(a).build();
        wallet.encryptAllAccounts(password);
        return wallet;
    }

    /**
     * Creates a new wallet with one account that is set as the default account. Also, encrypts such
     * account and persists the NEP6 wallet to a file.
     *
     * @param password    password used to encrypt the account.
     * @param destination destination to the new NEP6 wallet file.
     * @return the new wallet.
     * @throws IOException     throws if failed to create the wallet on disk.
     * @throws CipherException throws if failed encrypt the created wallet.
     */
    public static Wallet createWallet(String password, File destination)
            throws CipherException, IOException {
        Wallet wallet = createWallet(password);
        wallet.saveNEP6Wallet(destination);
        return wallet;
    }

    public Account getAccount(ScriptHash account) {
        return this.accounts.get(account);
    }

    public static class Builder {

        String name;
        String version;
        List<Account> accounts;
        ScryptParams scryptParams;
        private ScriptHash defaultAccount;

        public Builder() {
            this.name = DEFAULT_WALLET_NAME;
            this.version = CURRENT_VERSION;
            this.accounts = new ArrayList<>();
            this.scryptParams = NEP2.DEFAULT_SCRYPT_PARAMS;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        /**
         * Adds the given accounts to the wallet, the first of which is set to be he default
         * account.
         *
         * @param accounts The accounts to add.
         * @return this.
         */
        public Builder accounts(Account... accounts) {
            this.accounts.addAll(Arrays.asList(accounts));
            this.defaultAccount = accounts[0].getScriptHash();
            return this;
        }

        /**
         * TODO: Document
         * @param accountScriptHash
         * @return
         */
        public Builder defaultAccount(ScriptHash accountScriptHash) {
            // TODO: Implement
            return this;
        }

        public Builder scryptParams(ScryptParams scryptParams) {
            this.scryptParams = scryptParams;
            return this;
        }

        public Wallet build() {
            return new Wallet(this);
        }
    }

}
