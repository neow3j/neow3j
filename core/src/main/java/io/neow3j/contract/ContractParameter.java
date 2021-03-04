package io.neow3j.contract;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.neow3j.constants.NeoConstants;
import io.neow3j.contract.ContractParameter.ContractParameterDeserializer;
import io.neow3j.contract.ContractParameter.ContractParameterSerializer;
import io.neow3j.crypto.Base64;
import io.neow3j.model.types.ContractParameterType;
import io.neow3j.utils.Numeric;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Contract parameters are used for example in contract invocations and represent an input
 * parameter. They can also represent an output type and value.
 */
@JsonSerialize(using = ContractParameterSerializer.class)
@JsonDeserialize(using = ContractParameterDeserializer.class)
public class ContractParameter {

    @JsonProperty("name")
    private String paramName;

    @JsonProperty("type")
    private ContractParameterType paramType;

    @JsonProperty("value")
    protected Object value;

    private ContractParameter() {
    }

    public ContractParameter(String name, ContractParameterType paramType, Object value) {
        this.paramName = name;
        this.paramType = paramType;
        this.value = value;
    }

    ContractParameter(String name, ContractParameterType paramType) {
        this(name, paramType, null);
    }

    private ContractParameter(ContractParameterType paramType, Object value) {
        this(null, paramType, value);
    }

    public static ContractParameter any(Object value) {
        return new ContractParameter(ContractParameterType.ANY, value);
    }

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
     * @param value the array entries.
     * @return the contract parameter.
     */
    public static ContractParameter array(Object... value) {
        List<ContractParameter> params = new ArrayList<>();
        Arrays.stream(value).forEach(o -> {
            if (o instanceof ContractParameter) {
                params.add((ContractParameter) o);
            } else if (o instanceof Boolean) {
                params.add(bool((Boolean) o));
            } else if (o instanceof Integer) {
                params.add(integer((Integer) o));
            } else if (o instanceof byte[]) {
                params.add(byteArray((byte[]) o));
            } else if (o instanceof String) {
                params.add(string((String) o));
            } else {
                throw new IllegalArgumentException("The provided object could not be casted into " +
                        "a supported contract parameter type.");
            }
        });
        return array(params);
    }

    /**
     * Creates an array parameter from the given values.
     *
     * @param params the array entries.
     * @return the contract parameter.
     */
    public static ContractParameter array(List<ContractParameter> params) {
        return array(params.toArray(new ContractParameter[0]));
    }

    /**
     * Creates an array parameter from the given values.
     *
     * @param params the array entries.
     * @return the contract parameter.
     */
    public static ContractParameter array(ContractParameter... params) {
        if (params.length == 0) {
            throw new IllegalArgumentException("At least one parameter is required to create an " +
                    "array contract parameter.");
        }
        boolean anyNull = Arrays.stream(params).anyMatch(Objects::isNull);
        if (anyNull) {
            throw new IllegalArgumentException("Cannot add a null object to an array contract " +
                    "parameter.");
        }
        return new ContractParameter(ContractParameterType.ARRAY, params);
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
        if (!Numeric.isValidHexString(hexString)) {
            throw new IllegalArgumentException("Argument is not a valid hex number");
        }
        return byteArray(Numeric.hexStringToByteArray(hexString));
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
        if (!Numeric.isValidHexString(signatureHexString)) {
            throw new IllegalArgumentException("Argument is not a valid hex number");
        }
        return signature(Numeric.hexStringToByteArray(signatureHexString));
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
        return new ContractParameter(ContractParameterType.INTEGER, integer);
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
     * @param hashHexString a hex string (possibly a 256-bit hash) in little-endian order.
     * @return the contract parameter.
     */
    public static ContractParameter hash256(String hashHexString) {
        if (!Numeric.isValidHexString(hashHexString)) {
            throw new IllegalArgumentException("Argument is not a valid hex number");
        }
        return hash256(Numeric.hexStringToByteArray(hashHexString));
    }

    /**
     * Creates a hash256 parameter from the given bytes.
     *
     * @param hash bytes (possibly a 256-bit hash) in little-endian order.
     * @return the contract parameter.
     */
    public static ContractParameter hash256(byte[] hash) {
        if (hash.length != 32) {
            throw new IllegalArgumentException("A Hash256 parameter must be 32 bytes long but was" +
                    " " + hash.length + " bytes long.");
        }
        return new ContractParameter(ContractParameterType.HASH256, hash);
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
        return publicKey(Numeric.hexStringToByteArray(publicKey));
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
                    NeoConstants.PUBLIC_KEY_SIZE + " long but was " + publicKey.length + " bytes");
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

            serializeParameter(value, gen);
        }

