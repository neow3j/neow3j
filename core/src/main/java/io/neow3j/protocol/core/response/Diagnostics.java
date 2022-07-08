package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.types.Hash160;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Diagnostics {

    /**
     * The contracts that have been invoked in this invocation. The first hash is the script hash of the invocation
     * script that is executed.
     */
    @JsonProperty("invokedcontracts")
    private InvokedContract invokedContracts;

    /**
     * The resulting storage changes of the invocation.
     */
    @JsonProperty("storagechanges")
    private List<StorageChange> storageChanges;

    public Diagnostics() {
    }

    public Diagnostics(InvokedContract invokedContracts, List<StorageChange> storageChange) {
        this.invokedContracts = invokedContracts;
        this.storageChanges = storageChange;
    }

    public InvokedContract getInvokedContracts() {
        return invokedContracts;
    }

    public List<StorageChange> getStorageChanges() {
        return storageChanges;
    }

    public static class InvokedContract {

        /**
         * The script hash of the invoked contract.
         */
        @JsonProperty("hash")
        private Hash160 hash;

        /**
         * The contracts that were invoked by this contract.
         */
        @JsonProperty("call")
        private List<InvokedContract> invokedContracts = new ArrayList<>();

        public InvokedContract() {
        }

        public InvokedContract(Hash160 hash) {
            this(hash, new ArrayList<>());
        }

        public InvokedContract(Hash160 hash, List<InvokedContract> invokedContracts) {
            this.hash = hash;
            this.invokedContracts = invokedContracts;
        }

        public Hash160 getHash() {
            return hash;
        }

        public List<InvokedContract> getInvokedContracts() {
            return invokedContracts;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof InvokedContract)) {
                return false;
            }
            InvokedContract that = (InvokedContract) o;
            return Objects.equals(getHash(), that.getHash()) &&
                    Objects.equals(getInvokedContracts(), that.getInvokedContracts());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getHash(), getInvokedContracts());
        }

        @Override
        public String toString() {
            return "InvokedContract{" +
                    "hash=" + hash +
                    ", invokedContracts=" + invokedContracts +
                    '}';
        }

    }

    public static class StorageChange {

        /**
         * The type of change in the storage, e.g., "Added" or "Deleted".
         */
        @JsonProperty("state")
        private String state;

        /**
         * The returned key is based on the contract's id and the storage key that was used in that contract.
         */
        @JsonProperty("key")
        private String key;

        @JsonProperty("value")
        private String value;

        public StorageChange() {
        }

        public StorageChange(String state, String key, String value) {
            this.state = state;
            this.key = key;
            this.value = value;
        }

        public String getState() {
            return state;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof StorageChange)) {
                return false;
            }
            StorageChange that = (StorageChange) o;
            return Objects.equals(getState(), that.getState()) &&
                    Objects.equals(getKey(), that.getKey()) &&
                    Objects.equals(getValue(), that.getValue());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getState(), getKey(), getValue());
        }

        @Override
        public String toString() {
            return "StorageChanges{" +
                    "state=" + state +
                    ", key=" + key +
                    ", value=" + value +
                    '}';
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Diagnostics)) {
            return false;
        }
        Diagnostics that = (Diagnostics) o;
        return Objects.equals(getInvokedContracts(), that.getInvokedContracts()) &&
                Objects.equals(getStorageChanges(), that.getStorageChanges());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getInvokedContracts(), getStorageChanges());
    }

    @Override
    public String toString() {
        return "Diagnostics{" +
                "invokedContracts=" + invokedContracts +
                ", storageChanges=" + storageChanges +
                '}';
    }

}
