package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.protocol.core.Response;
import io.neow3j.protocol.exceptions.RpcResponseErrorException;

import java.math.BigInteger;
import java.util.Objects;

public class NeoGetPendingTransaction extends Response<NeoGetPendingTransaction.PendingTransaction> {

    /**
     * @return the result.
     * @throws RpcResponseErrorException if the Neo node returned an error.
     */
    public PendingTransaction getPendingTransaction() {
        return getResult();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PendingTransaction extends Transaction {

        @JsonProperty("blocksuntildeadline")
        private BigInteger blocksUntilDeadline;

        public PendingTransaction() {
        }

        public BigInteger getBlocksUntilDeadline() {
            return blocksUntilDeadline;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof PendingTransaction)) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }
            PendingTransaction that = (PendingTransaction) o;
            return Objects.equals(getBlocksUntilDeadline(), that.getBlocksUntilDeadline());
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), getBlocksUntilDeadline());
        }

        @Override
        public String toString() {
            return "PendingTransaction{" +
                    "transaction=" + super.toString() +
                    ", blocksUntilDeadline=" + blocksUntilDeadline +
                    '}';
        }

    }

}
