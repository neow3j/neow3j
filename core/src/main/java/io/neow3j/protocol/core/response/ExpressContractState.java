package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.types.Hash160;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExpressContractState {

    @JsonProperty("hash")
    private Hash160 hash;

    @JsonProperty("manifest")
    private ContractManifest manifest;

    public ExpressContractState() {
    }

    public ExpressContractState(Hash160 hash, ContractManifest manifest) {
        this.hash = hash;
        this.manifest = manifest;
    }

    public Hash160 getHash() {
        return hash;
    }

    public ContractManifest getManifest() {
        return manifest;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ExpressContractState)) {
            return false;
        }
        ExpressContractState that = (ExpressContractState) o;
        return Objects.equals(getHash(), that.getHash()) &&
                Objects.equals(getManifest(), that.getManifest());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getHash(), getManifest());
    }

    @Override
    public String toString() {
        return "ContractState{" +
                "hash='" + hash + '\'' +
                ", manifest=" + manifest +
                '}';
    }

}
