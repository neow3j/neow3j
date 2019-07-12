package io.neow3j.contract;

import io.neow3j.model.types.ContractParameterType;
import io.neow3j.protocol.Neow3j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

public class ContractDeployment {

    private static final Logger LOG = LoggerFactory.getLogger(ContractDeployment.class);

    private Neow3j neow3j;

    private ContractDeployment(final Builder builder) {
        this.neow3j = builder.neow3j;
    }

    public Contract deploy() {
        // TODO: 2019-07-03 Guil: to be implemented
        return new Contract(null, null);
    }

    public static class Builder {

        private Neow3j neow3j;

        public Builder(final Neow3j neow3j) {
            this.neow3j = neow3j;
        }

        public Builder loadAVMFile(String absoluteFileName) {
            // TODO: 2019-07-03 Guil: to be implemented
            return this;
        }

        public Builder loadAVMFile(File source) {
            // TODO: 2019-07-03 Guil: to be implemented
            return this;
        }

        public Builder loadABIFile(String absoluteFileName) {
            // TODO: 2019-07-03 Guil: to be implemented
            return this;
        }

        public Builder loadABIFile(File source) {
            // TODO: 2019-07-03 Guil: to be implemented
            return this;
        }

        public Builder needsStorage() {
            // TODO: 2019-07-03 Guil: to be implemented
            return this;
        }

        public Builder needsDynamicInvoke() {
            // TODO: 2019-07-03 Guil: to be implemented
            return this;
        }

        public Builder isPayable() {
            // TODO: 2019-07-03 Guil: to be implemented
            return this;
        }

        public Builder parameter(ContractParameterType parameterType) {
            // TODO: 2019-07-03 Guil: to be implemented
            return this;
        }

        public Builder parameters(List<ContractParameterType> parameters) {
            // TODO: 2019-07-03 Guil: to be implemented
            return this;
        }

        public Builder returnType(ContractParameterType returnType) {
            // TODO: 2019-07-03 Guil: to be implemented
            return this;
        }

        public Builder name(String name) {
            // TODO: 2019-07-03 Guil: to be implemented
            return this;
        }

        public Builder version(String version) {
            // TODO: 2019-07-03 Guil: to be implemented
            return this;
        }

        public Builder author(String author) {
            // TODO: 2019-07-03 Guil: to be implemented
            return this;
        }

        public Builder email(String email) {
            // TODO: 2019-07-03 Guil: to be implemented
            return this;
        }

        public Builder description(String description) {
            // TODO: 2019-07-03 Guil: to be implemented
            return this;
        }

        public ContractDeployment build() {
            return new ContractDeployment(this);
        }

    }

}
