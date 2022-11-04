package io.neow3j.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.neow3j.constants.NeoConstants;
import io.neow3j.crypto.Base64;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.Sign;
import io.neow3j.types.ContractParameter.ContractParameterSerializer;
import io.neow3j.wallet.Account;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.neow3j.types.ContractParameterType.ARRAY;
import static io.neow3j.types.ContractParameterType.BYTE_ARRAY;
import static io.neow3j.types.ContractParameterType.HASH160;
import static io.neow3j.types.ContractParameterType.HASH256;
import static io.neow3j.types.ContractParameterType.INTEGER;
import static io.neow3j.types.ContractParameterType.MAP;
import static io.neow3j.types.ContractParameterType.PUBLIC_KEY;
import static io.neow3j.types.ContractParameterType.SIGNATURE;
import static io.neow3j.utils.Numeric.hexStringToByteArray;
import static io.neow3j.utils.Numeric.isValidHexString;
import static io.neow3j.utils.Numeric.toHexStringNoPrefix;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Contract parameters represent an input parameter for contract invocations.
 */
@JsonSerialize(using = ContractParameterSerializer.class)
@JsonDeserialize(using = ContractParameter.ContractParameterDeserializer.class)
@SuppressWarnings("unchecked")
public class ContractParameter {

    @JsonProperty("name")
    private String name;

    @JsonProperty("type")
    private ContractParameterType type;

    @JsonProperty("value")
    protected Object value;

    private ContractParameter() {
    }

