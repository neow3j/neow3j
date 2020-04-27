package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import io.neow3j.model.types.TransactionType;

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

    @JsonProperty("sys_fee")
    private String sysFee;

    @JsonProperty("net_fee")
    private String netFee;

    @JsonProperty("valid_until_block")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long validUntilBlock;

    @JsonProperty("attributes")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<TransactionAttribute> attributes;

    @JsonProperty("cosigners")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<TransactionCosigner> cosigners;

    @JsonProperty("script")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String script;

    @JsonProperty("gas")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String gas;

    public Transaction() {
    }

    public Transaction(String hash, long size, int version, Long nonce, String sender,
            String sysFee, String netFee, Long validUntilBlock,
            List<TransactionAttribute> attributes,
            List<TransactionCosigner> cosigners, String script, String gas) {
        this.hash = hash;
        this.size = size;
        this.version = version;
        this.nonce = nonce;
        this.sender = sender;
        this.sysFee = sysFee;
        this.netFee = netFee;
        this.validUntilBlock = validUntilBlock;
        this.attributes = attributes;
        this.cosigners = cosigners;
        this.script = script;
        this.gas = gas;
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

    public List<TransactionCosigner> getCosigners() {
        return cosigners;
    }

    public String getScript() {
        return script;
    }

    public String getGas() {
        return gas;
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
                Objects.equals(getCosigners(), that.getCosigners()) &&
                Objects.equals(getScript(), that.getScript()) &&
                Objects.equals(getGas(), that.getGas());
    }

    @Override
    public int hashCode() {
        return Objects
                .hash(getHash(), getSize(), getVersion(), getNonce(), getSender(), getSysFee(),
                        getNetFee(), getValidUntilBlock(), getAttributes(), getCosigners(),
                        getScript(),
                        getGas());
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
                ", cosigners=" + cosigners +
                ", script='" + script + '\'' +
                ", gas='" + gas + '\'' +
                '}';
    }
}
