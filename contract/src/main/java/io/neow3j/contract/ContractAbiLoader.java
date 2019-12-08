package io.neow3j.contract;

import io.neow3j.contract.abi.NeoABIUtils;
import io.neow3j.contract.abi.exceptions.NEP3Exception;
import io.neow3j.contract.abi.model.NeoContractInterface;
import io.neow3j.utils.Numeric;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContractAbiLoader {

    private static final Logger LOG = LoggerFactory.getLogger(ContractAbiLoader.class);

    private ScriptHash contractScriptHash;
    private NeoContractInterface abi;

    private ContractAbiLoader(final Builder builder) {
        this.contractScriptHash = builder.contractScriptHash;
        this.abi = builder.abi;
    }

    public Contract load() {
        if (this.contractScriptHash == null) {
            if (this.abi == null) {
                throw new IllegalStateException(
                    "Either contract script hash or ABI should be set.");
            } else {
                String contractScriptHashNoPrefix = Numeric.cleanHexPrefix(this.abi.getHash());
                this.contractScriptHash = new ScriptHash(contractScriptHashNoPrefix);
            }
        } else {
            if (this.abi != null) {
                String abiContractScriptHashNoPrefix = Numeric.cleanHexPrefix(this.abi.getHash());
                if (!abiContractScriptHashNoPrefix.equals(this.contractScriptHash)) {
                    throw new IllegalStateException(
                        "Mismatch between the contract script hash provided "
                            + "and the contract script hash found in the specified ABI file.");
                }
            }
        }
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
         * @param contractScriptHash the script hash.
         * @return this Builder object.
         */
        public Builder contractScriptHash(ScriptHash contractScriptHash) {
            this.contractScriptHash = contractScriptHash;
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
