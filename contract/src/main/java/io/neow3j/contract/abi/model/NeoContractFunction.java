package io.neow3j.contract.abi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import io.neow3j.contract.ContractParameter;
import io.neow3j.model.types.ContractParameterType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NeoContractFunction {

    @JsonProperty("name")
    private String name;

    @JsonProperty("parameters")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<ContractParameter> parameters;

    @JsonProperty("returntype")
    private ContractParameterType returnType;

    public NeoContractFunction() {
    }

    public NeoContractFunction(String name, List<ContractParameter> parameters, ContractParameterType returnType) {
        this.name = name;
        this.parameters = parameters != null ? parameters : new ArrayList<>();
        this.returnType = returnType;
    }

    public String getName() {
        return name;
    }

    public List<ContractParameter> getParameters() {
        return parameters;
    }

    public ContractParameterType getReturnType() {
        return returnType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NeoContractFunction)) return false;
        NeoContractFunction that = (NeoContractFunction) o;
        return Objects.equals(getName(), that.getName()) &&
                Objects.equals(getParameters(), that.getParameters()) &&
                getReturnType() == that.getReturnType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getParameters(), getReturnType());
    }

    @Override
    public String toString() {
        return "NeoContractFunction{" +
                "name='" + name + '\'' +
                ", parameters=" + parameters +
                ", returnType=" + returnType +
                '}';
    }
}
