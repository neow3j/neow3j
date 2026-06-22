package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import io.neow3j.protocol.core.Response;
import io.neow3j.protocol.exceptions.RpcResponseErrorException;
import io.neow3j.types.Hash256;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NeoGetPendingValidUntilRelay extends Response<NeoGetPendingValidUntilRelay.PendingValidUntilRelay> {

    /**
     * @return the result.
     * @throws RpcResponseErrorException if the Neo node returned an error.
     */
    public PendingValidUntilRelay getPendingValidUntilRelay() {
        return getResult();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PendingValidUntilRelay {

        @JsonProperty("height")
        private BigInteger height;

        @JsonProperty("maxvaliduntilblockincrement")
        private Long maxValidUntilBlockIncrement;

        @JsonProperty("pending")
        @JsonSetter(nulls = Nulls.AS_EMPTY)
        private List<PendingTransaction> pending = new ArrayList<>();

        @JsonProperty("enabled")
        private Boolean enabled;

        @JsonProperty("pendingcheckfrequency")
        private Integer pendingCheckFrequency;

        @JsonProperty("pendingrelaymaxtransactions")
        private Integer pendingRelayMaxTransactions;

        @JsonProperty("count")
        private Integer count;

        public PendingValidUntilRelay() {
        }

        public PendingValidUntilRelay(BigInteger height, Long maxValidUntilBlockIncrement,
                List<PendingTransaction> pending, Boolean enabled, Integer pendingCheckFrequency,
                Integer pendingRelayMaxTransactions, Integer count) {
            this.height = height;
            this.maxValidUntilBlockIncrement = maxValidUntilBlockIncrement;
            this.pending = pending;
            this.enabled = enabled;
            this.pendingCheckFrequency = pendingCheckFrequency;
            this.pendingRelayMaxTransactions = pendingRelayMaxTransactions;
            this.count = count;
        }

        public BigInteger getHeight() {
            return height;
        }

        public Long getMaxValidUntilBlockIncrement() {
            return maxValidUntilBlockIncrement;
        }

        public List<PendingTransaction> getPending() {
            return pending;
        }

        public Boolean getEnabled() {
            return enabled;
        }

        public Integer getPendingCheckFrequency() {
            return pendingCheckFrequency;
        }

        public Integer getPendingRelayMaxTransactions() {
            return pendingRelayMaxTransactions;
        }

        public Integer getCount() {
            return count;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof PendingValidUntilRelay)) {
                return false;
            }
            PendingValidUntilRelay that = (PendingValidUntilRelay) o;
            return Objects.equals(getHeight(), that.getHeight()) &&
                    Objects.equals(getMaxValidUntilBlockIncrement(), that.getMaxValidUntilBlockIncrement()) &&
                    Objects.equals(getPending(), that.getPending()) &&
                    Objects.equals(getEnabled(), that.getEnabled()) &&
                    Objects.equals(getPendingCheckFrequency(), that.getPendingCheckFrequency()) &&
                    Objects.equals(getPendingRelayMaxTransactions(), that.getPendingRelayMaxTransactions()) &&
                    Objects.equals(getCount(), that.getCount());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getHeight(), getMaxValidUntilBlockIncrement(), getPending(), getEnabled(),
                    getPendingCheckFrequency(), getPendingRelayMaxTransactions(), getCount());
        }

        @Override
        public String toString() {
            return "PendingValidUntilRelay{" +
                    "height=" + height +
                    ", maxValidUntilBlockIncrement=" + maxValidUntilBlockIncrement +
                    ", pending=" + pending +
                    ", enabled=" + enabled +
                    ", pendingCheckFrequency=" + pendingCheckFrequency +
                    ", pendingRelayMaxTransactions=" + pendingRelayMaxTransactions +
                    ", count=" + count +
                    '}';
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class PendingTransaction {

            @JsonProperty("hash")
            private Hash256 hash;

            @JsonProperty("validuntilblock")
            private BigInteger validUntilBlock;

            @JsonProperty("size")
            private Integer size;

            @JsonProperty("blocksuntildeadline")
            private BigInteger blocksUntilDeadline;

            public PendingTransaction() {
            }

            public PendingTransaction(Hash256 hash, BigInteger validUntilBlock, Integer size,
                    BigInteger blocksUntilDeadline) {
                this.hash = hash;
                this.validUntilBlock = validUntilBlock;
                this.size = size;
                this.blocksUntilDeadline = blocksUntilDeadline;
            }

            public Hash256 getHash() {
                return hash;
            }

            public BigInteger getValidUntilBlock() {
                return validUntilBlock;
            }

            public Integer getSize() {
                return size;
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
                PendingTransaction that = (PendingTransaction) o;
                return Objects.equals(getHash(), that.getHash()) &&
                        Objects.equals(getValidUntilBlock(), that.getValidUntilBlock()) &&
                        Objects.equals(getSize(), that.getSize()) &&
                        Objects.equals(getBlocksUntilDeadline(), that.getBlocksUntilDeadline());
            }

            @Override
            public int hashCode() {
                return Objects.hash(getHash(), getValidUntilBlock(), getSize(), getBlocksUntilDeadline());
            }

            @Override
            public String toString() {
                return "PendingTransaction{" +
                        "hash='" + hash + '\'' +
                        ", validUntilBlock=" + validUntilBlock +
                        ", size=" + size +
                        ", blocksUntilDeadline=" + blocksUntilDeadline +
                        '}';
            }

        }

    }

}
