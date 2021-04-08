package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import io.neow3j.contract.Hash160;
import io.neow3j.contract.Hash256;
import io.neow3j.protocol.core.Response;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

public class NeoGetNep17Transfers extends Response<NeoGetNep17Transfers.Nep17TransferWrapper> {

    public Nep17TransferWrapper getNep17Transfer() {
        return getResult();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Nep17TransferWrapper {

        @JsonProperty("sent")
        @JsonSetter(nulls = Nulls.AS_EMPTY)
        private List<Nep17Transfer> sent;

        @JsonProperty("received")
        @JsonSetter(nulls = Nulls.AS_EMPTY)
        private List<Nep17Transfer> received;

        @JsonProperty("address")
        private String transferAddress;

        public Nep17TransferWrapper() {
        }

        public Nep17TransferWrapper(List<Nep17Transfer> sent, List<Nep17Transfer> received,
                String transferAddress) {
            this.sent = sent;
            this.received = received;
            this.transferAddress = transferAddress;
        }

        public List<Nep17Transfer> getSent() {
            return sent;
        }

        public List<Nep17Transfer> getReceived() {
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
            if (!(o instanceof Nep17TransferWrapper)) {
                return false;
            }
            Nep17TransferWrapper that = (Nep17TransferWrapper) o;
            return Objects.equals(getSent(), that.getSent()) &&
                    Objects.equals(getReceived(), that.getReceived()) && Objects
                    .equals(getTransferAddress(), that.getTransferAddress());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getSent(), getReceived(), getTransferAddress());
        }

        @Override
        public String toString() {
            return "Nep17TransferWrapper{" +
                    "sent=" + sent +
                    ", received=" + received +
                    ", transferAddress='" + transferAddress + '\'' + '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Nep17Transfer {

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

        public Nep17Transfer() {
        }

        public Nep17Transfer(long timestamp, Hash160 assetHash, String transferAddress,
                BigInteger amount, long blockIndex, long transferNotifyIndex, Hash256 txHash) {
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
            if (!(o instanceof Nep17Transfer)) {
                return false;
            }
            Nep17Transfer that = (Nep17Transfer) o;
            return getTimestamp() == that.getTimestamp() &&
                    getBlockIndex() == that.getBlockIndex() &&
                    getTransferNotifyIndex() == that.getTransferNotifyIndex() &&
                    Objects.equals(getAssetHash(), that.getAssetHash()) &&
                    Objects.equals(getTransferAddress(), that.getTransferAddress()) &&
                    Objects.equals(getAmount(), that.getAmount()) &&
                    Objects.equals(getTxHash(), that.getTxHash());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getTimestamp(), getAssetHash(), getTransferAddress(), getAmount(),
                    getBlockIndex(), getTransferNotifyIndex(), getTxHash());
        }

        @Override
        public String toString() {
            return "Nep17Transfer{" +
                    "timestamp=" + timestamp +
                    ", assetHash='" + assetHash + '\'' +
                    ", transferAddress='" + transferAddress + '\'' +
                    ", amount='" + amount + '\'' +
                    ", blockIndex=" + blockIndex +
                    ", transferNotifyIndex=" + transferNotifyIndex +
                    ", txHash='" + txHash + '\'' +
                    '}';
        }
    }

}
