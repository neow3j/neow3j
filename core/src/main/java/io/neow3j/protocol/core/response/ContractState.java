package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.types.Hash160;
import io.neow3j.types.StackItemType;
import io.neow3j.utils.BigIntegers;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

import static io.neow3j.utils.ArrayUtils.reverseArray;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ContractState extends CoreContractState {

    @JsonProperty("updatecounter")
    private Integer updateCounter;

    public ContractState() {
        super();
    }

    public ContractState(int id, int updateCounter, Hash160 hash, ContractNef nef, ContractManifest manifest) {
        super(id, hash, nef, manifest);
        this.updateCounter = updateCounter;
    }

    public Integer getUpdateCounter() {
        return updateCounter;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getHash(), getNef(), getManifest(), getUpdateCounter());
    }

    @Override
    public String toString() {
        return "ContractState{" +
                "id=" + getId() +
                ", updateCounter=" + updateCounter +
                ", hash='" + getHash() + '\'' +
                ", nef=" + getNef() +
                ", manifest=" + getManifest() +
                '}';
    }

    /**
     * Represents the identifiers of a contract, i.e., its hash and its id stored in the native ContractManagement
     * contract.
     */
    public static class ContractIdentifiers {

        private final BigInteger id;
        private final Hash160 hash;

        public ContractIdentifiers(BigInteger id, Hash160 hash) {
            this.id = id;
            this.hash = hash;
        }

        public static ContractIdentifiers fromStackItem(StackItem stackItem) {
            if (!stackItem.getType().equals(StackItemType.STRUCT)) {
                throw new IllegalArgumentException("Could not deserialise ContractIdentifiers from the stack item.");
            }
            List<StackItem> struct = stackItem.getList();
            BigInteger id = BigIntegers.fromBigEndianHexString(struct.get(0).getHexString());
            Hash160 hash = new Hash160(reverseArray(struct.get(1).getByteArray()));
            return new ContractIdentifiers(id, hash);
        }

        public BigInteger getId() {
            return id;
        }

        public Hash160 getHash() {
            return hash;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ContractIdentifiers)) {
                return false;
            }
            ContractIdentifiers that = (ContractIdentifiers) o;
            return Objects.equals(getId(), that.getId()) &&
                    Objects.equals(getHash(), that.getHash());
        }

        @Override
        public String toString() {
            return "ContractIdentifiers{" +
                    "id=" + id +
                    ", hash=" + hash +
                    '}';
        }
    }

}
