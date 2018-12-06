package io.neow3j.crypto;

import com.fasterxml.jackson.annotation.JsonAlias;
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

    public WalletFile addAccount(WalletFile.Account account) {
        this.accounts.add(account);
        return this;
    }

    public WalletFile removeAccount(WalletFile.Account account) {
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

    /**
     * NEP-2 defines the attributes "n", "r", and
     * "p". However, some wallets use different attribute
     * names. Thus, this class includes aliases for
     * compatibility purposes.
     */
    public static class ScryptParams {

        @JsonProperty("n")
        @JsonAlias("cost")
        private int n;

        @JsonProperty("r")
        @JsonAlias({"blockSize", "blocksize"})
        private int r;

        @JsonProperty("p")
        @JsonAlias("parallel")
        private int p;

        public ScryptParams() {
        }

        public ScryptParams(int n, int r, int p) {
            this.n = n;
            this.r = r;
            this.p = p;
        }

        public int getN() {
            return n;
        }

        public int getR() {
            return r;
        }

        public int getP() {
            return p;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ScryptParams)) return false;
            ScryptParams that = (ScryptParams) o;
            return getN() == that.getN() &&
                    getR() == that.getR() &&
                    getP() == that.getP();
        }

        @Override
        public int hashCode() {
            return Objects.hash(getN(), getR(), getP());
        }

        @Override
        public String toString() {
            return "ScryptParams{" +
                    "n=" + n +
                    ", r=" + r +
                    ", p=" + p +
                    '}';
        }
    }

    public static class Account {

        @JsonProperty("address")
        private String address;

        @JsonProperty("label")
        private String label;

        @JsonProperty("isDefault")
        private Boolean isDefault;

        @JsonProperty("lock")
        private Boolean lock;

        @JsonProperty("key")
        private String key;

        @JsonProperty("contract")
        private Contract contract;

        @JsonProperty("extra")
        private String extra;

        public Account() {
        }

        public Account(String address, String label, Boolean isDefault, Boolean lock, String key, Contract contract, String extra) {
            this.address = address;
            this.label = label;
            this.isDefault = isDefault;
            this.lock = lock;
            this.key = key;
            this.contract = contract;
            this.extra = extra;
        }

        public String getAddress() {
            return address;
        }

        public String getLabel() {
            return label;
        }

        public Boolean getDefault() {
            return isDefault;
        }

        public Boolean getLock() {
            return lock;
        }

        public String getKey() {
            return key;
        }

        public Contract getContract() {
            return contract;
        }

        public String getExtra() {
            return extra;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Account)) return false;
            Account account = (Account) o;
            return Objects.equals(getAddress(), account.getAddress());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getAddress(), getLabel(), isDefault, getLock(), getKey(), getContract(), getExtra());
        }

        @Override
        public String toString() {
            return "Account{" +
                    "address='" + address + '\'' +
                    ", label='" + label + '\'' +
                    ", isDefault=" + isDefault +
                    ", lock=" + lock +
                    ", key='" + key + '\'' +
                    ", contract=" + contract +
                    ", extra='" + extra + '\'' +
                    '}';
        }
    }

    public static class Contract {

        @JsonProperty("script")
        private String script;

        @JsonProperty("parameters")
        private List<String> parameters;

        @JsonProperty("deployed")
        private Boolean deployed;

        public Contract() {
        }

        public Contract(String script, List<String> parameters, Boolean deployed) {
            this.script = script;
            this.parameters = parameters;
            this.deployed = deployed;
        }

        public String getScript() {
            return script;
        }

        public List<String> getParameters() {
            return parameters;
        }

        public Boolean getDeployed() {
            return deployed;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Contract)) return false;
            Contract contract = (Contract) o;
            return Objects.equals(getScript(), contract.getScript()) &&
                    Objects.equals(getParameters(), contract.getParameters()) &&
                    Objects.equals(getDeployed(), contract.getDeployed());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getScript(), getParameters(), getDeployed());
        }

        @Override
        public String toString() {
            return "Contract{" +
                    "script='" + script + '\'' +
                    ", parameters=" + parameters +
                    ", deployed=" + deployed +
                    '}';
        }
    }

}
