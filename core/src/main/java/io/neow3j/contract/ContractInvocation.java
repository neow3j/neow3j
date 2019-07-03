package io.neow3j.contract;

import io.neow3j.model.types.ContractParameterType;
import io.neow3j.protocol.Neow3j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContractInvocation {

    private static final Logger LOG = LoggerFactory.getLogger(ContractInvocation.class);

    private Neow3j neow3j;
    private Contract contract;

    private ContractInvocation(final Builder builder) {
        this.neow3j = builder.neow3j;
        this.contract = builder.contract;
    }

    public ContractInvocation invoke() {
        // TODO: 2019-07-03 Guil: to be implemented
        return this;
    }

    public static class Builder {

        private Neow3j neow3j;
        private Contract contract;

        public Builder(Neow3j neow3j, Contract contract) {
            this.neow3j = neow3j;
            this.contract = contract;
        }

        public Builder parameter(ContractParameterType parameterType, Object value) {
            // TODO: 2019-07-03 Guil: to be implemented
            return this;
        }

        public ContractInvocation build() {
            return new ContractInvocation(this);
        }

    }

}
