package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ContractParameter {

    @JsonProperty("type")
    private ContractParameterType paramType;

    @JsonProperty("value")
    private Object value;

    public ContractParameter() {
    }

    public ContractParameter(ContractParameterType paramType, Object value) {
        this.paramType = paramType;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContractParameter)) return false;
        ContractParameter that = (ContractParameter) o;
        return paramType == that.paramType &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paramType, value);
    }

    @Override
    public String toString() {
        return "ContractParameter{" +
                "paramType=" + paramType +
                ", value=" + value +
                '}';
    }
}
