package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigInteger;
import java.util.Objects;

public class OracleResponse {

    @JsonProperty(value = "id", required = true)
    private BigInteger id;

    @JsonProperty(value = "code")
    private OracleResponseCode responseCode;

    @JsonProperty("result")
    private String result;

    public OracleResponse() {
    }

    /**
     * Constructs a new OracleResponse object.
     *
     * @param id           the request id.
     * @param responseCode the response code.
     * @param result       the result.
     */
    public OracleResponse(BigInteger id, OracleResponseCode responseCode, String result) {
        this.id = id;
        this.responseCode = responseCode;
        this.result = result;
    }


    /**
     * Gets the response id.
     *
     * @return the response id.
     */
    public BigInteger getId() {
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
        if (!(o instanceof OracleResponse)) {
            return false;
        }
        OracleResponse that = (OracleResponse) o;
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
                ", code='" + responseCode + '\'' +
                ", result='" + result + '\'' +
                "}";
    }

}
