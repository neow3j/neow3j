package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.contract.Hash256;
import io.neow3j.protocol.core.Response;

public class NeoSendRawTransaction extends Response<NeoSendRawTransaction.RawTransaction> {

    public RawTransaction getSendRawTransaction() {
        return getResult();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RawTransaction {

        @JsonProperty("hash")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private Hash256 hash;

        public RawTransaction() {
        }

        public RawTransaction(Hash256 hash) {
            this.hash = hash;
        }

        public Hash256 getHash() {
            return hash;
        }

        @Override
        public String toString() {
            return "RawTransaction{" +
                    "hash='" + hash + '\'' +
                    '}';
        }
    }

}
