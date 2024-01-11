package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.types.Hash160;

import java.math.BigInteger;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NativeContractState extends ExpressContractState {

    @JsonProperty("id")
    private BigInteger id;

    @JsonProperty("nef")
    private ContractNef nef;

    public NativeContractState() {
    }

    public NativeContractState(BigInteger id, Hash160 hash, ContractNef nef, ContractManifest manifest) {
        super(hash, manifest);
        this.id = id;
        this.nef = nef;
    }

    public BigInteger getId() {
        return id;
    }

    public ContractNef getNef() {
        return nef;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        NativeContractState that = (NativeContractState) o;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getNef(), that.getNef());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getId(), getNef());
    }

    @Override
    public String toString() {
        return "ContractState{" +
                "id=" + getId() +
                ", hash='" + getHash() + '\'' +
                ", nef=" + getNef() +
                ", manifest=" + getManifest() +
                '}';
    }
}
