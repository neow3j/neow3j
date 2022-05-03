package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import io.neow3j.crypto.Base64;
import io.neow3j.types.Hash256;

import io.neow3j.types.NeoVMStateType;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Transaction {

    @JsonProperty("hash")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Hash256 hash;

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

    @JsonProperty("signers")
    private List<TransactionSigner> signers;

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
    private Hash256 blockHash;

    @JsonProperty("confirmations")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private int confirmations;

    @JsonProperty("blocktime")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private long blockTime;

    @JsonProperty("vmstate")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private NeoVMStateType vmState;

    public Transaction() {
    }

    public Transaction(Hash256 hash, long size, int version, Long nonce, String sender, String sysFee, String netFee,
            Long validUntilBlock, List<TransactionSigner> signers, List<TransactionAttribute> attributes, String script,
            List<NeoWitness> witnesses) {
        this.hash = hash;
        this.size = size;
        this.version = version;
        this.nonce = nonce;
        this.sender = sender;
        this.sysFee = sysFee;
        this.netFee = netFee;
        this.validUntilBlock = validUntilBlock;
        this.signers = signers;
        this.attributes = attributes;
        this.script = script;
        this.witnesses = witnesses;
    }

    public Transaction(Hash256 hash, long size, int version, Long nonce, String sender, String sysFee, String netFee,
            Long validUntilBlock, List<TransactionSigner> signers, List<TransactionAttribute> attributes, String script,
            List<NeoWitness> witnesses, Hash256 blockHash, int confirmations, long blockTime, NeoVMStateType vmState) {
        this.hash = hash;
        this.size = size;
        this.version = version;
        this.nonce = nonce;
        this.sender = sender;
        this.sysFee = sysFee;
        this.netFee = netFee;
        this.validUntilBlock = validUntilBlock;
        this.signers = signers;
        this.attributes = attributes;
        this.script = script;
        this.witnesses = witnesses;
        this.blockHash = blockHash;
        this.confirmations = confirmations;
        this.blockTime = blockTime;
        this.vmState = vmState;
    }

    public Transaction(io.neow3j.transaction.Transaction tx) {
        hash = tx.getTxId();
        size = tx.getSize();
        version = tx.getVersion();
        nonce = tx.getNonce();
        sender = tx.getSender().toString();
        sysFee = Long.toString(tx.getSystemFee());
        netFee = Long.toString(tx.getNetworkFee());
        validUntilBlock = tx.getValidUntilBlock();
        signers = tx.getSigners().stream().map(TransactionSigner::new).collect(Collectors.toList());
        attributes = tx.getAttributes().stream()
                .map(a -> TransactionAttribute.fromType(a.getType()))
                .collect(Collectors.toList());
        script = Base64.encode(tx.getScript());
        witnesses = tx.getWitnesses().stream().map(NeoWitness::new).collect(Collectors.toList());
        // The last four properties are not available.
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

    public List<TransactionSigner> getSigners() {
        return signers;
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

    public Hash256 getBlockHash() {
        return blockHash;
    }

    public int getConfirmations() {
        return confirmations;
    }

    public long getBlockTime() {
        return blockTime;
    }

    public NeoVMStateType getVMState() {
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
                Objects.equals(getSigners(), that.getSigners()) &&
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
        return Objects.hash(getHash(), getSize(), getVersion(), getNonce(), getSender(), getSysFee(), getNetFee(),
                getValidUntilBlock(), getSigners(), getAttributes(), getScript(), getWitnesses(), getBlockHash(),
                getConfirmations(), getBlockTime(), getVMState());
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
                ", signers=" + signers +
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
