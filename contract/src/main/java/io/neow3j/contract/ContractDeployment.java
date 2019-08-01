package io.neow3j.contract;

import io.neow3j.contract.abi.NeoABIUtils;
import io.neow3j.contract.abi.exceptions.NEP3Exception;
import io.neow3j.contract.abi.model.NeoContractInterface;
import io.neow3j.model.types.ContractParameterType;
import io.neow3j.protocol.Neow3j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ContractDeployment {

    private static final Logger LOG = LoggerFactory.getLogger(ContractDeployment.class);

    private Neow3j neow3j;
    private NeoContractInterface abi;
    private ContractDeploymentScript deploymentScript;

    private ContractDeployment(final Builder builder) {
        this.neow3j = builder.neow3j;
        this.deploymentScript = builder.deploymentScript;
        this.abi = builder.abi;
    }

    public Contract deploy() {
        // TODO: 2019-07-24 Guil:
        // Here we need to send the transaction!
        return new Contract(this.deploymentScript, this.abi);
    }

    public static class Builder {

        private Neow3j neow3j;
        private String name;
        private String version;
        private String author;
        private String email;
        private String description;
        private List<ContractParameterType> parameters;
        private ContractParameterType returnType;
        private boolean needsStorage;
        private boolean needsDynamicInvoke;
        private boolean isPayable;
        private byte[] scriptBinary;
        private NeoContractInterface abi;
        private ContractDeploymentScript deploymentScript;

        public Builder(final Neow3j neow3j) {
            this.neow3j = neow3j;
            this.parameters = new ArrayList<>();
        }

        public Builder loadAVMFile(String absoluteFileName) throws IOException {
            loadAVMFile(new File(absoluteFileName));
            return this;
        }

        public Builder loadAVMFile(File source) throws IOException {
            this.scriptBinary = Files.readAllBytes(source.toPath());
            return this;
        }

        public Builder loadAVMFile(InputStream source) throws IOException {
            this.scriptBinary = new byte[source.available()];
            source.read(scriptBinary);
            return this;
        }

        public Builder loadABIFile(String absoluteFileName) throws NEP3Exception {
            this.abi = NeoABIUtils.loadABIFile(absoluteFileName);
            return this;
        }

        public Builder loadABIFile(File source) throws NEP3Exception {
            this.abi = NeoABIUtils.loadABIFile(source);
            return this;
        }

        public Builder loadABIFile(InputStream source) throws NEP3Exception {
            this.abi = NeoABIUtils.loadABIFile(source);
            return this;
        }

        public Builder needsStorage(boolean needsStorage) {
            this.needsStorage = needsStorage;
            return this;
        }

        public Builder needsStorage() {
            this.needsStorage = true;
            return this;
        }

        public Builder needsDynamicInvoke(boolean needsDynamicInvoke) {
            this.needsDynamicInvoke = needsDynamicInvoke;
            return this;
        }

        public Builder needsDynamicInvoke() {
            this.needsDynamicInvoke = true;
            return this;
        }

        public Builder isPayable(boolean isPayable) {
            this.isPayable = isPayable;
            return this;
        }

        public Builder isPayable() {
            this.isPayable = true;
            return this;
        }

        public Builder parameter(ContractParameterType parameterType) {
            this.parameters.add(parameterType);
            return this;
        }

        public Builder parameters(List<ContractParameterType> parameters) {
            this.parameters = parameters;
            return this;
        }

        public Builder returnType(ContractParameterType returnType) {
            this.returnType = returnType;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder author(String author) {
            this.author = author;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public ContractDeployment build() {
            ContractDescriptionProperties cdp = new ContractDescriptionProperties(
                    this.name, this.version, this.author, this.email, this.description);
            ContractFunctionProperties cfp = new ContractFunctionProperties(
                    this.parameters, this.returnType, this.needsStorage, this.needsDynamicInvoke, this.isPayable);
            this.deploymentScript = new ContractDeploymentScript(this.scriptBinary, cfp, cdp);
            return new ContractDeployment(this);
        }

    }

}
