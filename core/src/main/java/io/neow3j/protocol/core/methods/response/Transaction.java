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

}
