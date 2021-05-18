package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.types.Hash160;
import io.neow3j.protocol.core.Response;

import java.util.List;
import java.util.Objects;

public class NeoGetContractState extends Response<NeoGetContractState.ContractState> {

    public ContractState getContractState() {
        return getResult();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContractState {

        @JsonProperty("id")
        private int id;

        @JsonProperty("updatecounter")
        private Integer updateCounter;

        @JsonProperty("hash")
        private Hash160 hash;

        @JsonProperty("nef")
        private ContractNef nef;

        @JsonProperty("manifest")
        private ContractManifest manifest;

        @JsonProperty("updatehistory")
        private List<Integer> updateHistory;

        public ContractState() {
        }

        public ContractState(int id, int updateCounter, Hash160 hash, ContractNef nef,
                ContractManifest manifest, List<Integer> updateHistory) {
            this.id = id;
            this.updateCounter = updateCounter;
            this.hash = hash;
            this.nef = nef;
            this.manifest = manifest;
            this.updateHistory = updateHistory;
        }

        public int getId() {
            return id;
        }

        public Integer getUpdateCounter() {
            return updateCounter;
        }

        public Hash160 getHash() {
            return hash;
        }

        public ContractNef getNef() {
            return nef;
        }

        public ContractManifest getManifest() {
            return manifest;
        }

        public List<Integer> getUpdateHistory() {
            return updateHistory;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ContractState)) {
                return false;
            }
            ContractState that = (ContractState) o;
            return getId() == that.getId() &&
                    Objects.equals(getUpdateCounter(), that.getUpdateCounter()) &&
                    Objects.equals(getHash(), that.getHash()) &&
                    Objects.equals(getNef(), that.getNef()) &&
                    Objects.equals(getManifest(), that.getManifest()) &&
                    Objects.equals(getUpdateHistory(), that.getUpdateHistory());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getId(), getUpdateCounter(), getHash(), getNef(), getManifest(),
                    getUpdateCounter());
        }

        @Override
        public String toString() {
            return "ContractState{" +
                    "id=" + id +
                    ", updateCounter=" + updateCounter +
                    ", hash='" + hash + '\'' +
                    ", nef=" + nef +
                    ", manifest=" + manifest +
                    ", updateHistory=" + updateHistory +
                    '}';
        }
    }

}