        private void serializeParameter(ContractParameter p, JsonGenerator gen) throws IOException {
            gen.writeStartObject();
            if (p.getParamName() != null) {
                gen.writeStringField("name", p.getParamName());
            }
            if (p.getParamType() != null) {
                gen.writeStringField("type", p.getParamType().jsonValue());
            }
            if (p.getValue() != null) {
                serializeValue(p, gen);
            }
            gen.writeEndObject();
        }

        private void serializeValue(ContractParameter p, JsonGenerator gen) throws IOException {
            switch (p.getParamType()) {
                case SIGNATURE:
                case HASH256:
                case PUBLIC_KEY:
                    // Here we expect a simple byte array which is converted to a hex string. The
                    // byte order is not changed.
                    gen.writeStringField("value",
                            Numeric.toHexStringNoPrefix((byte[]) p.getValue()));
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
                case HASH160:
                    // In case of a script hash the value is of type ScriptHash, of which the
                    // toString() method returns a big-endian hex string of the hash.
                case INTEROP_INTERFACE:
                    // We assume that the interop interface parameter holds a plain string.
                case STRING:
                    gen.writeStringField("value", p.getValue().toString());
                    break;
                case ARRAY:
                    gen.writeArrayFieldStart("value");
                    for (final ContractParameter param : (ContractParameter[]) p.getValue()) {
                        serializeParameter(param, gen);
                    }
                    gen.writeEndArray();
                    break;
                default:
                    throw new UnsupportedOperationException("Parameter type \'" +
                            p.getParamType().toString() + "\' not supported.");
            }
        }

    }

    protected static class ContractParameterDeserializer
            extends ParameterDeserializer<ContractParameter> {

        @Override
        public ContractParameter newInstance(String name, ContractParameterType type,
                Object value) {
            return new ContractParameter(name, type, value);
        }

    }

    protected static abstract class ParameterDeserializer<T extends ContractParameter>
            extends StdDeserializer<T> {

        public ParameterDeserializer() {
            this(null);
        }

        public ParameterDeserializer(Class<T> vc) {
            super(vc);
        }

        public abstract T newInstance(String name, ContractParameterType type, Object value);

        @Override
        public T deserialize(JsonParser jp, DeserializationContext ctxt)
                throws IOException {

            JsonNode node = jp.getCodec().readTree(jp);
            return deserializeParameter(node, jp);
        }

        private T deserializeParameter(JsonNode param, JsonParser jp)
                throws JsonProcessingException {

            JsonNode nameNode = param.get("name");
            String name = null;
            if (nameNode != null) {
                name = nameNode.asText();
            }

            JsonNode typeNode = param.get("type");
            ContractParameterType type = null;
            if (typeNode != null) {
                type = jp.getCodec().treeToValue(typeNode, ContractParameterType.class);
            }

            JsonNode valueNode = param.get("value");
            Object value = null;
            if (valueNode != null) {
                value = deserializeValue(valueNode, type, jp);
            }
            return newInstance(name, type, value);
        }

        private Object deserializeValue(JsonNode value, ContractParameterType type, JsonParser jp)
                throws JsonProcessingException {

            switch (type) {
                case SIGNATURE:
                case HASH256:
                case PUBLIC_KEY:
                    // Expected to be a hexadecimal string.
                    return Numeric.hexStringToByteArray(value.asText());
                case BYTE_ARRAY:
                    // Expected to be a Base64-encoded byte array.
                    return Base64.decode(value.asText());
                case STRING:
                    return value.asText();
                case BOOLEAN:
                    return value.asBoolean();
                case INTEGER:
                    return new BigInteger(value.asText());
                case HASH160:
                    // The script hash value is expected to be a big-endian hex string.
                    return new Hash160(value.asText());
                case ARRAY:
                    if (value.isArray()) {
                        List<ContractParameter> arr = new ArrayList<>(value.size());
                        for (final JsonNode param : value) {
                            arr.add(deserializeParameter(param, jp));
                        }
                        return arr.toArray(new ContractParameter[]{});
                    }
                case INTEROP_INTERFACE:
                    // We assume that the interop interface parameter holds a plain string.
                    return value.asText();
                default:
                    throw new UnsupportedOperationException("Parameter type \'" + type +
                            "\' not supported.");
            }
        }
    }
}
