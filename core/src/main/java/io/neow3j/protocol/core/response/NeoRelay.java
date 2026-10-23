package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.protocol.core.Response;
import io.neow3j.protocol.exceptions.RpcResponseErrorException;
import io.neow3j.types.Hash256;

import java.util.Objects;

public class NeoRelay extends Response<NeoRelay.RelayedTransaction> {

    /**
     * @return the result.
     * @throws RpcResponseErrorException if the Neo node returned an error.
     */
    public RelayedTransaction getRelayedTransaction() {
        return getResult();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RelayedTransaction {

        @JsonProperty("hash")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private Hash256 hash;

        public RelayedTransaction() {
        }

        public RelayedTransaction(Hash256 hash) {
            this.hash = hash;
        }

        public Hash256 getHash() {
            return hash;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof RelayedTransaction)) {
                return false;
            }
            RelayedTransaction that = (RelayedTransaction) o;
            return Objects.equals(getHash(), that.getHash());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getHash());
        }

        @Override
        public String toString() {
            return "RelayedTransaction{" +
                    "hash='" + hash + '\'' +
                    '}';
        }

    }

}
