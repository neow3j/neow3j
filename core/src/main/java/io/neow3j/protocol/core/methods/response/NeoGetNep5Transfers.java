package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import io.neow3j.protocol.core.Response;

import java.util.List;
import java.util.Objects;

public class NeoGetNep5Transfers extends Response<NeoGetNep5Transfers.Nep5TransferWrapper> {

    public NeoGetNep5Transfers.Nep5TransferWrapper getNep5Transfer() {
        return getResult();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Nep5TransferWrapper {

        @JsonProperty("sent")
        @JsonSetter(nulls = Nulls.AS_EMPTY)
        private List<Nep5Transfer> sent;

        @JsonProperty("received")
        @JsonSetter(nulls = Nulls.AS_EMPTY)
        private List<Nep5Transfer> received;

        @JsonProperty("address")
        private String transferAddress;

        public Nep5TransferWrapper() {
        }

        public Nep5TransferWrapper(List<Nep5Transfer> sent, List<Nep5Transfer> received, String transferAddress) {
            this.sent = sent;
            this.received = received;
            this.transferAddress = transferAddress;
        }

        public List<Nep5Transfer> getSent() {
            return sent;
        }

        public List<Nep5Transfer> getReceived() {
            return received;
        }

        public String getTransferAddress() {
            return transferAddress;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Nep5TransferWrapper)) return false;
            Nep5TransferWrapper that = (Nep5TransferWrapper) o;
            return Objects.equals(getSent(), that.getSent()) && Objects.equals(getReceived(), that.getReceived()) && Objects
                    .equals(getTransferAddress(), that.getTransferAddress());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getSent(), getReceived(), getTransferAddress());
        }

        @Override
        public String toString() {
            return "Nep5TransferWrapper{" + "sent=" + sent + ", received=" + received + ", transferAddress='" + transferAddress + '\'' + '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Nep5Transfer {

        @JsonProperty("timestamp")
        private long timestamp;

        @JsonProperty("assethash")
        private String assetHash;

        @JsonProperty("transferaddress")
        private String transferAddress;

        @JsonProperty("amount")
        private String amount;

        @JsonProperty("blockindex")
        private long blockIndex;

        @JsonProperty("transfernotifyindex")
        private long transferNotifyIndex;

        @JsonProperty("txhash")
        private String txHash;

        public Nep5Transfer() {
        }

        public Nep5Transfer(long timestamp, String assetHash, String transferAddress, String amount, long blockIndex, long transferNotifyIndex, String txHash) {
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

        public String getAssetHash() {
            return assetHash;
        }

        public String getTransferAddress() {
            return transferAddress;
        }

        public String getAmount() {
            return amount;
        }

        public long getBlockIndex() {
            return blockIndex;
        }

        public long getTransferNotifyIndex() {
            return transferNotifyIndex;
        }

        public String getTxHash() {
            return txHash;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Nep5Transfer)) return false;
            Nep5Transfer that = (Nep5Transfer) o;
            return getTimestamp() == that.getTimestamp() && getBlockIndex() == that.getBlockIndex() && getTransferNotifyIndex() == that
                    .getTransferNotifyIndex() && Objects.equals(getAssetHash(), that.getAssetHash()) && Objects.equals(getTransferAddress(), that
                    .getTransferAddress()) && Objects.equals(getAmount(), that.getAmount()) && Objects.equals(getTxHash(), that
                    .getTxHash());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getTimestamp(), getAssetHash(), getTransferAddress(), getAmount(), getBlockIndex(), getTransferNotifyIndex(), getTxHash());
        }

        @Override
        public String toString() {
            return "Nep5Transfer{" + "timestamp=" + timestamp + ", assetHash='" + assetHash + '\'' + ", transferAddress='" + transferAddress + '\'' + ", amount='" + amount + '\'' + ", blockIndex=" + blockIndex + ", transferNotifyIndex=" + transferNotifyIndex + ", txHash='" + txHash + '\'' + '}';
        }
    }

}