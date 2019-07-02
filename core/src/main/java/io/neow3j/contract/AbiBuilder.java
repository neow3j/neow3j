package io.neow3j.contract;

import io.neow3j.abi.NeoABIUtils;
import io.neow3j.abi.exceptions.NEP3Exception;
import io.neow3j.abi.model.NeoContractInterface;
import io.neow3j.protocol.Neow3j;

import java.io.File;

public class AbiBuilder {

    private Neow3j neow3j;
    private String contractScriptHash;
    private NeoContractInterface abi;

    public Neow3j getNeow3j() {
        return neow3j;
    }

    public String getContractScriptHash() {
        return contractScriptHash;
    }

    public NeoContractInterface getAbi() {
        return abi;
    }

    public AbiBuilder neow3j(Neow3j neow3j) {
        this.neow3j = neow3j;
        return this;
    }

    public AbiBuilder contractScriptHash(String contractScriptHash) {
        this.contractScriptHash = contractScriptHash;
        return this;
    }

    public AbiBuilder address(String address) {
        contractScriptHash(address);
        return this;
    }

    public AbiBuilder loadABIFile(String absoluteFileName) {
        try {
            this.abi = NeoABIUtils.loadABIFile(absoluteFileName);
        } catch (NEP3Exception e) {
            throw new IllegalStateException("NEP3 Exception when loading the ABI.", e);
        }
        return this;
    }

    public AbiBuilder loadABIFile(File source) {
        try {
            this.abi = NeoABIUtils.loadABIFile(source);
        } catch (NEP3Exception e) {
            throw new IllegalStateException("NEP3 Exception when loading the ABI.", e);
        }
        return this;
    }

    public Contract build() {
        throwIfNeow3jNotSet();
        return new Contract(this);
    }

    private void throwIfNeow3jNotSet() {
        if (neow3j == null) throw new IllegalStateException("Neow3j not set");
    }

}
