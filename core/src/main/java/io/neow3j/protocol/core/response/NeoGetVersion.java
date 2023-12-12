package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import io.neow3j.protocol.core.Response;
import io.neow3j.protocol.exceptions.RpcResponseErrorException;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NeoGetVersion extends Response<NeoGetVersion.NeoVersion> {

    /**
     * @return the result.
     * @throws RpcResponseErrorException if the Neo node returned an error.
     */
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
        private Long nonce;

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

        public Long getNonce() {
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
            if (this == o) {
                return true;
            }
            if (!(o instanceof NeoVersion)) {
                return false;
            }
            NeoVersion that = (NeoVersion) o;
            return Objects.equals(getTCPPort(), that.getTCPPort()) &&
                    Objects.equals(getWSPort(), that.getWSPort()) &&
                    Objects.equals(getNonce(), that.getNonce()) &&
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

            @JsonProperty("network")
            private Long network;

            @JsonProperty("validatorscount")
            private Integer validatorsCount;

            @JsonProperty("msperblock")
            private Long msPerBlock;

            @JsonProperty("maxvaliduntilblockincrement")
            private Long maxValidUntilBlockIncrement;

            @JsonProperty("maxtraceableblocks")
            private Long maxTraceableBlocks;

            @JsonProperty("addressversion")
            private Integer addressVersion;

            @JsonProperty("maxtransactionsperblock")
            private Long maxTransactionsPerBlock;

            @JsonProperty("memorypoolmaxtransactions")
            private Integer memoryPoolMaxTransactions;

            @JsonProperty("initialgasdistribution")
            private BigInteger initialGasDistribution;

            @JsonProperty("hardforks")
            @JsonSetter(nulls = Nulls.AS_EMPTY)
            @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            private List<Hardforks> hardforks = new ArrayList<>();

            public Protocol() {
            }

            public Long getNetwork() {
                return network;
            }

            public Integer getValidatorsCount() {
                return validatorsCount;
            }

            public Long getMilliSecondsPerBlock() {
                return msPerBlock;
            }

            public Long getMaxValidUntilBlockIncrement() {
                return maxValidUntilBlockIncrement;
            }

            public Long getMaxTraceableBlocks() {
                return maxTraceableBlocks;
            }

            public Integer getAddressVersion() {
                return addressVersion;
            }

            public Long getMaxTransactionsPerBlock() {
                return maxTransactionsPerBlock;
            }

            public Integer getMemoryPoolMaxTransactions() {
                return memoryPoolMaxTransactions;
            }

            public BigInteger getInitialGasDistribution() {
                return initialGasDistribution;
            }

            public List<Hardforks> getHardforks() {
                return hardforks;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) {
                    return true;
                }
                if (!(o instanceof Protocol)) {
                    return false;
                }
                Protocol that = (Protocol) o;
                return Objects.equals(getNetwork(), that.getNetwork()) &&
                        Objects.equals(getValidatorsCount(), that.getValidatorsCount()) &&
                        Objects.equals(getMilliSecondsPerBlock(), that.getMilliSecondsPerBlock()) &&
                        Objects.equals(getMaxValidUntilBlockIncrement(), that.getMaxValidUntilBlockIncrement()) &&
                        Objects.equals(getMaxTraceableBlocks(), that.getMaxTraceableBlocks()) &&
                        Objects.equals(getAddressVersion(), that.getAddressVersion()) &&
                        Objects.equals(getMaxTransactionsPerBlock(), that.getMaxTransactionsPerBlock()) &&
                        Objects.equals(getMemoryPoolMaxTransactions(), that.getMemoryPoolMaxTransactions()) &&
                        Objects.equals(getInitialGasDistribution(), that.getInitialGasDistribution()) &&
                        Objects.equals(getHardforks(), that.getHardforks());
            }

            @Override
            public int hashCode() {
                return Objects.hash(getNetwork(), getValidatorsCount(), getMilliSecondsPerBlock(),
                        getMaxValidUntilBlockIncrement(), getMaxTraceableBlocks(), getAddressVersion(),
                        getMaxTransactionsPerBlock(), getMemoryPoolMaxTransactions(), getInitialGasDistribution(),
                        getHardforks());
            }

            @Override
            public String toString() {
                return "Protocol{" +
                        "network=" + network +
                        ", validatorsCount=" + validatorsCount +
                        ", milliSecondsPerBlock=" + msPerBlock +
                        ", maxValidUntilBlockIncrement=" + maxValidUntilBlockIncrement +
                        ", maxTraceableBlocks=" + maxTraceableBlocks +
                        ", addressVersion=" + addressVersion +
                        ", maxTransactionsPerBlock=" + maxTransactionsPerBlock +
                        ", memoryPoolMaxTransactions=" + memoryPoolMaxTransactions +
                        ", initialGasDistribution=" + initialGasDistribution +
                        ", hardforks=" + hardforks +
                        '}';
            }

            public static class Hardforks {

                @JsonProperty("name")
                private String name;

                @JsonProperty("blockheight")
                private BigInteger blockHeight;

                public Hardforks() {
                }

                public String getName() {
                    return name;
                }

                public BigInteger getBlockHeight() {
                    return blockHeight;
                }

                @Override
                public boolean equals(Object o) {
                    if (this == o) {
                        return true;
                    }
                    if (!(o instanceof Hardforks)) {
                        return false;
                    }
                    Hardforks that = (Hardforks) o;
                    return Objects.equals(getName(), that.getName()) &&
                            Objects.equals(getBlockHeight(), that.getBlockHeight());
                }

                @Override
                public int hashCode() {
                    return Objects.hash(getName(), getBlockHeight());
                }

                @Override
                public String toString() {
                    return "Hardforks{" +
                            "name=" + name +
                            ", blockheight=" + blockHeight +
                            '}';
                }
            }
        }

    }

}
