package io.neow3j.contract;

import io.neow3j.abi.NeoABIUtils;
import io.neow3j.abi.exceptions.NEP3Exception;
import io.neow3j.abi.model.NeoContractInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class ContractAbiLoader {

    private static final Logger LOG = LoggerFactory.getLogger(ContractAbiLoader.class);

    private String contractScriptHash;
    private NeoContractInterface abi;

    private ContractAbiLoader(final Builder builder) {
        this.contractScriptHash = builder.contractScriptHash;
        this.abi = builder.abi;
    }

    public Contract load() {
        // TODO: 2019-07-03 Guil: to be implemented
        return new Contract(this.contractScriptHash, this.abi);
    }

    public static class Builder {

        private String contractScriptHash;
        private NeoContractInterface abi;

        public Builder() {

        }

        public Builder contractScriptHash(String contractScriptHash) {
            this.contractScriptHash = contractScriptHash;
            return this;
        }

        public Builder address(String address) {
            contractScriptHash(address);
            return this;
        }

        public Builder loadABIFile(String absoluteFileName) {
            try {
                this.abi = NeoABIUtils.loadABIFile(absoluteFileName);
            } catch (NEP3Exception e) {
                throw new IllegalStateException("NEP3 Exception when loading the ABI.", e);
            }
            return this;
        }

        public Builder loadABIFile(File source) {
            try {
                this.abi = NeoABIUtils.loadABIFile(source);
            } catch (NEP3Exception e) {
                throw new IllegalStateException("NEP3 Exception when loading the ABI.", e);
            }
            return this;
        }

        public ContractAbiLoader build() {
            return new ContractAbiLoader(this);
        }
    }

}
