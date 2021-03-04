package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import io.neow3j.contract.Hash160;
import io.neow3j.transaction.Signer;
import io.neow3j.transaction.WitnessScope;
import io.neow3j.utils.Numeric;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionSigner {

    @JsonProperty(value = "account", required = true)
    private String account;

    @JsonProperty(value = "scopes", required = true)
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<WitnessScope> scopes;

    @JsonProperty("allowedcontracts")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> allowedContracts = new ArrayList<>();

    @JsonProperty("allowedgroups")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> allowedGroups = new ArrayList<>();

    protected TransactionSigner() {
    }

    public TransactionSigner(Signer signer) {
        this.account = signer.getScriptHash().toString();
        this.scopes = signer.getScopes();
        this.allowedContracts = signer.getAllowedContracts().stream()
                .map(Hash160::toString)
                .collect(Collectors.toList());
        this.allowedGroups = signer.getAllowedGroups().stream()
                .map(s -> Numeric.toHexStringNoPrefix(s.getEncoded(true)))
                .collect(Collectors.toList());
    }

    public TransactionSigner(String account, List<WitnessScope> scopes, List<String> allowedContracts,
            List<String> allowedGroups) {
        this.account = account;
        this.scopes = scopes;
        this.allowedContracts = allowedContracts;
        this.allowedGroups = allowedGroups;
    }

    public TransactionSigner(String account, List<WitnessScope> scopes) {
        this(account, scopes, new ArrayList<>(), new ArrayList<>());
    }

    public String getAccount() {
        return account;
    }

    public List<WitnessScope> getScopes() {
        return scopes;
    }

    public List<String> getAllowedContracts() {
        return allowedContracts;
    }

    public List<String> getAllowedGroups() {
        return allowedGroups;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TransactionSigner)) {
            return false;
        }
        TransactionSigner other = (TransactionSigner) o;
        return Objects.equals(getAccount(), other.getAccount()) &&
                Objects.equals(getScopes(), other.getScopes()) &&
                Objects.equals(getAllowedContracts(), other.getAllowedContracts()) &&
                Objects.equals(getAllowedGroups(), other.getAllowedGroups());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAccount(), getScopes(), getAllowedContracts(), getAllowedGroups());
    }

    @Override
    public String toString() {
        return "TransactionSigner{" +
                "account='" + account + '\'' +
                ", scopes=" + scopes +
                ", allowedContracts=" + allowedContracts +
                ", allowedGroups=" + allowedGroups +
                '}';
    }
}
