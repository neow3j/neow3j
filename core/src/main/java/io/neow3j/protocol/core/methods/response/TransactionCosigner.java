package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import io.neow3j.model.types.TransactionAttributeUsageType;
import io.neow3j.transaction.WitnessScope;
import io.neow3j.utils.Numeric;
import java.util.Arrays;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionCosigner {

    @JsonProperty("account")
    private String account;

    @JsonProperty("scopes")
    private WitnessScope scopes;

    public TransactionCosigner() {
    }

    public TransactionCosigner(String account, WitnessScope scopes) {
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
        TransactionCosigner that = (TransactionCosigner) o;
        return Objects.equals(getAccount(), that.getAccount()) &&
                getScopes() == that.getScopes();
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