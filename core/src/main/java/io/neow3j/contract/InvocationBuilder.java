package io.neow3j.contract;

import io.neow3j.model.types.ContractParameterType;
import io.neow3j.protocol.Neow3j;

public class InvocationBuilder {

    private Neow3j neow3j;

    public Neow3j getNeow3j() {
        return neow3j;
    }

    public InvocationBuilder neow3j(Neow3j neow3j) {
        this.neow3j = neow3j;
        return this;
    }

    public InvocationBuilder parameter(ContractParameterType parameterType, Object value) {
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
