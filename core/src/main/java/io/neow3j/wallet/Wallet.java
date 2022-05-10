package io.neow3j.wallet;

import io.neow3j.types.Hash160;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.neow3j.crypto.NEP2;
import io.neow3j.crypto.ScryptParams;
import io.neow3j.crypto.exceptions.CipherException;
import io.neow3j.crypto.exceptions.NEP2InvalidFormat;
import io.neow3j.crypto.exceptions.NEP2InvalidPassphrase;
import io.neow3j.protocol.Neow3j;
import io.neow3j.wallet.nep6.NEP6Account;
import io.neow3j.wallet.nep6.NEP6Wallet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.neow3j.crypto.SecurityProviderChecker.addBouncyCastle;
import static java.lang.String.format;

/**
 * The wallet manages a collection of accounts.
 */
public class Wallet {

    private static final String DEFAULT_WALLET_NAME = "neow3jWallet";
    public static final String CURRENT_VERSION = "3.0";
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private String name;
    private String version;
    private Map<Hash160, Account> accounts = new HashMap<>();
    private ScryptParams scryptParams;
    private Hash160 defaultAccount;

    static {
        addBouncyCastle();
    }

    private Wallet() {
        this.name = DEFAULT_WALLET_NAME;
        this.version = CURRENT_VERSION;
        this.scryptParams = NEP2.DEFAULT_SCRYPT_PARAMS;
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
     * @param accountHash160 the new default account.
     * @return the wallet.
     * @throws IllegalArgumentException if the given account is not in this wallet.
     */
    public Wallet defaultAccount(Hash160 accountHash160) {
        if (accountHash160 == null) {
            throw new IllegalArgumentException("No account provided to set default.");
        }
        if (!this.accounts.containsKey(accountHash160)) {
            throw new IllegalArgumentException(format("Cannot set default account on wallet. Wallet does not contain " +
                    "the account with script hash %s.", accountHash160.toString()));
        }
        this.defaultAccount = accountHash160;
        return this;
    }

    public ScryptParams getScryptParams() {
        return scryptParams;
    }

    /**
     * Gets the default account of this wallet.
     *
     * @return the default account.
     */
    public Account getDefaultAccount() {
        return this.accounts.get(this.defaultAccount);
    }

    /**
     * Checks whether an account is the default account in the wallet.
     *
     * @param account the account to be checked.
     * @return Whether the given account is the default account in this wallet.
     */
    public Boolean isDefault(Account account) {
        return isDefault(account.getScriptHash());
    }

    /**
     * Checks whether an account is the default account in the wallet.
     *
     * @param accountHash160 the account to be checked.
     * @return Whether the given account is the default account in this wallet.
     */
    public Boolean isDefault(Hash160 accountHash160) {
        return getDefaultAccount().getScriptHash().equals(accountHash160);
    }

    public Wallet name(String name) {
        this.name = name;
        return this;
    }

    public Wallet version(String version) {
        this.version = version;
        return this;
    }

    public Wallet scryptParams(ScryptParams scryptParams) {
        this.scryptParams = scryptParams;
        return this;
    }

    /**
     * Adds the given accounts to this wallet, if it doesn't contain an account with the same script hash (address).
     *
     * @param accounts the accounts to add.
     * @return this wallet instance.
     */
    public Wallet addAccounts(Account... accounts) {
        for (Account acct : accounts) {
            if (this.accounts.containsKey(acct.getScriptHash())) {
                continue;
            }
            // An account is only allowed to be in one wallet at a time.
            if (acct.getWallet() != null) {
                throw new IllegalArgumentException(
                        format("The account %s is already contained in a wallet. Please remove this account from its " +
                                "containing wallet before adding it to another wallet.", acct.getAddress()));
            }
            this.accounts.put(acct.getScriptHash(), acct);
            // Create a link for the account
            acct.setWallet(this);
        }
        return this;
    }

    /**
     * Removes the account from this wallet.
     * <p>
     * If there is only one account in the wallet left, this account can not be removed.
     *
     * @param account the account to be removed.
     * @return true if an account was removed, false if no account with the given address was found.
     */
    public boolean removeAccount(Account account) {
        return removeAccount(account.getScriptHash());
    }

    /**
     * Removes the account with the given script hash (address) from this wallet.
     * <p>
     * If there is only one account in the wallet left, this account can not be removed.
     *
     * @param hash160 the {@link Hash160} of the account to be removed.
     * @return true if an account was removed, false if no account with the given address was found.
     */
    public boolean removeAccount(Hash160 hash160) {
        if (!this.accounts.containsKey(hash160)) {
            return false;
        }
        // The wallet must have at least one account at all times.
        if (this.accounts.size() == 1) {
            throw new IllegalStateException(format("The account %s is the only account in the wallet. It cannot be " +
                    "removed.", hash160.toAddress()));
        }
        // Remove the link to this wallet in the account instance.
        this.accounts.get(hash160).setWallet(null);

        // If the removed account was the default account in this wallet, set a new default account.
        if (hash160.equals(this.getDefaultAccount().getScriptHash())) {
            Hash160 newDefaultAccountHash160 = this.accounts.entrySet().stream()
                    .filter(e -> !e.getKey().equals(hash160))
                    .iterator().next().getKey();
            this.defaultAccount(newDefaultAccountHash160);
        }
        return accounts.remove(hash160) != null;
    }

    public void decryptAllAccounts(String password)
            throws NEP2InvalidFormat, CipherException, NEP2InvalidPassphrase {

        for (Entry<Hash160, Account> e : accounts.entrySet()) {
            e.getValue().decryptPrivateKey(password, scryptParams);
        }
    }

    public void encryptAllAccounts(String password) throws CipherException {
        for (Entry<Hash160, Account> e : accounts.entrySet()) {
            e.getValue().encryptPrivateKey(password, scryptParams);
        }
    }

    public NEP6Wallet toNEP6Wallet() {
        List<NEP6Account> accts = this.accounts.values().stream()
                .map(Account::toNEP6Account)
                .collect(Collectors.toList());
        return new NEP6Wallet(name, version, scryptParams, accts, null);
    }

    public static Wallet fromNEP6Wallet(String nep6WalletFileName) throws IOException {
        return fromNEP6Wallet(Wallet.class.getClassLoader().getResourceAsStream(nep6WalletFileName));
    }

    public static Wallet fromNEP6Wallet(URI nep6WalletFileUri) throws IOException {
        return fromNEP6Wallet(nep6WalletFileUri.toURL().openStream());
    }

    public static Wallet fromNEP6Wallet(File nep6WalletFile) throws IOException {
        return fromNEP6Wallet(new FileInputStream(nep6WalletFile));
    }

    public static Wallet fromNEP6Wallet(InputStream nep6WalletFileInputStream) throws IOException {
        NEP6Wallet nep6Wallet = OBJECT_MAPPER.readValue(nep6WalletFileInputStream, NEP6Wallet.class);
        return fromNEP6Wallet(nep6Wallet);
    }

    public static Wallet fromNEP6Wallet(NEP6Wallet nep6Wallet) {
        Account[] accs = nep6Wallet.getAccounts().stream()
                .map(Account::fromNEP6Account)
                .toArray(Account[]::new);

        Optional<NEP6Account> defaultAccount = nep6Wallet.getAccounts().stream()
                .filter(NEP6Account::getDefault)
                .findFirst();

        if (defaultAccount.isPresent()) {
            Hash160 defaultAccountHash160 = Account.fromNEP6Account(defaultAccount.get()).getScriptHash();
            return new Wallet()
                    .name(nep6Wallet.getName())
                    .version(nep6Wallet.getVersion())
                    .scryptParams(nep6Wallet.getScrypt())
                    .addAccounts(accs)
                    .defaultAccount(defaultAccountHash160);
        } else {
            throw new IllegalArgumentException("The NEP-6 wallet does not contain any default account.");
        }
    }

    /**
     * Creates a NEP-6 compatible wallet file.
     *
     * @param destination the file that the wallet file should be saved.
     * @return the new wallet.
     * @throws IOException if the creation of the wallet on disk failed.
     */
    public Wallet saveNEP6Wallet(File destination) throws IOException {
        if (destination == null) {
            throw new IllegalArgumentException("Destination file cannot be null.");
        }
        NEP6Wallet nep6Wallet = toNEP6Wallet();
        if (destination.isDirectory()) {
            String fileName = getName() + ".json";
            destination = Paths.get(destination.toString(), fileName).toFile();
        }
        OBJECT_MAPPER.writeValue(destination, nep6Wallet);
        return this;
    }

    /**
     * Gets the balances of all NEP-17 tokens that this wallet owns.
     * <p>
     * The token amounts are returned in token fractions. E.g., an amount of 1 GAS is returned as 1*10^8 GAS fractions.
     * <p>
     * Requires on a Neo node with the RpcNep17Tracker plugin installed. The balances are not cached locally. Every
     * time this method is called requests are send to the neo-node for all contained accounts.
     *
     * @param neow3j the {@link Neow3j} object used to call a Neo node.
     * @return the map of token script hashes to token amounts.
     * @throws IOException if something goes wrong when communicating with the neo-node.
     */
    public Map<Hash160, BigInteger> getNep17TokenBalances(Neow3j neow3j) throws IOException {
        Map<Hash160, BigInteger> balances = new HashMap<>();
        for (Account a : this.accounts.values()) {
            for (Entry<Hash160, BigInteger> e : a.getNep17Balances(neow3j).entrySet()) {
                balances.merge(e.getKey(), e.getValue(), BigInteger::add);
            }
        }
        return balances;
    }

    /**
     * Creates a new wallet with one account.
     *
     * @return the new wallet.
     */
    public static Wallet create() {
        Account a = Account.create();
        return new Wallet().addAccounts(a).defaultAccount(a.getScriptHash());
    }

    /**
     * Creates a new wallet with one account that is set as the default account. Encrypts such account with the
     * password.
     *
     * @param password the passphrase used to encrypt the account.
     * @return the new wallet.
     * @throws CipherException if the encryption of the created wallet failed.
     */
    public static Wallet create(final String password)
            throws CipherException {
        Wallet w = create();
        w.encryptAllAccounts(password);
        return w;
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
    public static Wallet create(String password, File destination)
            throws CipherException, IOException {
        Wallet wallet = create(password);
        wallet.saveNEP6Wallet(destination);
        return wallet;
    }

    /**
     * Creates a new wallet with the given accounts.
     * The first account is set as the default account.
     *
     * @param accounts the accounts to add to the new wallet.
     * @return the new wallet.
     */
    public static Wallet withAccounts(Account... accounts) {
        if (accounts.length == 0) {
            throw new IllegalArgumentException("No accounts provided to initialize a wallet.");
        }
        return new Wallet()
                .addAccounts(accounts)
                .defaultAccount(accounts[0].getScriptHash());
    }

    public boolean holdsAccount(Hash160 hash160) {
        return this.accounts.containsKey(hash160);
    }

    /**
     * Gets the account with the given script hash if it is in this wallet.
     *
     * @param hash160 the script hash of the account.
     * @return the account if it is in this wallet. Null, otherwise.
     */
    public Account getAccount(Hash160 hash160) {
        return this.accounts.get(hash160);
    }

}
