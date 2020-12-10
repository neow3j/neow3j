package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.contract.ScriptHash;

import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OracleRequest {

    @JsonProperty(value = "originaltxid", required = true)
    private int originalTxId;

    @JsonProperty(value = "gasforresponse", required = true)
    private long gasForResponse;

    @JsonProperty("url")
    private String url;

    @JsonProperty("filter")
    private String filter;

    @JsonProperty("callbackcontract")
    private ScriptHash callbackContract;

    @JsonProperty("callbackmethod")
    private String callbackMethod;

    @JsonProperty("userdata")
    private List<String> userData;

    public OracleRequest() {
    }

    public OracleRequest(int originalTxId, long gasForResponse, String url, String filter,
                         ScriptHash callbackContract, String callbackMethod, List<String> userData) {
        this.originalTxId = originalTxId;
        this.gasForResponse = gasForResponse;
        this.url = url;
        this.filter = filter;
        this.callbackContract = callbackContract;
        this.callbackMethod = callbackMethod;
        this.userData = userData;
    }

    /**
     * Gets the original transaction id.
     *
     * @return the original transaction id.
     */
    public int getOriginalTxId() {
        return originalTxId;
    }

    /**
     * Gets the gas cost for the response.
     *
     * @return the gas cost for the response.
     */
    public long getGasForResponse() {
        return gasForResponse;
    }

    /**
     * Gets the URL.
     *
     * @return the URL.
     */
    public String getURL() {
        return url;
    }

    /**
     * Gets the filter.
     *
     * @return the filter.
     */
    public String getFilter() {
        return filter;
    }

    /**
     * Gets the callback contract.
     *
     * @return the callback contract.
     */
    public ScriptHash getCallbackContract() {
        return callbackContract;
    }

    /**
     * Gets the callback method.
     *
     * @return the callback method.
     */
    public String getCallbackMethod() {
        return callbackMethod;
    }

    /**
     * Gets the user data.
     *
     * @return the user data.
     */
    public List<String> getUserData() {
        return userData;
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
        return Objects.equals(getOriginalTxId(), that.getOriginalTxId()) &&
                Objects.equals(getGasForResponse(), that.getGasForResponse()) &&
                Objects.equals(getURL(), that.getURL()) &&
                Objects.equals(getFilter(), that.getFilter()) &&
                Objects.equals(getCallbackContract(), that.getCallbackContract()) &&
                Objects.equals(getCallbackMethod(), that.getCallbackMethod()) &&
                Objects.equals(getUserData(), that.getUserData());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOriginalTxId(), getGasForResponse(), getURL(), getFilter(),
                getCallbackContract(), getCallbackMethod(), getUserData());
    }

    @Override
    public String toString() {
        return "OracleRequest{" +
                "originalTxId='" + originalTxId + '\'' +
                "gasForResponse='" + gasForResponse + '\'' +
                "url='" + url + '\'' +
                "filter='" + filter + '\'' +
                "callbackContract='" + callbackContract + '\'' +
                "callbackMethod='" + callbackMethod + '\'' +
                "userData='" + userData + '\'' +
                "}";
    }
}
