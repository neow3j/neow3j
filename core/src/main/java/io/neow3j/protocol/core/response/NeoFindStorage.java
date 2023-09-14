package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.protocol.core.Response;
import io.neow3j.protocol.exceptions.RpcResponseErrorException;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

public class NeoFindStorage extends Response<NeoFindStorage.FoundStorage> {

    /**
     * @return the result.
     * @throws RpcResponseErrorException if the Neo node returned an error.
     */
    public FoundStorage getFoundStorage() {
        return getResult();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FoundStorage {

        @JsonProperty("truncated")
        private Boolean truncated;

        @JsonProperty("next")
        private BigInteger next;

        @JsonProperty("results")
        private List<ContractStorageEntry> storageEntries;

        public FoundStorage() {
        }

        /**
         * @return true, if the list of storageEntries is truncated. False, otherwise.
         */
        public Boolean isTruncated() {
            return truncated;
        }

        /**
         * @return the next index to start from to find more storage entries.
         */
        public BigInteger getNext() {
            return next;
        }

        /**
         * @return the found storage entries.
         */
        public List<ContractStorageEntry> getStorageEntries() {
            return storageEntries;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof FoundStorage)) return false;

            FoundStorage that = (FoundStorage) o;
            return Objects.equals(isTruncated(), that.isTruncated()) &&
                    Objects.equals(getNext(), that.getNext()) &&
                    Objects.equals(getStorageEntries(), that.getStorageEntries());
        }

        @Override
        public int hashCode() {
            return Objects.hash(isTruncated(), getNext(), getStorageEntries());
        }

        @Override
        public String toString() {
            return "FoundStorage{" +
                    "truncated=" + isTruncated() +
                    ", next=" + getNext() +
                    ", results=" + getStorageEntries() +
                    '}';
        }

    }

}
