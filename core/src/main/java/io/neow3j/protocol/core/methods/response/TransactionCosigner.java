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
    public String account;

    @JsonProperty("scopes")
    public WitnessScope scopes;

    public TransactionCosigner() {
    }


}