package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.transaction.TransactionAttributeType;
import io.neow3j.transaction.WitnessScope;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionSigner extends TransactionAttribute {

    @JsonProperty("account")
    private String account;

    @JsonProperty("scopes")
    private WitnessScope scopes;

    public TransactionSigner() {
        super(TransactionAttributeType.SIGNER);
    }

    public TransactionSigner(String account, WitnessScope scopes) {
        super(TransactionAttributeType.SIGNER);
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
        if (!(o instanceof TransactionSigner)) {
            return false;
        }
        TransactionSigner other = (TransactionSigner) o;
        return Objects.equals(getAccount(), other.getAccount()) &&
                getScopes() == other.getScopes();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAccount(), getScopes());
    }

    @Override
    public String toString() {
        return "TransactionSigner{" +
                "account='" + account + '\'' +
                ", scopes=" + scopes +
                '}';
    }
}