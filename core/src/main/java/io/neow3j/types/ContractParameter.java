package io.neow3j.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.neow3j.constants.NeoConstants;
import io.neow3j.types.ContractParameter.ContractParameterSerializer;
import io.neow3j.crypto.Base64;
import io.neow3j.wallet.Account;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.neow3j.types.ContractParameterType.ARRAY;
import static io.neow3j.types.ContractParameterType.HASH256;
import static io.neow3j.types.ContractParameterType.INTEGER;
import static io.neow3j.types.ContractParameterType.MAP;
import static io.neow3j.utils.Numeric.hexStringToByteArray;
import static io.neow3j.utils.Numeric.isValidHexString;
import static io.neow3j.utils.Numeric.toHexStringNoPrefix;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Contract parameters represent an input parameter for contract invocations.
 */
@JsonSerialize(using = ContractParameterSerializer.class)
@SuppressWarnings("unchecked")
public class ContractParameter {

    @JsonProperty("name")
    private String paramName;

    @JsonProperty("type")
    private ContractParameterType paramType;

    @JsonProperty("value")
    protected Object value;

    private ContractParameter() {
    }

    protected ContractParameter(String name, ContractParameterType paramType, Object value) {
        this.paramName = name;
        this.paramType = paramType;
        this.value = value;
    }

    public ContractParameter(String name, ContractParameterType paramType) {
        this(name, paramType, null);
    }

    protected ContractParameter(ContractParameterType paramType, Object value) {
        this(null, paramType, value);
    }

    /**
     * Creates a contract parameter from the given value.
     *
     * @param value any object value.
     * @return the contract parameter.
     */
    public static ContractParameter any(Object value) {
        return new ContractParameter(ContractParameterType.ANY, value);
    }

    /**
     * Creates a string parameter from the given value.
     *
     * @param value the string value.
     * @return the contract parameter.
     */
    public static ContractParameter string(String value) {
        return new ContractParameter(ContractParameterType.STRING, value);
    }

