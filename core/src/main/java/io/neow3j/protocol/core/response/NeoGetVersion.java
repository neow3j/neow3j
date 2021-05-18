package io.neow3j.protocol.core.response;

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

        @JsonProperty("tcpport")
        private int tcpPort;

        @JsonProperty("wsport")
        private int wsPort;

        @JsonProperty("nonce")
        private long nonce;

        @JsonProperty("useragent")
        private String userAgent;

        @JsonProperty("network")
        private int network;

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

        public int getNetwork() {
            return network;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Result)) return false;
            Result that = (Result) o;
            return getTCPPort() == that.getTCPPort() &&
                    getWSPort() == that.getWSPort() &&
                    getNonce() == that.getNonce() &&
                    Objects.equals(getUserAgent(), that.getUserAgent()) &&
                    getNetwork() == that.getNetwork();
        }

        @Override
        public int hashCode() {
            return Objects.hash(getTCPPort(), getWSPort(), getNonce(), getUserAgent(), getNetwork());
        }

        @Override
        public String toString() {
            return "Result{" +
                    "tcpport=" + tcpPort +
                    ", wsport=" + wsPort +
                    ", nonce=" + nonce +
                    ", useragent='" + userAgent + '\'' +
                    ", network=" + network +
                    '}';
        }
    }

}
