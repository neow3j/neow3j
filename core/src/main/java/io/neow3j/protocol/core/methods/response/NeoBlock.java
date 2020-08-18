package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NeoBlock {

    @JsonProperty("hash")
    private String hash;

    @JsonProperty("size")
    private long size;

    @JsonProperty("version")
    private int version;

    @JsonProperty("previousblockhash")
    private String prevBlockHash;

    @JsonProperty("merkleroot")
    private String merkleRootHash;

    @JsonProperty("time")
    private long time;

    @JsonProperty("index")
    private long index;

    @JsonProperty("nextconsensus")
    private String nextConsensus;

    @JsonProperty("witnesses")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<NeoWitness> witnesses;

    @JsonProperty("consensusdata")
    private ConsensusData consensusData;

    @JsonProperty("tx")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Transaction> transactions;

    @JsonProperty("confirmations")
    private int confirmations;

    @JsonProperty("nextblockhash")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String nextBlockHash;

    public NeoBlock() {
    }

    public NeoBlock(String hash, long size, int version, String prevBlockHash,
            String merkleRootHash, long time, long index, String nextConsensus,
            List<NeoWitness> witnesses,
            ConsensusData consensusData,
            List<Transaction> transactions, int confirmations, String nextBlockHash) {
        this.hash = hash;
        this.size = size;
        this.version = version;
        this.prevBlockHash = prevBlockHash;
        this.merkleRootHash = merkleRootHash;
        this.time = time;
        this.index = index;
        this.nextConsensus = nextConsensus;
        this.witnesses = witnesses;
        this.consensusData = consensusData;
        this.transactions = transactions;
        this.confirmations = confirmations;
        this.nextBlockHash = nextBlockHash;
    }

    public String getHash() {
        return hash;
    }

    public long getSize() {
        return size;
    }

    public int getVersion() {
        return version;
    }

    public String getPrevBlockHash() {
        return prevBlockHash;
    }

    public String getMerkleRootHash() {
        return merkleRootHash;
    }

    public long getTime() {
        return time;
    }

    public long getIndex() {
        return index;
    }

    public String getNextConsensus() {
        return nextConsensus;
    }

    public List<NeoWitness> getWitnesses() {
        return witnesses;
    }

    public ConsensusData getConsensusData() {
        return consensusData;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public int getConfirmations() {
        return confirmations;
    }

    public String getNextBlockHash() {
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
                Objects.equals(getHash(), neoBlock.getHash()) &&
                Objects.equals(getPrevBlockHash(), neoBlock.getPrevBlockHash()) &&
                Objects.equals(getMerkleRootHash(), neoBlock.getMerkleRootHash()) &&
                Objects.equals(getNextConsensus(), neoBlock.getNextConsensus()) &&
                Objects.equals(getWitnesses(), neoBlock.getWitnesses()) &&
                Objects.equals(getConsensusData(), neoBlock.getConsensusData()) &&
                Objects.equals(getTransactions(), neoBlock.getTransactions()) &&
                Objects.equals(getNextBlockHash(), neoBlock.getNextBlockHash());
    }

    @Override
    public int hashCode() {
        return Objects
                .hash(getHash(), getSize(), getVersion(), getPrevBlockHash(), getMerkleRootHash(),
                        getTime(), getIndex(), getNextConsensus(), getWitnesses(),
                        getConsensusData(),
                        getTransactions(), getConfirmations(), getNextBlockHash());
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
                ", nextConsensus='" + nextConsensus + '\'' +
                ", witnesses=" + witnesses +
                ", consensusData=" + consensusData +
                ", transactions=" + transactions +
                ", confirmations=" + confirmations +
                ", nextBlockHash='" + nextBlockHash + '\'' +
                '}';
    }
}
