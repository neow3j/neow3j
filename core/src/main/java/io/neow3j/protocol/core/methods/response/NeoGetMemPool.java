package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import io.neow3j.protocol.core.Response;
import io.neow3j.protocol.core.methods.response.NeoGetNep5Balances.Nep5Balance;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

public class NeoGetMemPool extends Response<NeoGetMemPool.MemPoolDetails> {

    public NeoGetMemPool.MemPoolDetails getMemPoolDetails() {
        return getResult();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MemPoolDetails {

        @JsonProperty("height")
        private BigInteger height;

        @JsonProperty("verified")
        @JsonSetter(nulls = Nulls.AS_EMPTY)
        private List<String> verified;

        @JsonProperty("unverified")
        @JsonSetter(nulls = Nulls.AS_EMPTY)
        private List<String> unverified;

        public MemPoolDetails() {
        }

        public MemPoolDetails(BigInteger height, List<String> verified,
                List<String> unverified) {
            this.height = height;
            this.verified = verified;
            this.unverified = unverified;
        }

        public BigInteger getHeight() {
            return height;
        }

        public List<String> getVerified() {
            return verified;
        }

        public List<String> getUnverified() {
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
