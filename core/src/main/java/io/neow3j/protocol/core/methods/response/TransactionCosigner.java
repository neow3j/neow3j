package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.transaction.TransactionAttributeType;
import io.neow3j.transaction.WitnessScope;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionCosigner extends TransactionAttribute {

    @JsonProperty("account")
    private String account;

    @JsonProperty("scopes")
    private WitnessScope scopes;

    public TransactionCosigner() {
        super(TransactionAttributeType.COSIGNER);
    }

    public TransactionCosigner(String account, WitnessScope scopes) {
        super(TransactionAttributeType.COSIGNER);
        this.account = account;
        this.scopes = scopes;
    }

    public String getAccount() {
        return account;
    }

    public WitnessScope getScopes() {
        return scopes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TransactionCosigner)) {
            return false;
        }
        TransactionCosigner other = (TransactionCosigner) o;
        return Objects.equals(getAccount(), other.getAccount()) &&
                getScopes() == other.getScopes();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAccount(), getScopes());
    }

    @Override
    public String toString() {
        return "TransactionCosigner{" +
                "account='" + account + '\'' +
                ", scopes=" + scopes +
                '}';
    }
}