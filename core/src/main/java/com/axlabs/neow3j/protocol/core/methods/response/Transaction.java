package com.axlabs.neow3j.protocol.core.methods.response;

import com.axlabs.neow3j.model.types.TransactionType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

import java.util.List;
import java.util.Objects;

public class Transaction {

    @JsonProperty("txid")
    private String transactionId;

    @JsonProperty("size")
    private long size;

    @JsonProperty("type")
    private TransactionType type;

    @JsonProperty("version")
    private int version;

    @JsonProperty("attributes")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<TransactionAttribute> attributes;

    @JsonProperty("vin")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<TransactionInput> inputs;

    @JsonProperty("vout")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<TransactionOutput> outputs;

    @JsonProperty("sys_fee")
    private String sysFee;

    @JsonProperty("net_fee")
    private String netFee;

    @JsonProperty("scripts")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<Script> scripts;

    @JsonProperty("script")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String script;

    @JsonProperty("gas")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String gas;

    @JsonProperty("nonce")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long nonce;

    public Transaction() {
    }

    public Transaction(String transactionId, long size, TransactionType type, int version, List<TransactionAttribute> attributes, List<TransactionInput> inputs, List<TransactionOutput> outputs, String sysFee, String netFee, List<Script> scripts, String script, String gas, Long nonce) {
        this.transactionId = transactionId;
        this.size = size;
        this.type = type;
        this.version = version;
        this.attributes = attributes;
        this.inputs = inputs;
        this.outputs = outputs;
        this.sysFee = sysFee;
        this.netFee = netFee;
        this.scripts = scripts;
        this.script = script;
        this.gas = gas;
        this.nonce = nonce;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public long getSize() {
        return size;
    }

    public TransactionType getType() {
        return type;
    }

    public int getVersion() {
        return version;
    }

    public List<TransactionAttribute> getAttributes() {
        return attributes;
    }

    public List<TransactionInput> getInputs() {
        return inputs;
    }

    public List<TransactionOutput> getOutputs() {
        return outputs;
    }

    public String getSysFee() {
        return sysFee;
    }

    public String getNetFee() {
        return netFee;
    }

    public List<Script> getScripts() {
        return scripts;
    }

    public String getScript() {
        return script;
    }

    public String getGas() {
        return gas;
    }

    public Long getNonce() {
        return nonce;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction)) return false;
        Transaction that = (Transaction) o;
        return getSize() == that.getSize() &&
                getVersion() == that.getVersion() &&
                Objects.equals(getTransactionId(), that.getTransactionId()) &&
                getType() == that.getType() &&
                Objects.equals(getAttributes(), that.getAttributes()) &&
                Objects.equals(getInputs(), that.getInputs()) &&
                Objects.equals(getOutputs(), that.getOutputs()) &&
                Objects.equals(getSysFee(), that.getSysFee()) &&
                Objects.equals(getNetFee(), that.getNetFee()) &&
                Objects.equals(getScripts(), that.getScripts()) &&
                Objects.equals(getScript(), that.getScript()) &&
                Objects.equals(getGas(), that.getGas()) &&
                Objects.equals(getNonce(), that.getNonce());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTransactionId(), getSize(), getType(), getVersion(), getAttributes(), getInputs(), getOutputs(), getSysFee(), getNetFee(), getScripts(), getScript(), getGas(), getNonce());
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId='" + transactionId + '\'' +
                ", size=" + size +
                ", type=" + type +
                ", version=" + version +
                ", attributes=" + attributes +
                ", inputs=" + inputs +
                ", outputs=" + outputs +
                ", sysFee='" + sysFee + '\'' +
                ", netFee='" + netFee + '\'' +
                ", scripts=" + scripts +
                ", script='" + script + '\'' +
                ", gas='" + gas + '\'' +
                ", nonce=" + nonce +
                '}';
    }
}
