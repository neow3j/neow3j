package io.neow3j.wallet.nep6;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.types.ContractParameterType;

import java.util.List;
import java.util.Objects;

public class NEP6Contract {

    @JsonProperty("script")
    private String script;

    @JsonProperty("parameters")
    private List<NEP6Parameter> nep6Parameters;

    @JsonProperty("deployed")
    private Boolean deployed;

    public NEP6Contract() {
    }

    public NEP6Contract(String script, List<NEP6Parameter> nep6Parameters, Boolean deployed) {
        this.script = script;
        this.nep6Parameters = nep6Parameters;
        this.deployed = deployed;
    }

    public String getScript() {
        return script;
    }

    public List<NEP6Parameter> getParameters() {
        return nep6Parameters;
    }

    public Boolean getDeployed() {
        return deployed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NEP6Contract)) return false;
        NEP6Contract contract = (NEP6Contract) o;
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
                ", nep6Parameters=" + nep6Parameters +
                ", deployed=" + deployed +
                '}';
    }

    public static class NEP6Parameter {

        @JsonProperty("name")
        private String paramName;

        @JsonProperty("type")
        private ContractParameterType paramType;

        public NEP6Parameter(String paramName, ContractParameterType paramType) {
            this.paramName = paramName;
            this.paramType = paramType;
        }

        public NEP6Parameter() {
        }

        public String getParamName() {
            return paramName;
        }

        public ContractParameterType getParamType() {
            return paramType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            NEP6Parameter nep6Parameter = (NEP6Parameter) o;
            return Objects.equals(paramName, nep6Parameter.paramName) &&
                    paramType == nep6Parameter.paramType;
        }

        @Override
        public int hashCode() {
            return Objects.hash(paramName, paramType);
        }

        @Override
        public String toString() {
            return "NEP6Parameter{" +
                    "paramName='" + paramName + '\'' +
                    ", paramType=" + paramType +
                    '}';
        }
    }
}
