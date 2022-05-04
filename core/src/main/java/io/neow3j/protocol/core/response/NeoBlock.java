package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import io.neow3j.types.Hash256;

import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NeoBlock {

    @JsonProperty("hash")
    private Hash256 hash;

    @JsonProperty("size")
    private long size;

    @JsonProperty("version")
    private int version;

    @JsonProperty("previousblockhash")
    private Hash256 prevBlockHash;

    @JsonProperty("merkleroot")
    private Hash256 merkleRootHash;

    @JsonProperty("time")
    private long time;

    @JsonProperty("index")
    private long index;

    @JsonProperty("primary")
    private int primary;

    @JsonProperty("nextconsensus")
    private String nextConsensus;

    @JsonProperty("witnesses")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<NeoWitness> witnesses;

    @JsonProperty("tx")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Transaction> transactions;

    @JsonProperty("confirmations")
    private int confirmations;

    @JsonProperty("nextblockhash")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Hash256 nextBlockHash;

    public NeoBlock() {
    }

    public NeoBlock(Hash256 hash, long size, int version, Hash256 prevBlockHash, Hash256 merkleRootHash, long time,
            long index, int primary, String nextConsensus, List<NeoWitness> witnesses, List<Transaction> transactions,
            int confirmations, Hash256 nextBlockHash) {
        this.hash = hash;
        this.size = size;
        this.version = version;
        this.prevBlockHash = prevBlockHash;
        this.merkleRootHash = merkleRootHash;
        this.time = time;
        this.index = index;
        this.primary = primary;
        this.nextConsensus = nextConsensus;
        this.witnesses = witnesses;
        this.transactions = transactions;
        this.confirmations = confirmations;
        this.nextBlockHash = nextBlockHash;
    }

    public Hash256 getHash() {
        return hash;
    }

    public long getSize() {
        return size;
    }

    public int getVersion() {
        return version;
    }

    public Hash256 getPrevBlockHash() {
        return prevBlockHash;
    }

    public Hash256 getMerkleRootHash() {
        return merkleRootHash;
    }

    public long getTime() {
        return time;
    }

    public long getIndex() {
        return index;
    }

    public int getPrimary() {
        return primary;
    }

    public String getNextConsensus() {
        return nextConsensus;
    }

    public List<NeoWitness> getWitnesses() {
        return witnesses;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public int getConfirmations() {
        return confirmations;
    }

    public Hash256 getNextBlockHash() {
        return nextBlockHash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NeoBlock)) {
            return false;
        }
        NeoBlock neoBlock = (NeoBlock) o;
        return getSize() == neoBlock.getSize() &&
                getVersion() == neoBlock.getVersion() &&
                getTime() == neoBlock.getTime() &&
                getIndex() == neoBlock.getIndex() &&
                getConfirmations() == neoBlock.getConfirmations() &&
                getPrimary() == neoBlock.getPrimary() &&
                Objects.equals(getHash(), neoBlock.getHash()) &&
                Objects.equals(getPrevBlockHash(), neoBlock.getPrevBlockHash()) &&
                Objects.equals(getMerkleRootHash(), neoBlock.getMerkleRootHash()) &&
                Objects.equals(getNextConsensus(), neoBlock.getNextConsensus()) &&
                Objects.equals(getWitnesses(), neoBlock.getWitnesses()) &&
                Objects.equals(getTransactions(), neoBlock.getTransactions()) &&
                Objects.equals(getNextBlockHash(), neoBlock.getNextBlockHash());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getHash(), getSize(), getVersion(), getPrevBlockHash(), getMerkleRootHash(), getTime(),
                getIndex(), getPrimary(), getNextConsensus(), getWitnesses(), getTransactions(), getConfirmations(),
                getNextBlockHash());
    }

    @Override
    public String toString() {
        return "NeoBlock{" +
                "hash='" + hash + '\'' +
                ", size=" + size +
                ", version=" + version +
                ", prevBlockHash='" + prevBlockHash + '\'' +
                ", merkleRootHash='" + merkleRootHash + '\'' +
                ", time=" + time +
                ", index=" + index +
                ", primary=" + primary +
                ", nextConsensus='" + nextConsensus + '\'' +
                ", witnesses=" + witnesses +
                ", transactions=" + transactions +
                ", confirmations=" + confirmations +
                ", nextBlockHash='" + nextBlockHash + '\'' +
                '}';
    }

}
