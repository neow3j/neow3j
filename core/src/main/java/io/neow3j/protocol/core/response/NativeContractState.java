package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.types.Hash160;

import java.math.BigInteger;

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

}
