package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import io.neow3j.crypto.ECKeyPair.ECPublicKey;
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

        @JsonProperty("nonce")
        private Long nonce;

        @JsonProperty("useragent")
        private String userAgent;

        @JsonProperty("rpc")
        private Rpc rpc;

        @JsonProperty("protocol")
        private Protocol protocol;

        public NeoVersion() {
        }

        public Integer getTCPPort() {
            return tcpPort;
        }

        public void setTCPPort(int tcpPort) {
            this.tcpPort = tcpPort;
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

        public Rpc getRpc() {
            return rpc;
        }

        public Protocol getProtocol() {
            return protocol;
        }

        public void setProtocol(Protocol protocol) {
            this.protocol = protocol;
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
                    Objects.equals(getNonce(), that.getNonce()) &&
                    Objects.equals(getUserAgent(), that.getUserAgent()) &&
                    Objects.equals(getRpc(), that.getRpc()) &&
                    Objects.equals(getProtocol(), that.getProtocol());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getTCPPort(), getNonce(), getUserAgent(), getRpc(), getProtocol());
        }

        @Override
        public String toString() {
            return "Result{" +
                    "tcpport=" + tcpPort +
                    ", nonce=" + nonce +
                    ", useragent='" + userAgent + '\'' +
                    ", rpc=" + rpc +
                    ", protocol=" + protocol +
                    '}';
        }

        public static class Rpc {

            @JsonProperty("maxiteratorresultitems")
            private Integer maxIteratorResultItems;

            @JsonProperty("sessionenabled")
            private Boolean sessionEnabled;

            public Rpc() {
            }

            public Integer getMaxIteratorResultItems() {
                return maxIteratorResultItems;
            }

            public Boolean sessionEnabled() {
                return sessionEnabled;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) {
                    return true;
                }
                if (!(o instanceof Rpc)) {
                    return false;
                }
                Rpc that = (Rpc) o;
                return Objects.equals(getMaxIteratorResultItems(), that.getMaxIteratorResultItems()) &&
                        Objects.equals(sessionEnabled(), that.sessionEnabled());
            }

            @Override
            public int hashCode() {
                return Objects.hash(getMaxIteratorResultItems(), sessionEnabled());
            }

            @Override
            public String toString() {
                return "Rpc{" +
                        "maxIteratorResultItems=" + maxIteratorResultItems +
                        ", sessionEnabled=" + sessionEnabled +
                        '}';
            }
        }

        public static class Protocol {

            @JsonProperty("addressversion")
            private Integer addressVersion;

            @JsonProperty("network")
            private Long network;

            @JsonProperty("validatorscount")
            private Integer validatorsCount;

            @JsonProperty("msperblock")
            private Long msPerBlock;

            @JsonProperty("maxtraceableblocks")
            private Long maxTraceableBlocks;

            @JsonProperty("maxvaliduntilblockincrement")
            private Long maxValidUntilBlockIncrement;

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

            @JsonProperty("standbycommittee")
            @JsonSetter(nulls = Nulls.AS_EMPTY)
            private List<ECPublicKey> standbyCommittee;

            @JsonProperty("seedlist")
            @JsonSetter(nulls = Nulls.AS_EMPTY)
            private List<String> seedList;

            public Protocol() {
            }

            public Integer getAddressVersion() {
                return addressVersion;
            }

            public Long getNetwork() {
                return network;
            }

            public void setNetwork(Long network) {
                this.network = network;
            }

            public Integer getValidatorsCount() {
                return validatorsCount;
            }

            public Long getMilliSecondsPerBlock() {
                return msPerBlock;
            }

            public void setMilliSecondsPerBlock(Long msPerBlock) {
                this.msPerBlock = msPerBlock;
            }

            public Long getMaxTraceableBlocks() {
                return maxTraceableBlocks;
            }

            public Long getMaxValidUntilBlockIncrement() {
                return maxValidUntilBlockIncrement;
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

            public List<ECPublicKey> getStandbyCommittee() {
                return standbyCommittee;
            }

            public List<String> getSeedList() {
                return seedList;
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
                return Objects.equals(getAddressVersion(), that.getAddressVersion()) &&
                        Objects.equals(getNetwork(), that.getNetwork()) &&
                        Objects.equals(getValidatorsCount(), that.getValidatorsCount()) &&
                        Objects.equals(getMilliSecondsPerBlock(), that.getMilliSecondsPerBlock()) &&
                        Objects.equals(getMaxTraceableBlocks(), that.getMaxTraceableBlocks()) &&
                        Objects.equals(getMaxValidUntilBlockIncrement(), that.getMaxValidUntilBlockIncrement()) &&
                        Objects.equals(getMaxTransactionsPerBlock(), that.getMaxTransactionsPerBlock()) &&
                        Objects.equals(getMemoryPoolMaxTransactions(), that.getMemoryPoolMaxTransactions()) &&
                        Objects.equals(getInitialGasDistribution(), that.getInitialGasDistribution()) &&
                        Objects.equals(getHardforks(), that.getHardforks()) &&
                        Objects.equals(getStandbyCommittee(), that.getStandbyCommittee()) &&
                        Objects.equals(getSeedList(), that.getSeedList());
            }

            @Override
            public int hashCode() {
                return Objects.hash(getAddressVersion(), getNetwork(), getValidatorsCount(), getMilliSecondsPerBlock(),
                        getMaxTraceableBlocks(), getMaxValidUntilBlockIncrement(), getMaxTransactionsPerBlock(),
                        getMemoryPoolMaxTransactions(), getInitialGasDistribution(), getHardforks(),
                        getStandbyCommittee(), getSeedList());
            }

            @Override
            public String toString() {
                return "Protocol{" +
                        "addressVersion=" + addressVersion +
                        ", network=" + network +
                        ", validatorsCount=" + validatorsCount +
                        ", milliSecondsPerBlock=" + msPerBlock +
                        ", maxTraceableBlocks=" + maxTraceableBlocks +
                        ", maxValidUntilBlockIncrement=" + maxValidUntilBlockIncrement +
                        ", maxTransactionsPerBlock=" + maxTransactionsPerBlock +
                        ", memoryPoolMaxTransactions=" + memoryPoolMaxTransactions +
                        ", initialGasDistribution=" + initialGasDistribution +
                        ", hardforks=" + hardforks +
                        ", standbyCommittee=" + standbyCommittee +
                        ", seedList=" + seedList +
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