    /**
     * Creates an array parameter from the given values.
     * <p>
     * This method supports parameters of types Boolean, Integer, byte[] and String. Array
     * entries of different contract parameter types first need to be instantiated as a
     * {@code ContractParameter} and can then be passed as a parameter as well.
     *
     * @param entries the array entries.
     * @return the contract parameter.
     */
    public static ContractParameter array(Object... entries) {
        if (entries.length == 0) {
            throw new IllegalArgumentException("At least one parameter is required to create an " +
                    "array contract parameter.");
        }
        if (Arrays.stream(entries).anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Cannot add a null object to an array contract " +
                    "parameter.");
        }
        ContractParameter[] params = Arrays.stream(entries)
                .map(ContractParameter::castToContractParameter)
                .toArray(ContractParameter[]::new);
        return new ContractParameter(ContractParameterType.ARRAY, params);
    }

    /**
     * Creates an array parameter from the given entries.
     *
     * @param entries the array entries in a list.
     * @return the contract parameter.
     */
    public static ContractParameter array(List<?> entries) {
        return array(entries.toArray());
    }

    /**
     * Creates a map contract parameter.
     * <p>
     * The {@code Map} argument can hold any types that can be cast to one of the available
     * {@link ContractParameterType}s. The types {@link ContractParameterType#ARRAY} and
     * {@link ContractParameterType#MAP} are not supported as map keys.
     * <p>
     * The first example below uses regular Java types that can automatically be wrapped into a
     * {@code ContractParameter}.
     * <pre>
     * Map map = new HashMap{@literal <>}();
     * map.put("one", "first");
     * map.put("two", 2);
     * ContractParameter param = map(map);
     * </pre>
     * The second example leads to the same result but uses {@code ContractParameter} before
     * adding elements.
     * <pre>
     * Map map = new HashMap{@literal <>}();
     * map.put(ContractParameter.string("one"), ContractParameter.string("first"));
     * map.put(ContractParameter.integer("two"), ContractParameter.integer(2));
     * ContractParameter param = map(map);
     * </pre>
     *
     * @param map The map entries.
     * @return the contract parameter.
     */
    public static ContractParameter map(Map<?, ?> map) {
        if (map.isEmpty()) {
            throw new IllegalArgumentException("At least one map entry is required to create a " +
                    "map contract parameter.");
        }

        Map<ContractParameter, ContractParameter> paramMap = new HashMap<>();
        map.forEach((k, v) -> {
            ContractParameter key = castToContractParameter(k);
            ContractParameter value = castToContractParameter(v);
            if (key.getParamType().equals(ARRAY) || key.getParamType().equals(MAP)) {
                throw new IllegalArgumentException("The provided map contains an invalid key. The" +
                        " keys cannot be of type array or map.");
            }
            paramMap.put(key, value);
        });
        return new ContractParameter(ContractParameterType.MAP, paramMap);
    }

    private static ContractParameter castToContractParameter(Object o) {
        if (o instanceof ContractParameter) {
            return (ContractParameter) o;
        } else if (o instanceof Boolean) {
            return bool((Boolean) o);
        } else if (o instanceof Integer) {
            return integer((Integer) o);
        } else if (o instanceof Long) {
            return integer(BigInteger.valueOf((Long) o));
        } else if (o instanceof BigInteger) {
            return integer((BigInteger) o);
        } else if (o instanceof byte[]) {
            return byteArray((byte[]) o);
        } else if (o instanceof String) {
            return string((String) o);
        } else if (o instanceof Hash160) {
            return hash160((Hash160) o);
        } else if (o instanceof Hash256) {
            return hash256((Hash256) o);
        } else if (o instanceof Account) {
            return hash160((Account) o);
        } else {
            throw new IllegalArgumentException("The provided object could not be casted into " +
                    "a supported contract parameter type.");
        }
    }

    /**
     * Creates a byte array parameter from the given value.
     * <p>
     * Make sure that the array is in the right byte order, i.e., endianness.
     *
     * @param byteArray the parameter value.
     * @return the contract parameter.
     */
    public static ContractParameter byteArray(byte[] byteArray) {
        return new ContractParameter(ContractParameterType.BYTE_ARRAY, byteArray);
    }

    /**
     * Creates a byte array parameter from the given hex string.
     *
     * @param hexString the hexadecimal string.
     * @return the contract parameter.
     */
    public static ContractParameter byteArray(String hexString) {
        if (!isValidHexString(hexString)) {
            throw new IllegalArgumentException("Argument is not a valid hex number.");
        }
        return byteArray(hexStringToByteArray(hexString));
    }

    /**
     * Create a byte array parameter from a string by converting the string to bytes using the UTF-8
     * character set.
     *
     * @param value the parameter value.
     * @return the contract parameter.
     */
    public static ContractParameter byteArrayFromString(String value) {
        return byteArray(value.getBytes(UTF_8));
    }

    /**
     * Creates a signature parameter from the given signature hexadecimal string.
     *
     * @param signatureHexString a signature as hexadecimal string.
     * @return the contract parameter.
     */
    public static ContractParameter signature(String signatureHexString) {
        if (!isValidHexString(signatureHexString)) {
            throw new IllegalArgumentException("Argument is not a valid hex number.");
        }
        return signature(hexStringToByteArray(signatureHexString));
    }

    /**
     * Creates a signature parameter from the given signature.
     *
     * @param signature a signature.
     * @return the contract parameter.
     */
    public static ContractParameter signature(byte[] signature) {
        if (signature.length != NeoConstants.SIGNATURE_SIZE) {
            throw new IllegalArgumentException("Signature is expected to have a length of " +
                    NeoConstants.SIGNATURE_SIZE + " bytes, but had " + signature.length + ".");
        }
        return new ContractParameter(ContractParameterType.SIGNATURE, signature);
    }

    /**
     * Creates a boolean parameter from the given boolean.
     *
     * @param bool a boolean value.
     * @return the contract parameter.
     */
    public static ContractParameter bool(boolean bool) {
        return new ContractParameter(ContractParameterType.BOOLEAN, bool);
    }

    /**
     * Creates an integer parameter from the given integer.
     *
     * @param integer an integer value.
     * @return the contract parameter.
     */
    public static ContractParameter integer(int integer) {
        return integer(BigInteger.valueOf(integer));
    }

    /**
     * Creates an integer parameter from the given integer.
     *
     * @param integer an integer value.
     * @return the contract parameter.
     */
    public static ContractParameter integer(BigInteger integer) {
        return new ContractParameter(INTEGER, integer);
    }

    /**
     * Creates a hash160 parameter from the given account.
     *
     * @param account an account.
     * @return the contract parameter.
     */
    public static ContractParameter hash160(Account account) {
        return hash160(account.getScriptHash());
    }

    /**
     * Creates a hash160 parameter from the given script hash.
     *
     * @param hash a script hash
     * @return the contract parameter.
     */
    public static ContractParameter hash160(Hash160 hash) {
        if (hash == null) {
            throw new IllegalArgumentException("The script hash argument must not be null.");
        }
        return new ContractParameter(ContractParameterType.HASH160, hash);
    }

    /**
     * Creates a hash256 parameter from the given hex string.
     *
     * @param hashHexString a 256-bit hash in hexadecimal and big-endian order.
     * @return the contract parameter.
     */
    public static ContractParameter hash256(String hashHexString) {
        if (!isValidHexString(hashHexString)) {
            throw new IllegalArgumentException("Argument is not a valid hex number.");
        }
        return hash256(hexStringToByteArray(hashHexString));
    }

    /**
     * Creates a hash256 parameter from the given hash.
     *
     * @param hash a 256-bit hash.
     * @return the contract parameter.
     */
    public static ContractParameter hash256(Hash256 hash) {
        return new ContractParameter(HASH256, hash);
    }

    /**
     * Creates a hash256 parameter from the given bytes.
     *
     * @param hash a 256-bit hash in big-endian order.
     * @return the contract parameter.
     */
    public static ContractParameter hash256(byte[] hash) {
        if (hash.length != 32) {
            throw new IllegalArgumentException("A Hash256 parameter must be 32 bytes but was " +
                    hash.length + " bytes.");
        }
        return hash256(new Hash256(hash));
    }

    /**
     * Creates a public key parameter from the given public key.
     * <p>
     * The public key must be encoded in compressed format as described in section 2.3.3 of
     * <a href="http://www.secg.org/sec1-v2.pdf">SEC1</a>.
     *
     * @param publicKey the public key in hexadecimal representation.
     * @return the contract parameter.
     */
    public static ContractParameter publicKey(String publicKey) {
        return publicKey(hexStringToByteArray(publicKey));
    }

    /**
     * Creates a public key parameter from the given public key bytes.
     * <p>
     * The public key must be encoded in compressed format as described in section 2.3.3 of
     * <a href="http://www.secg.org/sec1-v2.pdf">SEC1</a>.
     *
     * @param publicKey the public key to use in the parameter.
     * @return the contract parameter.
     */
    public static ContractParameter publicKey(byte[] publicKey) {
        if (publicKey.length != NeoConstants.PUBLIC_KEY_SIZE) {
            throw new IllegalArgumentException("Public key argument must be " +
                    NeoConstants.PUBLIC_KEY_SIZE + " bytes but was " + publicKey.length +
                    " bytes.");
        }
        return new ContractParameter(ContractParameterType.PUBLIC_KEY, publicKey);
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
        if (this == o) {
            return true;
        }
        if (!(o instanceof ContractParameter)) {
            return false;
        }
        ContractParameter that = (ContractParameter) o;

        if (paramType == that.paramType &&
                Objects.equals(paramName, that.paramName)) {

            if (paramType.equals(ContractParameterType.BYTE_ARRAY) ||
                    paramType.equals(ContractParameterType.SIGNATURE) ||
                    paramType.equals(ContractParameterType.PUBLIC_KEY) ||
                    paramType.equals(ContractParameterType.HASH160) ||
                    paramType.equals(ContractParameterType.HASH256)) {

                return Arrays.equals((byte[]) value, (byte[]) that.value);
            } else if (paramType.equals(ContractParameterType.ARRAY)) {
                ContractParameter[] thatValue = (ContractParameter[]) that.getValue();
                ContractParameter[] oValue =
                        (ContractParameter[]) ((ContractParameter) o).getValue();
                return Arrays.equals(oValue, thatValue);
            } else {
                return Objects.equals(value, that.value);
            }
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(paramName, paramType, value);
    }

    protected static class ContractParameterSerializer extends StdSerializer<ContractParameter> {

        public ContractParameterSerializer() {
            this(null);
        }

        public ContractParameterSerializer(Class<ContractParameter> vc) {
            super(vc);
        }

        @Override
        public void serialize(ContractParameter value, JsonGenerator gen,
                SerializerProvider provider) throws IOException {

            gen.writeStartObject();
            serializeParameter(value, gen);
            gen.writeEndObject();
        }

        private void serializeParameter(ContractParameter p, JsonGenerator gen) throws IOException {
            if (p.getParamName() != null) {
                gen.writeStringField("name", p.getParamName());
            }
            if (p.getParamType() != null) {
                gen.writeStringField("type", p.getParamType().jsonValue());
            }
            if (p.getValue() != null) {
                serializeValue(p, gen);
            }
        }

        private void serializeValue(ContractParameter p, JsonGenerator gen) throws IOException {
            switch (p.getParamType()) {
                case SIGNATURE:
                case PUBLIC_KEY:
                    // Here we expect a simple byte array which is converted to a hex string. The
                    // byte order is not changed.
                    gen.writeStringField("value",
                            toHexStringNoPrefix((byte[]) p.getValue()));
                    break;
                case BYTE_ARRAY:
                    gen.writeStringField("value", Base64.encode((byte[]) p.getValue()));
                    break;
                case BOOLEAN:
                    // Convert to true or false without quotes
                    gen.writeBooleanField("value", (boolean) p.getValue());
                    break;
                case INTEGER:
                    // Convert to a string, i.e. in the final json the number has quotes around it.
                case HASH256:
                case HASH160:
                    // In case of a hash, the toString() method returns a big-endian hex string.
                case INTEROP_INTERFACE:
                    // We assume that the interop interface parameter holds a plain string.
                case STRING:
                    gen.writeStringField("value", p.getValue().toString());
                    break;
                case ARRAY:
                    gen.writeArrayFieldStart("value");
                    for (final ContractParameter param : (ContractParameter[]) p.getValue()) {
                        gen.writeStartObject();
                        serializeParameter(param, gen);
                        gen.writeEndObject();
                    }
                    gen.writeEndArray();
                    break;
                case MAP:
                    gen.writeArrayFieldStart("value");
                    HashMap<ContractParameter, ContractParameter> map =
                            (HashMap<ContractParameter, ContractParameter>) p.getValue();
                    for (final ContractParameter key : map.keySet()) {
                        gen.writeStartObject();

                        gen.writeFieldName("key");
                        gen.writeStartObject();
                        serializeParameter(key, gen);
                        gen.writeEndObject();

                        gen.writeFieldName("value");
                        gen.writeStartObject();
                        serializeParameter(map.get(key), gen);
                        gen.writeEndObject();

                        gen.writeEndObject();
                    }
                    gen.writeEndArray();
                    break;
                default:
                    throw new UnsupportedOperationException("Parameter type '" +
                            p.getParamType().toString() + "' not supported.");
            }
        }

    }

}
