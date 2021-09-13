package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.protocol.core.Response;

import java.math.BigInteger;
import java.util.Objects;

public class NeoGetVersion extends Response<NeoGetVersion.NeoVersion> {

    public NeoVersion getVersion() {
        return getResult();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NeoVersion {

        @JsonProperty("tcpport")
        private Integer tcpPort;

        @JsonProperty("wsport")
        private Integer wsPort;

        @JsonProperty("nonce")
        private long nonce;

        @JsonProperty("useragent")
        private String userAgent;

        @JsonProperty("protocol")
        private Protocol protocol;

        public NeoVersion() {
        }

        public Integer getTCPPort() {
            return tcpPort;
        }

        public Integer getWSPort() {
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

        public Protocol getProtocol() {
            return protocol;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof NeoVersion)) return false;
            NeoVersion that = (NeoVersion) o;
            return Objects.equals(getTCPPort(), that.getTCPPort()) &&
                    Objects.equals(getWSPort(), that.getWSPort()) &&
                    getNonce() == that.getNonce() &&
                    Objects.equals(getUserAgent(), that.getUserAgent()) &&
                    Objects.equals(getProtocol(), that.getProtocol());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getTCPPort(), getWSPort(), getNonce(), getUserAgent(),
                    getProtocol());
        }

        @Override
        public String toString() {
            return "Result{" +
                    "tcpport=" + tcpPort +
                    ", wsport=" + wsPort +
                    ", nonce=" + nonce +
                    ", useragent='" + userAgent + '\'' +
                    ", protocol=" + protocol +
                    '}';
        }

        public static class Protocol {

            @JsonProperty("addressversion")
            private int addressVersion;

            @JsonProperty("network")
            private long network;

            @JsonProperty("msperblock")
            private long msPerBlock;

            @JsonProperty("maxtraceableblocks")
            private long maxTraceableBlocks;

            @JsonProperty("maxvaliduntilblockincrement")
            private long maxValidUntilBlockIncrement;

            @JsonProperty("maxtransactionsperblock")
            private long maxTransactionsPerBlock;

            @JsonProperty("memorypoolmaxtransactions")
            private Integer memoryPoolMaxTransactions;

            @JsonProperty("initialgasdistribution")
            private BigInteger initialGasDistribution;

            public Protocol() {
            }

            public int getAddressVersion() {
                return addressVersion;
            }

            public long getNetwork() {
                return network;
            }

            public long getMilliSecondsPerBlock() {
                return msPerBlock;
            }

            public long getMaxTraceableBlocks() {
                return maxTraceableBlocks;
            }

            public long getMaxValidUntilBlockIncrement() {
                return maxValidUntilBlockIncrement;
            }

            public long getMaxTransactionsPerBlock() {
                return maxTransactionsPerBlock;
            }

            public Integer getMemoryPoolMaxTransactions() {
                return memoryPoolMaxTransactions;
            }

            public BigInteger getInitialGasDistribution() {
                return initialGasDistribution;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof Protocol)) return false;
                Protocol that = (Protocol) o;
                return getAddressVersion() == that.getAddressVersion() &&
                        getNetwork() == that.getNetwork() &&
                        getMilliSecondsPerBlock() == that.getMilliSecondsPerBlock() &&
                        getMaxTraceableBlocks() == that.getMaxTraceableBlocks() &&
                        getMaxValidUntilBlockIncrement() == that.getMaxValidUntilBlockIncrement() &&
                        getMaxTransactionsPerBlock() == that.getMaxTransactionsPerBlock() &&
                        Objects.equals(getMemoryPoolMaxTransactions(),
                                that.getMemoryPoolMaxTransactions()) &&
                        Objects.equals(getInitialGasDistribution(),
                                that.getInitialGasDistribution());
            }

            @Override
            public int hashCode() {
                return Objects.hash(getAddressVersion(), getNetwork(), getMilliSecondsPerBlock(),
                        getMaxTraceableBlocks(), getMaxValidUntilBlockIncrement(),
                        getMaxTransactionsPerBlock(), getMemoryPoolMaxTransactions(),
                        getInitialGasDistribution());
            }

            @Override
            public String toString() {
                return "Protocol{" +
                        "addressVersion=" + addressVersion +
                        ", network=" + network +
                        ", milliSecondsPerBlock=" + msPerBlock +
                        ", maxTraceableBlocks=" + maxTraceableBlocks +
                        ", maxValidUntilBlockIncrement=" + maxValidUntilBlockIncrement +
                        ", maxTransactionsPerBlock=" + maxTransactionsPerBlock +
                        ", memoryPoolMaxTransactions=" + memoryPoolMaxTransactions +
                        ", initialGasDistribution=" + initialGasDistribution +
                        '}';
            }
        }
    }

}
