package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.types.Hash160;

import java.math.BigInteger;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ContractState extends NativeContractState {

    @JsonProperty("updatecounter")
    private Integer updateCounter;

    public ContractState() {
        super();
    }

    public ContractState(BigInteger id, int updateCounter, Hash160 hash, ContractNef nef, ContractManifest manifest) {
        super(id, hash, nef, manifest);
        this.updateCounter = updateCounter;
    }

    public Integer getUpdateCounter() {
        return updateCounter;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getHash(), getNef(), getManifest(), getUpdateCounter());
    }

    @Override
    public String toString() {
        return "ContractState{" +
                "id=" + getId() +
                ", updateCounter=" + getUpdateCounter() +
                ", hash='" + getHash() + '\'' +
                ", nef=" + getNef() +
                ", manifest=" + getManifest() +
                '}';
    }

}
