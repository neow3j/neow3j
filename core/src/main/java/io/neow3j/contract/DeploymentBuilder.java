package io.neow3j.contract;

import io.neow3j.model.types.ContractParameterType;
import io.neow3j.protocol.Neow3j;

import java.io.File;
import java.util.List;

public class DeploymentBuilder {

    private Neow3j neow3j;

    public Neow3j getNeow3j() {
        return neow3j;
    }

    public DeploymentBuilder neow3j(Neow3j neow3j) {
        this.neow3j = neow3j;
        return this;
    }

    public DeploymentBuilder loadAVMFile(String absoluteFileName) {
        // TODO: 2019-07-03 Guil: to be implemented
        return this;
    }

    public DeploymentBuilder loadAVMFile(File source) {
        // TODO: 2019-07-03 Guil: to be implemented
        return this;
    }

    public DeploymentBuilder needsStorage() {
        // TODO: 2019-07-03 Guil: to be implemented
        return this;
    }

    public DeploymentBuilder needsDynamicInvoke() {
        // TODO: 2019-07-03 Guil: to be implemented
        return this;
    }

    public DeploymentBuilder isPayable() {
        // TODO: 2019-07-03 Guil: to be implemented
        return this;
    }

    public DeploymentBuilder addParameter(ContractParameterType parameterType) {
        // TODO: 2019-07-03 Guil: to be implemented
        return this;
    }

    public DeploymentBuilder parameters(List<ContractParameterType> parameters) {
        // TODO: 2019-07-03 Guil: to be implemented
        return this;
    }

    public DeploymentBuilder returnType(ContractParameterType returnType) {
        // TODO: 2019-07-03 Guil: to be implemented
        return this;
    }

    public DeploymentBuilder name(String  name) {
        // TODO: 2019-07-03 Guil: to be implemented
        return this;
    }

    public DeploymentBuilder version(String version) {
        // TODO: 2019-07-03 Guil: to be implemented
        return this;
    }

    public DeploymentBuilder author(String author) {
        // TODO: 2019-07-03 Guil: to be implemented
        return this;
    }

    public DeploymentBuilder email(String email) {
        // TODO: 2019-07-03 Guil: to be implemented
        return this;
    }

    public DeploymentBuilder description(String description) {
        // TODO: 2019-07-03 Guil: to be implemented
        return this;
    }

    public Contract build() {
        throwIfNeow3jNotSet();
        return new Contract(this);
    }

    private void throwIfNeow3jNotSet() {
        if (neow3j == null) {
            throw new IllegalStateException("Neow3j object is not set.");
        }
    }

}
