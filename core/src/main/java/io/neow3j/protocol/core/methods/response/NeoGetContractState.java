package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.protocol.core.Response;
import java.util.Objects;

public class NeoGetContractState extends Response<NeoGetContractState.ContractState> {

    public ContractState getContractState() {
        return getResult();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContractState {

        @JsonProperty("id")
        private int id;

        @JsonProperty("hash")
        private String hash;

        @JsonProperty("script")
        private String script;

        @JsonProperty("manifest")
        private ContractManifest manifest;

        public ContractState() {
        }

        public ContractState(int id, String hash, String script, ContractManifest manifest) {
            this.id = id;
            this.hash = hash;
            this.script = script;
            this.manifest = manifest;
        }

        public int getId() {
            return id;
        }

        public String getHash() {
            return hash;
        }

        public String getScript() {
            return script;
        }

        public ContractManifest getManifest() {
            return manifest;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ContractState)) return false;
            ContractState that = (ContractState) o;
            return Objects.equals(getId(), that.getId()) &&
                    Objects.equals(getHash(), that.getHash()) &&
                    Objects.equals(getScript(), that.getScript()) &&
                    Objects.equals(getManifest(), that.getManifest());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getId(), getHash(), getScript(), getManifest());
        }

        @Override
        public String toString() {
            return "ContractState{" +
                    "id=" + id +
                    ", hash=" + hash +
                    ", script=" + script +
                    ", manifest=" + manifest +
                    '}';
        }
    }
}
