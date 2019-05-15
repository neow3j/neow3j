package io.neow3j.wallet;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

public class Account {

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
