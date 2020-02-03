package io.neow3j.contract.abi.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"hash", "entrypoint", "methods", "events"})
public class NeoContractInterface {

    @JsonProperty("hash")
    private String hash;

    @JsonProperty("entrypoint")
    private String entryPoint;

    @JsonProperty("methods")
    @JsonAlias({"functions"})
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<NeoContractMethod> functions;

    @JsonProperty("events")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<NeoContractEvent> events;

    public NeoContractInterface() {
    }

    public NeoContractInterface(String hash, String entryPoint, List<NeoContractMethod> functions, List<NeoContractEvent> events) {
        this.hash = hash;
        this.entryPoint = entryPoint;
        this.functions = functions != null ? functions : new ArrayList<>();
        this.events = events != null ? events : new ArrayList<>();
    }

    public String getHash() {
        return hash;
    }

    public String getEntryPoint() {
        return entryPoint;
    }

    public List<NeoContractMethod> getMethods() {
        return functions;
    }

    public List<NeoContractEvent> getEvents() {
        return events;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NeoContractInterface)) return false;
        NeoContractInterface that = (NeoContractInterface) o;
        return Objects.equals(getHash(), that.getHash()) &&
                Objects.equals(getEntryPoint(), that.getEntryPoint()) &&
                Objects.equals(getMethods(), that.getMethods()) &&
                Objects.equals(getEvents(), that.getEvents());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getHash(), getEntryPoint(), getMethods(), getEvents());
    }

    @Override
    public String toString() {
        return "NeoContractInterface{" +
                "hash='" + hash + '\'' +
                ", entryPoint='" + entryPoint + '\'' +
                ", functions=" + functions +
                ", events=" + events +
                '}';
    }
}
