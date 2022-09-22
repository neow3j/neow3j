package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.protocol.core.Response;
import io.neow3j.protocol.exceptions.RpcResponseErrorException;

import java.util.List;
import java.util.Objects;

public class NeoFindStates extends Response<NeoFindStates.States> {

    /**
     * @return the result.
     * @throws RpcResponseErrorException if the Neo node returned an error.
     */
    public States getStates() {
        return getResult();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class States {

        @JsonProperty("firstProof")
        private String firstProof;

        @JsonProperty("lastProof")
        private String lastProof;

        @JsonProperty("truncated")
        private boolean truncated;

        @JsonProperty("results")
        private List<Result> results;

        public States() {
        }

        public States(String firstProof, String lastProof, Boolean truncated,
                List<Result> results) {
            this.firstProof = firstProof;
            this.lastProof = lastProof;
            this.truncated = truncated;
            this.results = results;
        }

        public String getFirstProof() {
            return firstProof;
        }

        public String getLastProof() {
            return lastProof;
        }

        public boolean isTruncated() {
            return truncated;
        }

        public List<Result> getResults() {
            return results;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof States)) return false;
            States that = (States) o;
            return Objects.equals(getFirstProof(), that.getFirstProof()) &&
                    Objects.equals(getLastProof(), that.getLastProof()) &&
                    isTruncated() == that.isTruncated() &&
                    Objects.equals(getResults(), that.getResults());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getFirstProof(), getLastProof(), isTruncated(), getResults());
        }

        @Override
        public String toString() {
            return "States{" +
                    "firstProof='" + firstProof + "'" +
                    ", lastProof='" + lastProof + "'" +
                    ", isTruncated=" + truncated +
                    ", results=" + results +
                    '}';
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Result {

            @JsonProperty("key")
            private String key;

            @JsonProperty("value")
            private String value;

            public Result() {
            }

            public Result(String key, String value) {
                this.key = key;
                this.value = value;
            }

            public String getKey() {
                return key;
            }

            public String getValue() {
                return value;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof Result)) return false;

                Result that = (Result) o;
                return Objects.equals(getKey(), that.getKey()) &&
                        Objects.equals(getValue(), that.getValue());
            }

            @Override
            public int hashCode() {
                return Objects.hash(getKey(), getValue());
            }

            @Override
            public String toString() {
                return "Result{" +
                        "key=" + key +
                        ", value=" + value +
                        '}';
            }
        }

    }

}
