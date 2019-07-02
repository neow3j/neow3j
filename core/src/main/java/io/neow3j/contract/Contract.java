package io.neow3j.contract;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Contract {

    private static final Logger LOG = LoggerFactory.getLogger(Contract.class);

    private AbiBuilder abiBuilder;
    private InvocationBuilder invocationBuilder;
    private DeploymentBuilder deploymentBuilder;

    public Contract(final AbiBuilder builder) {
        this.abiBuilder = builder;
    }

    public Contract(final InvocationBuilder builder) {
        this.invocationBuilder = builder;
    }

    public Contract(final DeploymentBuilder builder) {
        this.deploymentBuilder = builder;
    }

    public Contract invoke() {
        // TODO: 2019-07-03 Guil: to be implemented
        return this;
    }

    public Contract deploy() {
        // TODO: 2019-07-03 Guil: to be implemented
        return this;
    }

    public static AbiBuilder abi() {
        return new AbiBuilder();
    }

    public InvocationBuilder invocation() {
        return new InvocationBuilder();
    }

    public static DeploymentBuilder deployment() {
        return new DeploymentBuilder();
    }


}
