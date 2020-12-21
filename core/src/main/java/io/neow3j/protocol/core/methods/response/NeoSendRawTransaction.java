package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.protocol.core.Response;

public class NeoSendRawTransaction extends Response<NeoSendRawTransaction.RawTransaction> {

    public RawTransaction getSendRawTransaction() {
        return getResult();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RawTransaction {

        @JsonProperty("hash")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String hash;

        public RawTransaction() {
        }

        public RawTransaction(String hash) {
            this.hash = hash;
        }

        public String getHash() {
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
