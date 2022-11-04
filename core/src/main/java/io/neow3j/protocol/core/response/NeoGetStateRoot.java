package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.protocol.exceptions.RpcResponseErrorException;
import io.neow3j.types.Hash256;
import io.neow3j.protocol.core.Response;

import java.util.List;
import java.util.Objects;

public class NeoGetStateRoot extends Response<NeoGetStateRoot.StateRoot> {

    /**
     * @return the result.
     * @throws RpcResponseErrorException if the Neo node returned an error.
     */
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

        @JsonProperty("witnesses")
        private List<NeoWitness> witness;

        public StateRoot() {
        }

        public StateRoot(int version, long index, Hash256 rootHash, List<NeoWitness> witness) {
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

        public List<NeoWitness> getWitnesses() {
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
                    Objects.equals(getWitnesses(), that.getWitnesses());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getVersion(), getIndex(), getRootHash(), getWitnesses());
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
