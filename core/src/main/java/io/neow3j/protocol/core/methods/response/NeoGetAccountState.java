package io.neow3j.protocol.core.methods.response;

import io.neow3j.protocol.core.Response;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

public class NeoGetAccountState extends Response<NeoGetAccountState.State> {

    public State getAccountState() {
        return getResult();
    }

    public static class State {

        @JsonProperty("version")
        private int version;

        @JsonProperty("script_hash")
        private String scriptHash;

        @JsonProperty("frozen")
        private Boolean frozen;

        @JsonProperty("votes")
        private List<String> votes;

        @JsonProperty("balances")
        private List<Balance> balances;

        public State() {
        }

        public State(int version, String scriptHash, Boolean frozen, List<String> votes, List<Balance> balances) {
            this.version = version;
            this.scriptHash = scriptHash;
            this.frozen = frozen;
            this.votes = votes;
            this.balances = balances;
        }

        public int getVersion() {
            return version;
        }

        public String getScriptHash() {
            return scriptHash;
        }

        public Boolean getFrozen() {
            return frozen;
        }

        public List<String> getVotes() {
            return votes;
        }

        public List<Balance> getBalances() {
            return balances;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof State)) return false;
            State state = (State) o;
            return getVersion() == state.getVersion() &&
                    Objects.equals(getScriptHash(), state.getScriptHash()) &&
                    Objects.equals(getFrozen(), state.getFrozen()) &&
                    Objects.equals(getVotes(), state.getVotes()) &&
                    Objects.equals(getBalances(), state.getBalances());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getVersion(), getScriptHash(), getFrozen(), getVotes(), getBalances());
        }

        @Override
        public String toString() {
            return "State{" +
                    "version=" + version +
                    ", scriptHash='" + scriptHash + '\'' +
                    ", frozen=" + frozen +
                    ", votes=" + votes +
                    ", balances=" + balances +
                    '}';
        }
    }

    public static class Balance {

        @JsonProperty("asset")
        private String assetAddress;

        @JsonProperty("value")
        private String value;

        public Balance() {
        }

        public Balance(String assetAddress, String value) {
            this.assetAddress = assetAddress;
            this.value = value;
        }

        public String getAssetAddress() {
            return assetAddress;
        }

        public String getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Balance)) return false;
            Balance balance = (Balance) o;
            return Objects.equals(getAssetAddress(), balance.getAssetAddress()) &&
                    Objects.equals(getValue(), balance.getValue());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getAssetAddress(), getValue());
        }

        @Override
        public String toString() {
            return "Balance{" +
                    "assetAddress='" + assetAddress + '\'' +
                    ", value='" + value + '\'' +
                    '}';
        }
    }

}
