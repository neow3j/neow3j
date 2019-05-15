package io.neow3j.wallet;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

/**
 * NEO NEP-6 wallet file.
 */
public class WalletFile {

    @JsonProperty("name")
    private String name;

    @JsonProperty("version")
    private String version;

    @JsonProperty("scrypt")
    private ScryptParams scrypt;

    @JsonProperty("accounts")
    private List<Account> accounts;

    @JsonProperty("extra")
    private String extra;

    public WalletFile() {
    }

    public WalletFile(String name, String version, ScryptParams scrypt, List<Account> accounts, String extra) {
        this.name = name;
        this.version = version;
        this.scrypt = scrypt;
        this.accounts = accounts;
        this.extra = extra;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public ScryptParams getScrypt() {
        return scrypt;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public WalletFile addAccount(Account account) {
        this.accounts.add(account);
        return this;
    }

    public WalletFile removeAccount(Account account) {
        this.accounts.remove(account);
        return this;
    }

    public String getExtra() {
        return extra;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WalletFile)) return false;
        WalletFile that = (WalletFile) o;
        return Objects.equals(getName(), that.getName()) &&
                Objects.equals(getVersion(), that.getVersion()) &&
                Objects.equals(getScrypt(), that.getScrypt()) &&
                Objects.equals(getAccounts(), that.getAccounts()) &&
                Objects.equals(getExtra(), that.getExtra());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getVersion(), getScrypt(), getAccounts(), getExtra());
    }

    @Override
    public String toString() {
        return "WalletFile{" +
                "name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", scrypt=" + scrypt +
                ", accounts=" + accounts +
                ", extra='" + extra + '\'' +
                '}';
    }

}
