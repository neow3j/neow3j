package io.neow3j.contract;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.constants.NeoConstants;
import io.neow3j.model.types.ContractParameterType;
import io.neow3j.utils.ArrayUtils;
import io.neow3j.utils.Keys;
import io.neow3j.utils.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

/**
 * Contract parameters are used for example in contract invocations and represent an input parameter.
 * But it can also represent an output type and value. <br><br>
 *
 * The static creation methods in this class all create parameters with values of type string. E.g.
 * if you create a parameter of type {@link ContractParameterType#INTEGER} the value will be stored
 * as a string. The only exceptions are {@link ContractParameterType#BOOLEAN} and
 * {@link ContractParameterType#ARRAY} type parameters. The reason for this is the serialization and
 * deserialization of <code>ContractParameter</code>'s. It works without any custom code when
 * strings are used.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContractParameter {

    @JsonProperty("name")
    private String paramName;

    @JsonProperty("type")
    private ContractParameterType paramType;

    @JsonProperty("value")
    protected Object value;

    protected ContractParameter() {
    }

    private ContractParameter(ContractParameterType paramType, Object value) {
        this.paramType = paramType;
        this.value = value;
    }

    public static ContractParameter string(String value) {
        return new ContractParameter(ContractParameterType.STRING, value);
    }

    public static ContractParameter array(List<ContractParameter> params) {
        return array(params.toArray(new ContractParameter[0]));
    }

    public static ContractParameter array(ContractParameter... params) {
        return new ContractParameter(ContractParameterType.ARRAY, params);
    }

    /**
     * Creates a byte array parameter from the given value.
     *
     * @param byteArray The parameter value.
     * @return the contract parameter.
     */
    public static ContractParameter byteArray(byte[] byteArray) {
        return byteArray(Numeric.toHexStringNoPrefix(byteArray));
    }

    /**
     * Creates a byte array parameter from the given string. <br><br>
     *
     * If this parameter is used in an invocation, and its string is not a valid hexadecimal number
     * it will be converted to the number made up from his UTF8 characters before adding it to the
     * vm script.
     *
     * @param value The value as a string.
     * @return the contract parameter.
     */
    public static ContractParameter byteArray(String value) {
        value = Numeric.cleanHexPrefix(value);
        return new ContractParameter(ContractParameterType.BYTE_ARRAY, value);
    }

    /**
     * Creates a byte array parameter from the given address.
     * The address is converted to its script hash.
     *
     * @param address An address.
     * @return the contract parameter.
     */
    public static ContractParameter byteArrayFromAddress(String address) {
        if (!Keys.isValidAddress(address)) {
            throw new IllegalArgumentException("Not a valid address.");
        }
        return byteArray(Keys.toScriptHash(address));
    }

    /**
     * Creates a byte array parameter from the given number, transforming it to the Fixed8 number
     * format in little-andian order.
     *
     * @param number A decimal number
     * @return the contract parameter.
     */
    public static ContractParameter fixed8ByteArray(BigDecimal number) {
        return byteArray(Numeric.fromDecimalToFixed8ByteArray(number));
    }

    /**
     * Creates a signature parameter from the given signature hexadecimal string.
     *
     * @param signatureHexString A signature as hexadecimal string.
     * @return the contract parameter.
     */
    public static ContractParameter signature(String signatureHexString) {
        if (!Numeric.isValidHexString(signatureHexString)) {
            throw new IllegalArgumentException("String is not a valid hex number");
        }
        signatureHexString = Numeric.cleanHexPrefix(signatureHexString);
        if (signatureHexString.length() != NeoConstants.SIGNATURE_SIZE_HEXSTRING) {
            throw new IllegalArgumentException("Signature is expected to have a length of " +
                    NeoConstants.SIGNATURE_SIZE_BYTES + " bytes, but had " +
                    signatureHexString.length()/2 + ".");
        }
        return new ContractParameter(ContractParameterType.SIGNATURE, signatureHexString);
    }

    /**
     * Creates a signature parameter from the given signature.
     *
     * @param signature A signature.
     * @return the contract parameter.
     */
    public static ContractParameter signature(byte[] signature) {
        return signature(Numeric.toHexStringNoPrefix(signature));
    }

    /**
     * Creates a boolean parameter from the given boolean.
     *
     * @param  bool a boolean value.
     * @return the contract parameter.
     */
    public static ContractParameter bool(boolean bool) {
        return new ContractParameter(ContractParameterType.BOOLEAN, bool);
    }

    /**
     * Creates an integer parameter from the given integer.
     *
     * @param  integer an integer value.
     * @return the contract parameter.
     */
    public static ContractParameter integer(int integer) {
        return integer(BigInteger.valueOf(integer));
    }

    /**
     * Creates an integer parameter from the given integer.
     *
     * @param  integer an integer value.
     * @return the contract parameter.
     */
    public static ContractParameter integer(BigInteger integer) {
        return new ContractParameter(ContractParameterType.INTEGER, integer.toString());
    }

    /**
     * Creates a hash160 parameter from the given hexadecimal string.
     *
     * @param  hashHexString a hash160 value as hexadecimal string.
     * @return the contract parameter.
     */
    public static ContractParameter hash160(String hashHexString) {
        if (!Numeric.isValidHexString(hashHexString)) {
            throw new IllegalArgumentException("String is not a valid hex number");
        }
        hashHexString = Numeric.cleanHexPrefix(hashHexString);
        if (hashHexString.length() != NeoConstants.SCRIPTHASH_LENGHT_HEXSTRING) {
            throw new IllegalArgumentException("Hash160 is expected to have a length of " +
                    NeoConstants.SCRIPTHASH_LENGHT_BYTES + " bytes, but had " +
                    hashHexString.length()/2 + ".");
        }
        return new ContractParameter(ContractParameterType.HASH160, hashHexString);
    }

    /**
     * Creates a hash160 parameter from the given hash.
     *
     * @param  hash a hash160 value.
     * @return the contract parameter.
     */
    public static ContractParameter hash160(byte[] hash) {
        return hash160(Numeric.toHexStringNoPrefix(hash));
    }

    /**
     * Creates a hash256 parameter from the given hexadecimal string.
     *
     * @param  hashHexString a hash256 value as hexadecimal string.
     * @return the contract parameter.
     */
    public static ContractParameter hash256(String hashHexString) {
        if (!Numeric.isValidHexString(hashHexString)) {
            throw new IllegalArgumentException("String is not a valid hex number");
        }
        hashHexString = Numeric.cleanHexPrefix(hashHexString);
        if (hashHexString.length() != 64) {
            throw new IllegalArgumentException("Hash256 is expected to have a length of 32 " +
                    "bytes, but had " + hashHexString.length()/2 + ".");
        }
        return new ContractParameter(ContractParameterType.HASH256, hashHexString);
    }

    /**
     * Creates a hash256 parameter from the given hash.
     *
     * @param  hash a hash256 value.
     * @return the contract parameter.
     */
    public static ContractParameter hash256(byte[] hash) {
        return hash256(Numeric.toHexStringNoPrefix(hash));
    }

    public static ContractParameter publicKey(String publicKey) {
        // TODO 17.07.19 claude: Implement
        throw new UnsupportedOperationException();
    }

    public static ContractParameter publicKey(byte[] publicKey) {
        // TODO 17.07.19 claude: Implement
        throw new UnsupportedOperationException();
    }

    public String getParamName() {
        return paramName;
    }

    public ContractParameterType getParamType() {
        return paramType;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContractParameter)) return false;
        ContractParameter that = (ContractParameter) o;
        return Objects.equals(paramName, that.paramName) &&
                paramType == that.paramType &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paramName, paramType, value);
    }

    @Override
    public String toString() {
        return "ContractParameter{" +
                "paramName='" + paramName + '\'' +
                ", paramType=" + paramType +
                ", value=" + value +
                '}';
    }

}
