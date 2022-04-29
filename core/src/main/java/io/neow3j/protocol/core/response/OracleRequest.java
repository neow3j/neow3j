package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.types.Hash160;
import io.neow3j.types.Hash256;

import java.math.BigInteger;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OracleRequest {

    @JsonProperty("requestid")
    private BigInteger requestId;

    @JsonProperty("originaltxid")
    private Hash256 originalTransactionHash;

    @JsonProperty("gasforresponse")
    private BigInteger gasForResponse;

    @JsonProperty("url")
    private String url;

    @JsonProperty("filter")
    private String filter;

    @JsonProperty("callbackcontract")
    private Hash160 callbackContract;

    @JsonProperty("callbackmethod")
    private String callbackMethod;

    @JsonProperty("userdata")
    private String userData;

    public OracleRequest() {
    }

    public OracleRequest(BigInteger requestId, Hash256 originalTransactionHash, BigInteger gasForResponse, String url,
            String filter, Hash160 callbackContract, String callbackMethod, String userData) {
        this.requestId = requestId;
        this.originalTransactionHash = originalTransactionHash;
        this.gasForResponse = gasForResponse;
        this.url = url;
        this.filter = filter;
        this.callbackContract = callbackContract;
        this.callbackMethod = callbackMethod;
        this.userData = userData;
    }

    public BigInteger getRequestId() {
        return requestId;
    }

    public Hash256 getOriginalTransactionHash() {
        return originalTransactionHash;
    }

    public BigInteger getGasForResponse() {
        return gasForResponse;
    }

    public String getUrl() {
        return url;
    }

    public String getFilter() {
        return filter;
    }

    public Hash160 getCallbackContract() {
        return callbackContract;
    }

    public String getCallbackMethod() {
        return callbackMethod;
    }

    public String getUserData() {
        return userData;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRequestId(), getOriginalTransactionHash(), getGasForResponse(), getUrl(), getFilter(),
                getCallbackContract(), getCallbackMethod(), getUserData());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OracleRequest)) {
            return false;
        }
        OracleRequest that = (OracleRequest) o;
        return Objects.equals(getRequestId(), that.getRequestId()) &&
                Objects.equals(getOriginalTransactionHash(), that.getOriginalTransactionHash()) &&
                Objects.equals(getGasForResponse(), that.getGasForResponse()) &&
                Objects.equals(getUrl(), that.getUrl()) &&
                Objects.equals(getFilter(), that.getFilter()) &&
                Objects.equals(getCallbackContract(), that.getCallbackContract()) &&
                Objects.equals(getCallbackMethod(), that.getCallbackMethod()) &&
                Objects.equals(getUserData(), that.getUserData());
    }

    @Override
    public String toString() {
        return "OracleResponse{" +
                "requestId=" + requestId +
                ", originalTxHash='" + originalTransactionHash + "'" +
                ", gasForResponse=" + gasForResponse +
                ", url='" + url + "'" +
                ", filter='" + filter + "'" +
                ", callbackContract='" + callbackContract + "'" +
                ", callbackMethod='" + callbackMethod + "'" +
                ", userData='" + userData + "'" +
                '}';
    }

}
