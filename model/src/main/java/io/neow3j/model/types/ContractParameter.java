package io.neow3j.model.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContractParameter {

    @JsonProperty("name")
    private String paramName;

    @JsonProperty("type")
    private ContractParameterType paramType;

    @JsonProperty("value")
    private Object value;

    public ContractParameter() {
    }

    public ContractParameter(String paramName, ContractParameterType paramType) {
        this.paramName = paramName;
        this.paramType = paramType;
    }

    public ContractParameter(ContractParameterType paramType, Object value) {
        this.paramType = paramType;
        this.value = value;
    }

    public ContractParameter(String paramName, ContractParameterType paramType, Object value) {
        this.paramName = paramName;
        this.paramType = paramType;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContractParameter)) return false;
        ContractParameter that = (ContractParameter) o;
        return Objects.equals(paramName, that.paramName) &&
                paramType == that.paramType &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paramName, paramType, value);
    }

    @Override
    public String toString() {
        return "ContractParameter{" +
                "paramName='" + paramName + '\'' +
                ", paramType=" + paramType +
                ", value=" + value +
                '}';
    }
}
