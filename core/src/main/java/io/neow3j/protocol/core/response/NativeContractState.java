package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.types.Hash160;

import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NativeContractState extends CoreContractState {

    @JsonProperty("updatehistory")
    private List<Integer> updateHistory;

    public NativeContractState() {
        super();
    }

    public NativeContractState(int id, Hash160 hash, ContractNef nef, ContractManifest manifest,
            List<Integer> updateHistory) {

        super(id, hash, nef, manifest);
        this.updateHistory = updateHistory;
    }

    public List<Integer> getUpdateHistory() {
        return updateHistory;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getHash(), getNef(), getManifest(), getUpdateHistory());
    }

    @Override
    public String toString() {
        return "ContractState{" +
                "id=" + getId() +
                ", hash='" + getHash() + '\'' +
                ", nef=" + getNef() +
                ", manifest=" + getManifest() +
                ", updateHistory=" + updateHistory +
                '}';
    }

}
