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

        @JsonProperty("updatecounter")
        private Integer updateCounter;

        @JsonProperty("hash")
        private String hash;

        @JsonProperty("nef")
        private ContractNef nef;

        @JsonProperty("manifest")
        private ContractManifest manifest;

        @JsonProperty("activeblockindex")
        private Integer activeBlockIndex;

        public ContractState() {
        }

        public ContractState(int id, int updateCounter, String hash, ContractNef nef,
                             ContractManifest manifest, Integer activeBlockIndex) {
            this.id = id;
            this.updateCounter = updateCounter;
            this.hash = hash;
            this.nef = nef;
            this.manifest = manifest;
            this.activeBlockIndex = activeBlockIndex;
        }

        public int getId() {
            return id;
        }

        public Integer getUpdateCounter() {
            return updateCounter;
        }

        public String getHash() {
            return hash;
        }

        public ContractNef getNef() {
            return nef;
        }

        public ContractManifest getManifest() {
            return manifest;
        }

        public Integer getActiveBlockIndex() {
            return activeBlockIndex;
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
                    Objects.equals(getUpdateCounter(),that.getUpdateCounter()) &&
                    Objects.equals(getHash(), that.getHash()) &&
                    Objects.equals(getNef(), that.getNef()) &&
                    Objects.equals(getManifest(), that.getManifest()) &&
                    Objects.equals(getActiveBlockIndex(), that.getActiveBlockIndex());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getId(), getUpdateCounter(), getHash(), getNef(), getManifest(),
                    getActiveBlockIndex());
        }

        @Override
        public String toString() {
            return "ContractState{" +
                    "id=" + id +
                    ", updateCounter=" + updateCounter +
                    ", hash='" + hash + '\'' +
                    ", nef=" + nef +
                    ", manifest=" + manifest +
                    ", activeBlockIndex=" + activeBlockIndex +
                    '}';
        }
    }
}
