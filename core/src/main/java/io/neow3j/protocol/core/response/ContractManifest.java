package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import io.neow3j.types.ContractParameter;
import io.neow3j.types.ContractParameterType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ContractManifest {

    @JsonProperty("name")
    private String name;

    @JsonProperty("groups")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<ContractGroup> groups;

    @JsonProperty(value = "features")
    private HashMap<Object, Object> features;

    @JsonProperty("supportedstandards")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<String> supportedStandards;

    @JsonProperty("abi")
    private ContractABI abi;

    @JsonProperty("permissions")
    private List<ContractPermission> permissions;

    // TODO: If the wildcard character "*" is read the list should be empty or null.
    // List of trusted contracts
    @JsonProperty("trusts")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<String> trusts;

    // Custom user data
    @JsonProperty("extra")
    private Object extra;

    public ContractManifest() {
    }

    public ContractManifest(String name, List<ContractGroup> groups,
            HashMap<Object, Object> features, List<String> supportedStandards, ContractABI abi,
            List<ContractPermission> permissions, List<String> trusts, Object extra) {
        this.name = name;
        this.groups = groups;
        this.features = features == null ? new HashMap<>() : features;
        this.supportedStandards = supportedStandards;
        this.abi = abi;
        this.permissions = permissions;
        this.trusts = trusts;
        this.extra = extra;
    }

    public String getName() {
        return name;
    }

    public List<ContractGroup> getGroups() {
        return groups;
    }

    public HashMap<Object, Object> getFeatures() {
        return features;
    }

    public List<String> getSupportedStandards() {
        return supportedStandards;
    }

    public ContractABI getAbi() {
        return abi;
    }

    public List<ContractPermission> getPermissions() {
        return permissions;
    }

    public List<String> getTrusts() {
        return trusts;
    }

    public Object getExtra() {
        return extra;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ContractManifest)) {
            return false;
        }
        ContractManifest that = (ContractManifest) o;
        return Objects.equals(getName(), that.getName()) &&
                Objects.equals(getGroups(), that.getGroups()) &&
                Objects.equals(getFeatures(), that.getFeatures()) &&
                Objects.equals(getAbi(), that.getAbi()) &&
                Objects.equals(getPermissions(), that.getPermissions()) &&
                Objects.equals(getTrusts(), that.getTrusts()) &&
                Objects.equals(getSupportedStandards(), that.getSupportedStandards()) &&
                Objects.equals(getExtra(), that.getExtra());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(),
                getGroups(),
                getFeatures(),
                getAbi(),
                getPermissions(),
                getTrusts(),
                getSupportedStandards(),
                getExtra());
    }

    @Override
    public String toString() {
        return "ContractManifest{" +
                "name=" + name +
                ", groups=" + groups +
                ", features=" + features +
                ", abi=" + abi +
                ", permissions=" + permissions +
                ", trusts=" + trusts +
                ", supportedStandards=" + supportedStandards +
                ", extra=" + extra +
                '}';
    }

    // Mutually trusted contract
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContractGroup {

        @JsonProperty("pubKey")
        @JsonAlias("pubkey")
        private String pubKey;

        @JsonProperty("signature")
        private String signature;

        public ContractGroup() {
        }

        public ContractGroup(String pubKey, String signature) {
            this.pubKey = pubKey;
            this.signature = signature;
        }

        public String getPubKey() {
            return pubKey;
        }

        public String getSignature() {
            return signature;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ContractGroup)) {
                return false;
            }
            ContractGroup that = (ContractGroup) o;
            return Objects.equals(getPubKey(), that.getPubKey()) &&
                    Objects.equals(getSignature(), that.getSignature());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getPubKey(), getSignature());
        }

        @Override
        public String toString() {
            return "ContractGroup{" +
                    "pubKey=" + pubKey +
                    ", signature=" + signature +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContractABI {

        @JsonProperty("methods")
        @JsonSetter(nulls = Nulls.AS_EMPTY)
        private List<ContractMethod> methods;

        @JsonProperty("events")
        @JsonSetter(nulls = Nulls.AS_EMPTY)
        private List<ContractEvent> events;

        public ContractABI() {
        }

        public ContractABI(List<ContractMethod> methods, List<ContractEvent> events) {
            this.methods = methods != null ? methods : new ArrayList<>();
            this.events = events != null ? events : new ArrayList<>();
        }

        public List<ContractMethod> getMethods() {
            return methods;
        }

        public List<ContractEvent> getEvents() {
            return events;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ContractABI)) {
                return false;
            }
            ContractABI that = (ContractABI) o;
            return Objects.equals(getMethods(), that.getMethods()) &&
                    Objects.equals(getEvents(), that.getEvents());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getMethods(), getEvents());
        }

        @Override
        public String toString() {
            return "ContractABI{" +
                    "methods=" + methods +
                    ", events=" + events +
                    '}';
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class ContractMethod {

            @JsonProperty("name")
            private String name;

            @JsonProperty("parameters")
            @JsonSetter(nulls = Nulls.AS_EMPTY)
            private List<ContractParameter> parameters;

            @JsonProperty("offset")
            private int offset;

            @JsonProperty("returntype")
            private ContractParameterType returnType;

            @JsonProperty("safe")
            private boolean safe;

            public ContractMethod() {
            }

            public ContractMethod(String name, List<ContractParameter> parameters, int offset,
                    ContractParameterType returnType, boolean safe) {
                this.name = name;
                this.parameters = parameters != null ? parameters : new ArrayList<>();
                this.offset = offset;
                this.returnType = returnType;
                this.safe = safe;
            }

            public String getName() {
                return name;
            }

            public List<ContractParameter> getParameters() {
                return parameters;
            }

            public int getOffset() {
                return offset;
            }

            public ContractParameterType getReturnType() {
                return returnType;
            }

            public boolean isSafe() {
                return safe;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) {
                    return true;
                }
                if (!(o instanceof ContractMethod)) {
                    return false;
                }
                ContractMethod that = (ContractMethod) o;
                return Objects.equals(getName(), that.getName()) &&
                        Objects.equals(getParameters(), that.getParameters()) &&
                        getOffset() == that.getOffset() &&
                        getReturnType() == that.getReturnType() &&
                        isSafe() == that.isSafe();
            }

            @Override
            public int hashCode() {
                return Objects.hash(getName(), getParameters(), getOffset(), getReturnType(),
                        isSafe());
            }

            @Override
            public String toString() {
                return "ContractMethod{" +
                        "name='" + name + '\'' +
                        ", parameters=" + parameters +
                        ", offset=" + offset +
                        ", returnType=" + returnType +
                        ", safe=" + safe +
                        '}';
            }
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class ContractEvent {

            @JsonProperty("name")
            private String name;

            @JsonProperty("parameters")
            @JsonSetter(nulls = Nulls.AS_EMPTY)
            private List<ContractParameter> parameters;

            public ContractEvent() {
            }

            public ContractEvent(String name,
                    List<ContractParameter> parameters) {
                this.name = name;
                this.parameters = parameters != null ? parameters : new ArrayList<>();
            }

            public String getName() {
                return name;
            }

            public List<ContractParameter> getParameters() {
                return parameters;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) {
                    return true;
                }
                if (!(o instanceof ContractEvent)) {
                    return false;
                }
                ContractEvent that = (ContractEvent) o;
                return Objects.equals(getName(), that.getName()) &&
                        Objects.equals(getParameters(), that.getParameters());
            }

            @Override
            public int hashCode() {
                return Objects.hash(getName(), getParameters());
            }

            @Override
            public String toString() {
                return "ContractEvent{" +
                        "name='" + name + '\'' +
                        ", parameters=" + parameters +
                        '}';
            }
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContractPermission {

        @JsonProperty("contract")
        private String contract;

        @JsonProperty("methods")
        @JsonFormat(with = {JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY,
                JsonFormat.Feature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED})
        private List<String> methods;

        public ContractPermission() {
        }

        public ContractPermission(String contract, List<String> methods) {
            this.contract = contract;
            this.methods = methods;
        }

        public String getContract() {
            return contract;
        }

        public List<String> getMethods() {
            return methods;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ContractPermission)) {
                return false;
            }
            ContractPermission that = (ContractPermission) o;
            return Objects.equals(getContract(), that.getContract()) &&
                    Objects.equals(getMethods(), that.getMethods());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getContract(), getMethods());
        }

        @Override
        public String toString() {
            return "ContractPermission{" +
                    "contract=" + contract +
                    ", methods=" + methods +
                    '}';
        }
    }

}
