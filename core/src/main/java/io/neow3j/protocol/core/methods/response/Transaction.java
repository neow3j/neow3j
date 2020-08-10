package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Transaction {

    @JsonProperty("hash")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String hash;

    @JsonProperty("size")
    private long size;

    @JsonProperty("version")
    private int version;

    @JsonProperty("nonce")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long nonce;

    @JsonProperty("sender")
    private String sender;

    @JsonProperty("sysfee")
    private String sysFee;

    @JsonProperty("netfee")
    private String netFee;

    @JsonProperty("validuntilblock")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long validUntilBlock;

    @JsonProperty("attributes")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<TransactionAttribute> attributes;

    @JsonProperty("script")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String script;

    @JsonProperty("witnesses")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<NeoWitness> witnesses;

    @JsonProperty("blockhash")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private String blockHash;

    @JsonProperty("confirmations")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private int confirmations;

    @JsonProperty("blocktime")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private long blockTime;

    @JsonProperty("vmstate")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private String vmState;

    public Transaction() {
    }

    public Transaction(String hash, long size, int version, Long nonce, String sender,
            String sysFee, String netFee, Long validUntilBlock,
            List<TransactionAttribute> attributes, String script,
            List<NeoWitness> witnesses) {
        this.hash = hash;
        this.size = size;
        this.version = version;
        this.nonce = nonce;
        this.sender = sender;
        this.sysFee = sysFee;
        this.netFee = netFee;
        this.validUntilBlock = validUntilBlock;
        this.attributes = attributes;
        this.script = script;
        this.witnesses = witnesses;
    }

    public Transaction(String hash, long size, int version, Long nonce, String sender,
                       String sysFee, String netFee, Long validUntilBlock,
                       List<TransactionAttribute> attributes, String script,
                       List<NeoWitness> witnesses, String blockHash, int confirmations,
                       long blockTime, String vmState) {
        this.hash = hash;
        this.size = size;
        this.version = version;
        this.nonce = nonce;
        this.sender = sender;
        this.sysFee = sysFee;
        this.netFee = netFee;
        this.validUntilBlock = validUntilBlock;
        this.attributes = attributes;
        this.script = script;
        this.witnesses = witnesses;
        this.blockHash = blockHash;
        this.confirmations = confirmations;
        this.blockTime = blockTime;
        this.vmState = vmState;
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

    public Long getNonce() {
        return nonce;
    }

    public String getSender() {
        return sender;
    }

    public String getSysFee() {
        return sysFee;
    }

    public String getNetFee() {
        return netFee;
    }

    public Long getValidUntilBlock() {
        return validUntilBlock;
    }

    public List<TransactionAttribute> getAttributes() {
        return attributes;
    }

    public String getScript() {
        return script;
    }

    public List<NeoWitness> getWitnesses() {
        return witnesses;
    }

    public String getBlockHash() {
        return blockHash;
    }

    public int getConfirmations() {
        return confirmations;
    }

    public long getBlockTime() {
        return blockTime;
    }

    public String getVMState() {
        return vmState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Transaction)) {
            return false;
        }
        Transaction that = (Transaction) o;
        return getSize() == that.getSize() &&
                getVersion() == that.getVersion() &&
                Objects.equals(getHash(), that.getHash()) &&
                Objects.equals(getNonce(), that.getNonce()) &&
                Objects.equals(getSender(), that.getSender()) &&
                Objects.equals(getSysFee(), that.getSysFee()) &&
                Objects.equals(getNetFee(), that.getNetFee()) &&
                Objects.equals(getValidUntilBlock(), that.getValidUntilBlock()) &&
                Objects.equals(getAttributes(), that.getAttributes()) &&
                Objects.equals(getScript(), that.getScript()) &&
                Objects.equals(getWitnesses(), that.getWitnesses()) &&
                Objects.equals(getBlockHash(), that.getBlockHash()) &&
                Objects.equals(getConfirmations(), that.getConfirmations()) &&
                Objects.equals(getBlockTime(), that.getBlockTime()) &&
                Objects.equals(getVMState(), that.getVMState());
    }

    @Override
    public int hashCode() {
        return Objects
                .hash(getHash(), getSize(), getVersion(), getNonce(), getSender(), getSysFee(),
                        getNetFee(), getValidUntilBlock(), getAttributes(), getScript(),
                        getWitnesses(), getBlockHash(), getConfirmations(), getBlockTime(),
                        getVMState());
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "hash='" + hash + '\'' +
                ", size=" + size +
                ", version=" + version +
                ", nonce=" + nonce +
                ", sender='" + sender + '\'' +
                ", sysFee='" + sysFee + '\'' +
                ", netFee='" + netFee + '\'' +
                ", validUntilBlock=" + validUntilBlock +
                ", attributes=" + attributes +
                ", script='" + script + '\'' +
                ", witnesses=" + witnesses +
                ", blockHash=" + blockHash +
                ", confirmations=" + confirmations +
                ", blockTime=" + blockTime +
                ", vmState=" + vmState +
                '}';
    }
}