    protected ContractParameter(String name, ContractParameterType type, Object value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public ContractParameter(String name, ContractParameterType type) {
        this(name, type, null);
    }

    public ContractParameter(ContractParameterType type, Object value) {
        this(null, type, value);
    }

    public ContractParameter(ContractParameterType type) {
        this(null, type, null);
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
     * The method will try to map the given objects to the correct {@link ContractParameterType}s. You can pass in
     * objects of type {@link ContractParameter} to fix the parameter type of an element.
     * <p>
     * Use {@code array()} without a parameter if you need an empty array.
     *
     * @param entries the array entries.
     * @return the contract parameter.
     */
    public static ContractParameter array(Object... entries) {
        ContractParameter[] params;
        if (entries == null || entries.length == 0) {
            params = new ContractParameter[0];
        } else {
            params = Arrays.stream(entries)
                    .map(ContractParameter::mapToContractParameter)
                    .toArray(ContractParameter[]::new);
        }
        return new ContractParameter(ARRAY, params);
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
     *     Map map = new HashMap{@literal <>}();
     *     map.put("one", "first");
     *     map.put("two", 2);
     *     ContractParameter param = map(map);
     * </pre>
     * The second example leads to the same result but uses {@code ContractParameter} before adding elements.
     * <pre>
     *     Map map = new HashMap{@literal <>}();
     *     map.put(ContractParameter.string("one"), ContractParameter.string("first"));
     *     map.put(ContractParameter.integer("two"), ContractParameter.integer(2));
     *     ContractParameter param = map(map);
     * </pre>
     *
     * @param map the map entries.
     * @return the contract parameter.
     */
    public static ContractParameter map(Map<?, ?> map) {
        if (map.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one map entry is required to create a map contract parameter.");
        }

        // Use a linked hash map to keep the ordering of the argument map.
        Map<ContractParameter, ContractParameter> paramMap = new LinkedHashMap<>();
        map.forEach((k, v) -> {
            ContractParameter key = mapToContractParameter(k);
            ContractParameter value = mapToContractParameter(v);
            if (key.getType().equals(ARRAY) || key.getType().equals(MAP)) {
                throw new IllegalArgumentException(
                        "The provided map contains an invalid key. The keys cannot be of type array or map.");
            }
            paramMap.put(key, value);
        });
        return new ContractParameter(ContractParameterType.MAP, paramMap);
    }

    /**
     * Maps the given object to a contract parameter of the appropriate type.
     *
     * @param o the object to map.
     * @return the parameter.
     * @throws IllegalArgumentException if no suitable parameter type is known for the object.
     */
    public static ContractParameter mapToContractParameter(Object o) {
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
        } else if (o instanceof Byte) {
            return integer((byte) o);
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
        } else if (o instanceof ECKeyPair.ECPublicKey) {
            return publicKey((ECKeyPair.ECPublicKey) o);
        } else if (o instanceof Sign.SignatureData) {
            return signature((Sign.SignatureData) o);
        } else if (o instanceof List) {
            return array((List<?>) o);
        } else if (o instanceof Map) {
            return map((Map<?, ?>) o);
        } else if (o == null) {
            return any(null);
        } else {
            throw new IllegalArgumentException(
                    "The provided object could not be casted into a supported contract parameter type.");
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
        return new ContractParameter(BYTE_ARRAY, byteArray);
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
     * Create a byte array parameter from a string by converting the string to bytes using the UTF-8 character set.
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
     * Creates a signature parameter from the provided {@link io.neow3j.crypto.Sign.SignatureData}.
     *
     * @param signatureData the signature data.
     * @return the contract parameter.
     */
    public static ContractParameter signature(Sign.SignatureData signatureData) {
        return signature(signatureData.getConcatenated());
    }

    /**
     * Creates a signature parameter from the given signature.
     *
     * @param signature a signature.
     * @return the contract parameter.
     */
    public static ContractParameter signature(byte[] signature) {
        if (signature.length != NeoConstants.SIGNATURE_SIZE) {
            throw new IllegalArgumentException(format("Signature is expected to have a length of %s bytes, but had %s.",
                    NeoConstants.SIGNATURE_SIZE, signature.length));
        }
        return new ContractParameter(SIGNATURE, signature);
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
     * Creates an integer parameter from the given byte value.
     *
     * @param byteValue a byte value.
     * @return the contract parameter.
     */
    public static ContractParameter integer(byte byteValue) {
        return integer(BigInteger.valueOf(byteValue));
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
        return new ContractParameter(HASH160, hash);
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
            throw new IllegalArgumentException(
                    format("A Hash256 parameter must be 32 bytes but was %s bytes.", hash.length));
        }
        return hash256(new Hash256(hash));
    }

    /**
     * Creates a public key parameter from the given public key.
     *
     * @param publicKey the public key.
     * @return the contract parameter.
     */
    public static ContractParameter publicKey(ECKeyPair.ECPublicKey publicKey) {
        return publicKey(publicKey.getEncoded(true));
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
        if (publicKey.length != NeoConstants.PUBLIC_KEY_SIZE_COMPRESSED) {
            throw new IllegalArgumentException(
                    "Public key argument must be " + NeoConstants.PUBLIC_KEY_SIZE_COMPRESSED +
                            " bytes but was " + publicKey.length + " bytes.");
        }
        return new ContractParameter(PUBLIC_KEY, publicKey);
    }

    public String getName() {
        return name;
    }

    public ContractParameterType getType() {
        return type;
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
        if (type == that.type && Objects.equals(name, that.name)) {
            if (type.equals(BYTE_ARRAY) || type.equals(SIGNATURE) || type.equals(PUBLIC_KEY)) {
                return Arrays.equals((byte[]) value, (byte[]) that.value);
            } else if (type.equals(HASH160) || type.equals(HASH256)) {
                return Objects.equals(getValue(), that.getValue());
            } else if (type.equals(ARRAY)) {
                ContractParameter[] thatValue = (ContractParameter[]) that.getValue();
                ContractParameter[] oValue = (ContractParameter[]) ((ContractParameter) o).getValue();
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
        return Objects.hash(name, type, value);
    }

    protected static class ContractParameterSerializer extends StdSerializer<ContractParameter> {

        public ContractParameterSerializer() {
            this(null);
        }

        public ContractParameterSerializer(Class<ContractParameter> vc) {
            super(vc);
        }

        @Override
        public void serialize(ContractParameter value, JsonGenerator gen, SerializerProvider provider)
                throws IOException {
            gen.writeStartObject();
            serializeParameter(value, gen);
            gen.writeEndObject();
        }

        private void serializeParameter(ContractParameter p, JsonGenerator gen) throws IOException {
            if (p.getName() != null) {
                gen.writeStringField("name", p.getName());
            }
            if (p.getType() != null) {
                gen.writeStringField("type", p.getType().jsonValue());
            }
            if (p.getValue() != null) {
                serializeValue(p, gen);
            }
        }

        private void serializeValue(ContractParameter p, JsonGenerator gen) throws IOException {
            switch (p.getType()) {
                case PUBLIC_KEY:
                    // Here we expect a simple byte array which is converted to a hex string. The byte order is not
                    // changed.
                    gen.writeStringField("value", toHexStringNoPrefix((byte[]) p.getValue()));
                    break;
                case BYTE_ARRAY:
                case SIGNATURE:
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
                    throw new UnsupportedOperationException(
                            format("Parameter type '%s' not supported.", p.getType().toString()));
            }
        }

    }

    protected static class ContractParameterDeserializer extends StdDeserializer<ContractParameter> {

        public ContractParameterDeserializer() {
            this(null);
        }

        public ContractParameterDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public ContractParameter deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
            Map<String, Object> param = p.getCodec().readValue(p, Map.class);
            return deserializeParam(param);
        }

        private ContractParameter deserializeParam(Map<String, Object> param) throws IOException {
            String name = (String) param.get("name");
            String typeString = (String) param.get("type");
            ContractParameterType type = null;
            if (typeString != null) {
                type = ContractParameterType.fromJsonValue(typeString);
            }
            Object value = null;
            if (param.containsKey("value")) {
                value = deserializeValue(param.get("value"), type);
            }
            return new ContractParameter(name, type, value);
        }

        private Object deserializeValue(Object value, ContractParameterType type) throws IOException {
            switch (type) {
                case PUBLIC_KEY:
                    // Here we expect a simple byte array which is converted to a hex string. The byte order is not
                    // changed.
                    return hexStringToByteArray((String) value);
                case BYTE_ARRAY:
                case SIGNATURE:
                    return Base64.decode((String) value);
                case BOOLEAN:
                    // Convert to true or false without quotes
                    if (value instanceof String) {
                        return Boolean.valueOf((String) value);
                    } else {
                        return value;
                    }
                case INTEGER:
                    if (value instanceof String) {
                        return new BigInteger((String) value);
                    } else {
                        return BigInteger.valueOf((int) value);
                    }
                case HASH256:
                    return new Hash256((String) value);
                case HASH160:
                    return new Hash160((String) value);
                case INTEROP_INTERFACE:
                    // We assume that the interop interface parameter holds a plain string.
                case STRING:
                    return (String) value;
                case ARRAY:
                    List<Map<String, Object>> array = (List<Map<String, Object>>) value;
                    ContractParameter[] params = new ContractParameter[array.size()];
                    for (int i = 0; i < params.length; i++) {
                        Map<String, Object> param = array.get(i);
                        params[i] = deserializeParam(param);
                    }
                    return params;
                case MAP:
                    List<Map<String, Object>> mapArray = (List<Map<String, Object>>) value;
                    Map<ContractParameter, ContractParameter> map = new HashMap<>();
                    for (Map<String, Object> keyValuePair : mapArray) {
                        ContractParameter key = deserializeParam((Map<String, Object>) keyValuePair.get("key"));
                        ContractParameter val = deserializeParam((Map<String, Object>) keyValuePair.get("value"));
                        map.put(key, val);
                    }
                    return map;
                case ANY:
                    return null;
                default:
                    throw new UnsupportedOperationException("Parameter type '" + type + "' not supported.");
            }
        }
    }

}
