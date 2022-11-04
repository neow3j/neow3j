package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.protocol.core.Response;
import io.neow3j.protocol.exceptions.RpcResponseErrorException;

import java.util.Objects;

public class NeoGetStateHeight extends Response<NeoGetStateHeight.StateHeight> {

    /**
     * @return the result.
     * @throws RpcResponseErrorException if the Neo node returned an error.
     */
    public StateHeight getStateHeight() {
        return getResult();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StateHeight {

        @JsonProperty("localrootindex")
        private long localRootIndex;

        @JsonProperty("validatedrootindex")
        private long validatedRootIndex;

        public StateHeight() {
        }

        public StateHeight(long localRootIndex, long validatedRootIndex) {
            this.localRootIndex = localRootIndex;
            this.validatedRootIndex = validatedRootIndex;
        }

        public long getLocalRootIndex() {
            return localRootIndex;
        }

        public long getValidatedRootIndex() {
            return validatedRootIndex;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof StateHeight)) {
                return false;
            }

            StateHeight that = (StateHeight) o;
            return Objects.equals(getLocalRootIndex(), that.getLocalRootIndex()) &&
                    Objects.equals(getValidatedRootIndex(), that.getValidatedRootIndex());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getLocalRootIndex(), getValidatedRootIndex());
        }

        @Override
        public String toString() {
            return "StateHeight{" +
                    "localRootIndex=" + localRootIndex +
                    ", validatedRootIndex=" + validatedRootIndex +
                    '}';
        }

    }

}
