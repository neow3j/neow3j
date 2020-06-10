package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.*;
import io.neow3j.contract.ContractParameter;
import io.neow3j.model.types.ContractParameterType;
import io.neow3j.protocol.core.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NeoGetContractState extends Response<NeoGetContractState.ContractState> {

    public ContractState getContractState() {
        return getResult();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContractState {

        @JsonProperty("id")
        private int id;

        @JsonProperty("hash")
        private String hash;

        @JsonProperty("script")
        private String script;

        @JsonProperty("manifest")
        private ContractManifest manifest;

        public ContractState() {
        }

        public ContractState(int id, String hash, String script, ContractManifest manifest) {
            this.id = id;
            this.hash = hash;
            this.script = script;
            this.manifest = manifest;
        }

        public int getId() {
            return id;
        }

        public String getHash() {
            return hash;
        }

        public String getScript() {
            return script;
        }

        public ContractManifest getManifest() {
            return manifest;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ContractState)) return false;
            ContractState that = (ContractState) o;
            return Objects.equals(getId(), that.getId()) &&
                    Objects.equals(getHash(), that.getHash()) &&
                    Objects.equals(getScript(), that.getScript()) &&
                    Objects.equals(getManifest(), that.getManifest());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getId(), getHash(), getScript(), getManifest());
        }

        @Override
        public String toString() {
            return "ContractState{" +
                    "id=" + id +
                    ", hash=" + hash +
                    ", script=" + script +
                    ", manifest=" + manifest +
                    '}';
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class ContractManifest {

            @JsonProperty("groups")
            private List<ContractGroup> groups;

            @JsonProperty("features")
            private ContractFeatures features;

            @JsonProperty("abi")
            private ContractABI abi;

            @JsonProperty("permissions")
            private List<ContractPermission> permissions;

            // List of trusted contracts
            @JsonProperty("trusts") // TODO: 13.05.20  Wildcard
            @JsonSetter(nulls = Nulls.AS_EMPTY)
            @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            private List<String> trusts;

            @JsonProperty("safeMethods") // TODO: 13.05.20  Wildcard
            @JsonSetter(nulls = Nulls.AS_EMPTY)
            @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            private List<String> safeMethods;

            // Custom user data
            @JsonProperty("extra")
            private Object extra;

            public ContractManifest() {
            }

            public ContractManifest(List<ContractGroup> groups,
                                    ContractFeatures features,
                                    ContractABI abi,
                                    List<ContractPermission> permissions,
                                    List<String> trusts,
                                    List<String> safeMethods,
                                    Object extra) {
                this.groups = groups;
                this.features = features;
                this.abi = abi;
                this.permissions = permissions;
                this.trusts = trusts;
                this.safeMethods = safeMethods;
                this.extra = extra;
            }

            public List<ContractGroup> getGroups() {
                return groups;
            }

            public ContractFeatures getFeatures() {
                return features;
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

            public List<String> getSafeMethods() {
                return safeMethods;
            }

            public boolean safeMethods_isWildCard() {
                return (safeMethods.get(0).equals("*"));
            }

            public boolean trusts_isWildCard() {
                return (trusts.get(0).equals("*"));
            }

            public Object getExtra() {
                return extra;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof ContractManifest)) return false;
                ContractManifest that = (ContractManifest) o;
                return Objects.equals(getGroups(), that.getGroups()) &&
                        Objects.equals(getFeatures(), that.getFeatures()) &&
                        Objects.equals(getAbi(), that.getAbi()) &&
                        Objects.equals(getPermissions(), that.getPermissions()) &&
                        Objects.equals(getTrusts(), that.getTrusts()) &&
                        Objects.equals(getSafeMethods(), that.getSafeMethods()) &&
                        Objects.equals(getExtra(), that.getExtra());
            }

            @Override
            public int hashCode() {
                return Objects.hash(getGroups(),
                        getFeatures(),
                        getAbi(),
                        getPermissions(),
                        getTrusts(),
                        getSafeMethods(),
                        getExtra());
            }

            @Override
            public String toString() {
                return "ContractManifest{" +
                        "groups=" + groups +
                        ", features=" + features +
                        ", features=" + features +
                        ", abi=" + abi +
                        ", permissions=" + permissions +
                        ", trusts=" + trusts +
                        ", safeMethods=" + safeMethods +
                        ", extra=" + extra +
                        '}';
            }

            // Mutually trusted contract
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class ContractGroup {

                @JsonProperty("pubKey")
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
                    if (this == o) return true;
                    if (!(o instanceof ContractGroup)) return false;
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

            // Features available for the contract
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class ContractFeatures {

                // Note: 13.05.20 Michael: Neo documentation contains a third entry 'NoProperty', which is not
                //  available in the current responses. If it will be, just add the additional JsonProperty here.

                @JsonProperty("storage")
                private Boolean storage;

                @JsonProperty("payable")
                private Boolean payable;

                public ContractFeatures() {
                }

                public ContractFeatures(Boolean storage, Boolean payable) {
                    this.storage = storage;
                    this.payable = payable;
                }

                public Boolean getStorage() {
                    return storage;
                }

                public Boolean getPayable() {
                    return payable;
                }

                @Override
                public boolean equals(Object o) {
                    if (this == o) return true;
                    if (!(o instanceof ContractFeatures)) return false;
                    ContractFeatures that = (ContractFeatures) o;
                    return Objects.equals(getStorage(), that.getStorage()) &&
                            Objects.equals(getPayable(), that.getPayable());
                }

                @Override
                public int hashCode() {
                    return Objects.hash(getStorage(), getPayable());
                }

                @Override
                public String toString() {
                    return "ContractFeatures{" +
                            "storage=" + storage +
                            ", payable=" + payable +
                            '}';
                }
            }

            // TODO: 13.05.20 Michael: This ContractABI class is currently mainly copied from contract module abi/model classes.
            //  These need to be restructured and/or new positioned - same for ContractParameter
            @JsonIgnoreProperties(ignoreUnknown = true)
            @JsonPropertyOrder({"hash", "methods", "events"})
            public static class ContractABI {

                @JsonProperty("hash")
                private String hash;

                @JsonProperty("methods")
                @JsonAlias({"functions"})
                @JsonSetter(nulls = Nulls.AS_EMPTY)
                private List<ContractMethod> methods;

                @JsonProperty("events")
                @JsonSetter(nulls = Nulls.AS_EMPTY)
                private List<ContractEvent> events;

                public ContractABI() {
                }

                public ContractABI(String hash, List<ContractMethod> methods, List<ContractEvent> events) {
                    this.hash = hash;
                    this.methods = methods != null ? methods : new ArrayList<>();
                    this.events = events != null ? events : new ArrayList<>();
                }

                public String getHash() {
                    return hash;
                }

                public List<ContractMethod> getMethods() {
                    return methods;
                }

                public List<ContractEvent> getEvents() {
                    return events;
                }

                @Override
                public boolean equals(Object o) {
                    if (this == o) return true;
                    if (!(o instanceof ContractABI)) return false;
                    ContractABI that = (ContractABI) o;
                    return Objects.equals(getHash(), that.getHash()) &&
                            Objects.equals(getMethods(), that.getMethods()) &&
                            Objects.equals(getEvents(), that.getEvents());
                }

                @Override
                public int hashCode() {
                    return Objects.hash(getHash(), getMethods(), getEvents());
                }

                @Override
                public String toString() {
                    return "NeoContractInterface{" +
                            "hash='" + hash + '\'' +
                            ", methods=" + methods +
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

                    @JsonProperty("returnType")
                    private ContractParameterType returnType;

                    public ContractMethod() {
                    }

                    public ContractMethod(String name, List<ContractParameter> parameters, ContractParameterType returnType) {
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
                        if (!(o instanceof ContractMethod)) return false;
                        ContractMethod that = (ContractMethod) o;
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

                @JsonIgnoreProperties(ignoreUnknown = true)
                public static class ContractEvent {

                    @JsonProperty("name")
                    private String name;

                    @JsonProperty("parameters")
                    @JsonSetter(nulls = Nulls.AS_EMPTY)
                    private List<ContractParameter> parameters;

                    @JsonProperty("returnType")
                    private ContractParameterType returnType;

                    public ContractEvent() {
                    }

                    public ContractEvent(String name,
                                         List<ContractParameter> parameters,
                                         ContractParameterType returnType) {
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
                        if (!(o instanceof ContractEvent)) return false;
                        ContractEvent that = (ContractEvent) o;
                        return Objects.equals(getName(), that.getName()) &&
                                Objects.equals(getParameters(), that.getParameters()) &&
                                Objects.equals(getReturnType(), that.getReturnType());
                    }

                    @Override
                    public int hashCode() {
                        return Objects.hash(getName(), getParameters(), getReturnType());
                    }

                    @Override
                    public String toString() {
                        return "NeoContractEvent{" +
                                "name='" + name + '\'' +
                                ", parameters=" + parameters +
                                ", returnType=" + returnType +
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
                    if (this == o) return true;
                    if (!(o instanceof ContractPermission)) return false;
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
    }
}
