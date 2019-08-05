package io.neow3j.contract;

import io.neow3j.contract.abi.NeoABIUtils;
import io.neow3j.contract.abi.exceptions.NEP3Exception;
import io.neow3j.contract.abi.model.NeoContractInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class ContractAbiLoader {

    private static final Logger LOG = LoggerFactory.getLogger(ContractAbiLoader.class);

    private ScriptHash contractScriptHash;
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

        private ScriptHash contractScriptHash;
        private NeoContractInterface abi;

        public Builder() {

        }

        /**
         * Adds the given script hash to this ABI loader.
         *
         * @param contractScriptHash the script hash in big-endian order.
         * @return this Builder object.
         * @deprecated Use {@link Builder#contractScriptHash(ScriptHash)} instead.
         */
        @Deprecated
        public Builder contractScriptHash(String contractScriptHash) {
            this.contractScriptHash = new ScriptHash(contractScriptHash);
            return this;
        }

        /**
         * Adds the given script hash to this ABI loader.
         *
         * @param contractScriptHash the script hash.
         * @return this Builder object.
         */
        public Builder contractScriptHash(ScriptHash contractScriptHash) {
            this.contractScriptHash = contractScriptHash;
            return this;
        }

        public Builder address(String address) {
            this.contractScriptHash = ScriptHash.fromAddress(address);
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
