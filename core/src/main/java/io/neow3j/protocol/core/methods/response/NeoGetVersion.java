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

        @JsonProperty("tcpPort")
        private int tcpPort;

        @JsonProperty("wsPort")
        private int wsPort;

        @JsonProperty("nonce")
        private long nonce;

        @JsonProperty("userAgent")
        private String userAgent;

        public Result() {
        }

        public int getTCPPort() {
            return tcpPort;
        }

        public int getWSPort() {
            return wsPort;
        }

        public void setTCPPort(int tcpPort) {
            this.tcpPort = tcpPort;
        }

        public void setWSPort(int wsPort) {
            this.wsPort = wsPort;
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
            return getTCPPort() == result.getTCPPort() &&
                    getWSPort() == result.getWSPort() &&
                    getNonce() == result.getNonce() &&
                    Objects.equals(getUserAgent(), result.getUserAgent());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getTCPPort(), getWSPort(), getNonce(), getUserAgent());
        }

        @Override
        public String toString() {
            return "Result{" +
                    "tcpPort=" + tcpPort +
                    ", wsPort=" + wsPort +
                    ", nonce=" + nonce +
                    ", userAgent='" + userAgent + '\'' +
                    '}';
        }
    }

}
