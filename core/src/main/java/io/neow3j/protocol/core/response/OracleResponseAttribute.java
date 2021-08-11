package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import io.neow3j.transaction.TransactionAttributeType;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OracleResponseAttribute extends TransactionAttribute {

    @JsonUnwrapped
    private OracleResponse oracleResponse;

    public OracleResponseAttribute() {
        super(TransactionAttributeType.ORACLE_RESPONSE);
    }

    public OracleResponseAttribute(OracleResponse oracleResponse) {
        super(TransactionAttributeType.ORACLE_RESPONSE);
        this.oracleResponse = oracleResponse;
    }

    /**
     * Gets the oracle response.
     *
     * @return the oracle response.
     */
    public OracleResponse getOracleResponse() {
        return oracleResponse;
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
        return Objects.equals(getOracleResponse(), that.getOracleResponse());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOracleResponse());
    }

    @Override
    public String toString() {
        return "OracleResponseAttribute{" +
                "response=" + oracleResponse +
                '}';
    }

}
