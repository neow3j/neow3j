package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.protocol.core.Response;

import java.util.List;
import java.util.Objects;

public class NeoGetContractState extends Response<NeoGetContractState.ContractState> {

    public ContractState getContractState() {
        return getResult();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContractState {

        @JsonProperty("version")
        private int version;

        @JsonProperty("hash")
        private String hash;

        @JsonProperty("script")
        private String script;

        @JsonProperty("parameters")
        private List<ContractParameterType> contractParameters;

        @JsonProperty("returntype")
        private ContractParameterType returnContractType;

        @JsonProperty("name")
        private String name;

        @JsonProperty("code_version")
        private String codeVersion;

        @JsonProperty("author")
        private String author;

        @JsonProperty("email")
        private String email;

        @JsonProperty("description")
        private String description;

        @JsonProperty("properties")
        private ContractStateProperties properties;

        public ContractState() {
        }

        public ContractState(int version, String hash, String script, List<ContractParameterType> contractParameters, ContractParameterType returnContractType, String name, String codeVersion, String author, String email, String description, ContractStateProperties properties) {
            this.version = version;
            this.hash = hash;
            this.script = script;
            this.contractParameters = contractParameters;
            this.returnContractType = returnContractType;
            this.name = name;
            this.codeVersion = codeVersion;
            this.author = author;
            this.email = email;
            this.description = description;
            this.properties = properties;
        }

        public int getVersion() {
            return version;
        }

        public String getHash() {
            return hash;
        }

        public String getScript() {
            return script;
        }

        public List<ContractParameterType> getContractParameters() {
            return contractParameters;
        }

        public ContractParameterType getReturnContractType() {
            return returnContractType;
        }

        public String getName() {
            return name;
        }

        public String getCodeVersion() {
            return codeVersion;
        }

        public String getAuthor() {
            return author;
        }

        public String getEmail() {
            return email;
        }

        public String getDescription() {
            return description;
        }

        public ContractStateProperties getProperties() {
            return properties;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ContractState)) return false;
            ContractState that = (ContractState) o;
            return getVersion() == that.getVersion() &&
                    Objects.equals(getHash(), that.getHash()) &&
                    Objects.equals(getScript(), that.getScript()) &&
                    Objects.equals(getContractParameters(), that.getContractParameters()) &&
                    getReturnContractType() == that.getReturnContractType() &&
                    Objects.equals(getName(), that.getName()) &&
                    Objects.equals(getCodeVersion(), that.getCodeVersion()) &&
                    Objects.equals(getAuthor(), that.getAuthor()) &&
                    Objects.equals(getEmail(), that.getEmail()) &&
                    Objects.equals(getDescription(), that.getDescription()) &&
                    Objects.equals(getProperties(), that.getProperties());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getVersion(), getHash(), getScript(), getContractParameters(), getReturnContractType(), getName(), getCodeVersion(), getAuthor(), getEmail(), getDescription(), getProperties());
        }

        @Override
        public String toString() {
            return "ContractState{" +
                    "version=" + version +
                    ", hash='" + hash + '\'' +
                    ", script='" + script + '\'' +
                    ", contractParameters=" + contractParameters +
                    ", returnContractType=" + returnContractType +
                    ", name='" + name + '\'' +
                    ", codeVersion='" + codeVersion + '\'' +
                    ", author='" + author + '\'' +
                    ", email='" + email + '\'' +
                    ", description='" + description + '\'' +
                    ", properties=" + properties +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContractStateProperties {

        @JsonProperty("storage")
        private Boolean storage;

        @JsonProperty("dynamic_invoke")
        private Boolean dynamicInvoke;

        public ContractStateProperties() {
        }

        public ContractStateProperties(Boolean storage, Boolean dynamicInvoke) {
            this.storage = storage;
            this.dynamicInvoke = dynamicInvoke;
        }

        public Boolean getStorage() {
            return storage;
        }

        public Boolean getDynamicInvoke() {
            return dynamicInvoke;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ContractStateProperties)) return false;
            ContractStateProperties that = (ContractStateProperties) o;
            return Objects.equals(getStorage(), that.getStorage()) &&
                    Objects.equals(getDynamicInvoke(), that.getDynamicInvoke());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getStorage(), getDynamicInvoke());
        }

        @Override
        public String toString() {
            return "ContractStateProperties{" +
                    "storage=" + storage +
                    ", dynamicInvoke=" + dynamicInvoke +
                    '}';
        }
    }

}
