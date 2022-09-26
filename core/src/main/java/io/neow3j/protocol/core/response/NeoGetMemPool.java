package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import io.neow3j.protocol.exceptions.RpcResponseErrorException;
import io.neow3j.types.Hash256;
import io.neow3j.protocol.core.Response;

import java.util.List;
import java.util.Objects;

public class NeoGetMemPool extends Response<NeoGetMemPool.MemPoolDetails> {

    /**
     * @return the result.
     * @throws RpcResponseErrorException if the Neo node returned an error.
     */
    public NeoGetMemPool.MemPoolDetails getMemPoolDetails() {
        return getResult();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MemPoolDetails {

        @JsonProperty("height")
        private Long height;

        @JsonProperty("verified")
        @JsonSetter(nulls = Nulls.AS_EMPTY)
        private List<Hash256> verified;

        @JsonProperty("unverified")
        @JsonSetter(nulls = Nulls.AS_EMPTY)
        private List<Hash256> unverified;

        public MemPoolDetails() {
        }

        public MemPoolDetails(Long height, List<Hash256> verified, List<Hash256> unverified) {
            this.height = height;
            this.verified = verified;
            this.unverified = unverified;
        }

        public Long getHeight() {
            return height;
        }

        public List<Hash256> getVerified() {
            return verified;
        }

        public List<Hash256> getUnverified() {
            return unverified;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof MemPoolDetails)) {
                return false;
            }
            MemPoolDetails that = (MemPoolDetails) o;
            return Objects.equals(getHeight(), that.getHeight()) &&
                    Objects.equals(getVerified(), that.getVerified()) &&
                    Objects.equals(getUnverified(), that.getUnverified());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getHeight(), getVerified(), getUnverified());
        }

        @Override
        public String toString() {
            return "MemPoolDetails{" +
                    "height=" + height +
                    ", verified=" + verified +
                    ", unverified=" + unverified +
                    '}';
        }
    }

}
