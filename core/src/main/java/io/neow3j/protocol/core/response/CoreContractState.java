package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.types.Hash160;

@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class CoreContractState extends ExpressContractState {

    @JsonProperty("id")
    private int id;

    @JsonProperty("nef")
    private ContractNef nef;

    public CoreContractState() {
    }

    public CoreContractState(int id, Hash160 hash, ContractNef nef, ContractManifest manifest) {
        super(hash, manifest);
        this.id = id;
        this.nef = nef;
    }

    public int getId() {
        return id;
    }

    public ContractNef getNef() {
        return nef;
    }

}
