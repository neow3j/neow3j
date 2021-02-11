package io.neow3j.wallet.nep6;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.crypto.ScryptParams;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * NEO NEP-6 wallet file.
 */
public class NEP6Wallet {

    @JsonProperty("name")
    private String name;

    @JsonProperty("version")
    private String version;

    @JsonProperty("scrypt")
    private ScryptParams scrypt;

    @JsonProperty("accounts")
    private List<NEP6Account> accounts;

    @JsonProperty("extra")
    private Object extra;

    public NEP6Wallet() {
    }

    public NEP6Wallet(String name, String version, ScryptParams scrypt, List<NEP6Account> accounts, Object extra) {
        this.name = name;
        this.version = version;
        this.scrypt = scrypt;
        this.accounts = (accounts == null) ? new ArrayList<>() :  accounts;
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

    public List<NEP6Account> getAccounts() {
        return accounts;
    }

    public Object getExtra() {
        return extra;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NEP6Wallet)) return false;
        NEP6Wallet that = (NEP6Wallet) o;
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
        return "NEP6Wallet{" +
                "name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", scrypt=" + scrypt +
                ", accounts=" + accounts +
                ", extra=" + extra +
                '}';
    }

}
