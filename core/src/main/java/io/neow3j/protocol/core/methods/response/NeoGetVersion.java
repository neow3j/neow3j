package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.neow3j.protocol.core.Response;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class NeoGetVersion extends Response<NeoGetVersion.Result> {

    public NeoGetVersion.Result getVersion() {
        return getResult();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {

        @JsonProperty("port")
        private int port;

        @JsonProperty("nonce")
        private long nonce;

        @JsonProperty("useragent")
        private String userAgent;

        public Result() {
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public long getNonce() {
            return nonce;
        }

        public void setNonce(long nonce) {
            this.nonce = nonce;
        }

        public String getUserAgent() {
            return userAgent;
        }

        public void setUserAgent(String userAgent) {
            this.userAgent = userAgent;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Result)) return false;
            Result result = (Result) o;
            return getPort() == result.getPort() &&
                    getNonce() == result.getNonce() &&
                    Objects.equals(getUserAgent(), result.getUserAgent());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getPort(), getNonce(), getUserAgent());
        }

        @Override
        public String toString() {
            return "Result{" +
                    "port=" + port +
                    ", nonce=" + nonce +
                    ", userAgent='" + userAgent + '\'' +
                    '}';
        }
    }

}
