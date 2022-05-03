package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import io.neow3j.protocol.core.Response;
import io.neow3j.types.Hash160;
import io.neow3j.types.Hash256;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

public abstract class NeoGetTokenTransfers<T extends NeoGetTokenTransfers.TokenTransfers> extends Response<T> {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TokenTransfers<K extends TokenTransfer> {

        @JsonProperty("sent")
        @JsonSetter(nulls = Nulls.AS_EMPTY)
        private List<K> sent;

        @JsonProperty("received")
        @JsonSetter(nulls = Nulls.AS_EMPTY)
        private List<K> received;

        @JsonProperty("address")
        private String transferAddress;

        public TokenTransfers() {
        }

        public TokenTransfers(List<K> sent, List<K> received,
                String transferAddress) {
            this.sent = sent;
            this.received = received;
            this.transferAddress = transferAddress;
        }

        public List<K> getSent() {
            return sent;
        }

        public List<K> getReceived() {
            return received;
        }

        public String getTransferAddress() {
            return transferAddress;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof TokenTransfers)) {
                return false;
            }
            TokenTransfers<?> that = (TokenTransfers<?>) o;
            return Objects.equals(sent, that.sent) &&
                    Objects.equals(received, that.received) &&
                    Objects.equals(transferAddress, that.transferAddress);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sent, received, transferAddress);
        }

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TokenTransfer {

        @JsonProperty("timestamp")
        private long timestamp;

        @JsonProperty("assethash")
        private Hash160 assetHash;

        @JsonProperty("transferaddress")
        private String transferAddress;

        @JsonProperty("amount")
        private BigInteger amount;

        @JsonProperty("blockindex")
        private long blockIndex;

        @JsonProperty("transfernotifyindex")
        private long transferNotifyIndex;

        @JsonProperty("txhash")
        private Hash256 txHash;

        public TokenTransfer() {
        }

        public TokenTransfer(long timestamp, Hash160 assetHash, String transferAddress, BigInteger amount,
                long blockIndex, long transferNotifyIndex, Hash256 txHash) {
            this.timestamp = timestamp;
            this.assetHash = assetHash;
            this.transferAddress = transferAddress;
            this.amount = amount;
            this.blockIndex = blockIndex;
            this.transferNotifyIndex = transferNotifyIndex;
            this.txHash = txHash;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public Hash160 getAssetHash() {
            return assetHash;
        }

        public String getTransferAddress() {
            return transferAddress;
        }

        public BigInteger getAmount() {
            return amount;
        }

        public long getBlockIndex() {
            return blockIndex;
        }

        public long getTransferNotifyIndex() {
            return transferNotifyIndex;
        }

        public Hash256 getTxHash() {
            return txHash;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof TokenTransfer)) {
                return false;
            }
            TokenTransfer that = (TokenTransfer) o;
            return timestamp == that.timestamp &&
                    blockIndex == that.blockIndex &&
                    transferNotifyIndex == that.transferNotifyIndex &&
                    Objects.equals(assetHash, that.assetHash) &&
                    Objects.equals(transferAddress, that.transferAddress) &&
                    Objects.equals(amount, that.amount) &&
                    Objects.equals(txHash, that.txHash);
        }

        @Override
        public int hashCode() {
            return Objects.hash(timestamp, assetHash, transferAddress, amount, blockIndex, transferNotifyIndex, txHash);
        }

    }

}
