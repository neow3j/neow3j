package io.neow3j.wallet.nep6;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class NEP6Account {

    @JsonProperty("address")
    private String address;

    @JsonProperty("label")
    private String label;

    @JsonProperty("isDefault")
    @JsonAlias("isdefault")
    private Boolean isDefault;

    @JsonProperty("lock")
    private Boolean lock;

    @JsonProperty("key")
    private String key;

    @JsonProperty("contract")
    private NEP6Contract contract;

    @JsonProperty("extra")
    private Object extra;

    public NEP6Account() {
    }

    public NEP6Account(String address, String label, Boolean isDefault, Boolean lock, String key,
            NEP6Contract contract, Object extra) {
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

    // https://stackoverflow.com/questions/32270422/jackson-renames-primitive-boolean-field-by-removing-is
    @JsonProperty("isDefault")
    public Boolean getDefault() {
        return isDefault;
    }

    public Boolean getLock() {
        return lock;
    }

    public String getKey() {
        return key;
    }

    public NEP6Contract getContract() {
        return contract;
    }

    public Object getExtra() {
        return extra;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NEP6Account)) return false;
        NEP6Account account = (NEP6Account) o;
        return Objects.equals(getAddress(), account.getAddress());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAddress(), getLabel(), isDefault, getLock(), getKey(),
                getContract(), getExtra());
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
                ", extra=" + extra +
                '}';
    }

}
