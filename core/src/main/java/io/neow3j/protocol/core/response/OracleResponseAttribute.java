package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.transaction.TransactionAttributeType;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OracleResponseAttribute extends TransactionAttribute {

    public OracleResponseAttribute() {
        super(TransactionAttributeType.ORACLE_RESPONSE);
    }

    public OracleResponseAttribute(int id, OracleResponseCode responseCode, String result) {
        super(TransactionAttributeType.ORACLE_RESPONSE);
        this.id = id;
        this.responseCode = responseCode;
        this.result = result;
    }

    @JsonProperty(value = "id", required = true)
    private int id;

    @JsonProperty(value = "code")
    private OracleResponseCode responseCode;

    @JsonProperty("result")
    private String result;

    /**
     * Gets the response id.
     *
     * @return the response id.
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the response code.
     *
     * @return the response code.
     */
    public OracleResponseCode getResponseCode() {
        return responseCode;
    }

    /**
     * Gets the result.
     *
     * @return the result.
     */
    public String getResult() {
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OracleResponseAttribute)) {
            return false;
        }
        OracleResponseAttribute that = (OracleResponseAttribute) o;
        return Objects.equals(getId(), that.getId()) &&
               Objects.equals(getResponseCode(), that.getResponseCode()) &&
               Objects.equals(getResult(), that.getResult());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getResponseCode(), getResult());
    }

    @Override
    public String toString() {
        return "OracleResponse{" +
               "id='" + id + '\'' +
               "code='" + responseCode + '\'' +
               "result='" + result + '\'' +
               "}";
    }
}
