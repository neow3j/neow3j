package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.contract.Hash256;
import io.neow3j.protocol.core.Response;

import java.util.Objects;

public class NeoGetStateRoot extends Response<NeoGetStateRoot.StateRoot> {

    public StateRoot getStateRoot() {
        return getResult();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StateRoot {

        @JsonProperty("version")
        private int version;

        @JsonProperty("index")
        private long index;

        @JsonProperty("roothash")
        private Hash256 rootHash;

        @JsonProperty("witness")
        private NeoWitness witness;

        public StateRoot() {
        }

        public StateRoot(int version, long index, Hash256 rootHash, NeoWitness witness) {
            this.version = version;
            this.index = index;
            this.rootHash = rootHash;
            this.witness = witness;
        }

        public int getVersion() {
            return version;
        }

        public long getIndex() {
            return index;
        }

        public Hash256 getRootHash() {
            return rootHash;
        }

        public NeoWitness getWitness() {
            return witness;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof StateRoot)) {
                return false;
            }
            StateRoot that = (StateRoot) o;
            return getVersion() == that.getVersion() &&
                    getIndex() == that.getIndex() &&
                    Objects.equals(getRootHash(), that.getRootHash()) &&
                    Objects.equals(getWitness(), that.getWitness());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getVersion(), getIndex(), getRootHash(), getWitness());
        }

        @Override
        public String toString() {
            return "StateRoot{" +
                    "version=" + version +
                    ", index=" + index +
                    ", rootHash='" + rootHash + '\'' +
                    ", witness=" + witness +
                    '}';
        }

    }

}
