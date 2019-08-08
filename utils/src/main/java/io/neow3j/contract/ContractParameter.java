package io.neow3j.contract;

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
import io.neow3j.model.types.ContractParameterType;
import io.neow3j.utils.Keys;
import io.neow3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Contract parameters are used for example in contract invocations and represent an input parameter.
 * But they can also represent an output type and value.
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

    protected ContractParameter() {
    }

    protected ContractParameter(String name, ContractParameterType paramType, Object value) {
        this.paramName = name;
        this.paramType = paramType;
        this.value = value;
    }

    protected ContractParameter(String name, ContractParameterType paramType) {
        this(name, paramType, null);
    }

    private ContractParameter(ContractParameterType paramType, Object value) {
        this(null, paramType, value);
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
     * <p>Creates a byte array parameter from the given value.</p>
     * <br>
     * <p>Make sure that the array is already in the right order. E.g. Fixed8 numbers need to be in
     * little-endian order. It will be sent in the order provided.</p>
     *
     * @param byteArray The parameter value.
     * @return the contract parameter.
     */
    public static ContractParameter byteArray(byte[] byteArray) {
        return new ContractParameter(ContractParameterType.BYTE_ARRAY, byteArray);
    }

    /**
     * <p>Creates a byte array parameter from the given hex string.</p>
     * <br>
     * <p>Make sure that the value is already in the right order. E.g. Fixed8 numbers need to be in
     * little-endian order. It will be sent in the order provided.</p>
     *
     * @param value The value as a string.
     * @return the contract parameter.
     */
    public static ContractParameter byteArray(String value) {
        if (!Numeric.isValidHexString(value)) {
            throw new IllegalArgumentException("Argument is not a valid hex number");
        }
        return byteArray(Numeric.hexStringToByteArray(value));
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
            throw new IllegalArgumentException("Argument is not a valid address.");
        }
        return byteArray(ScriptHash.fromAddress(address).toArray());
    }

    /**
     * Creates a byte array parameter from the given number, transforming it to the Fixed8 number
     * format in little-endian order.
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
            throw new IllegalArgumentException("Argument is not a valid hex number");
        }
        return signature(Numeric.hexStringToByteArray(signatureHexString));
    }


    /**
     * Creates a signature parameter from the given signature.
     *
     * @param signature A signature.
     * @return the contract parameter.
     */
    public static ContractParameter signature(byte[] signature) {
        if (signature.length != NeoConstants.SIGNATURE_SIZE_BYTES) {
            throw new IllegalArgumentException("Signature is expected to have a length of " +
                    NeoConstants.SIGNATURE_SIZE_BYTES + " bytes, but had " +
                    signature.length + ".");
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
     * Creates a hash160 parameter from the given hexadecimal string.
     *
     * @param hashHexString a hash160 value as hexadecimal string in big-endian order.
     * @return the contract parameter.
     * @deprecated
     */
    @Deprecated
    public static ContractParameter hash160(String hashHexString) {
        if (!Numeric.isValidHexString(hashHexString)) {
            throw new IllegalArgumentException("Argument is not a valid hex number");
        }
        return hash160(new ScriptHash(hashHexString));
    }

    /**
     * Creates a hash160 parameter from the given hash.
     *
     * @param hash a hash160 value in little-endian order.
     * @return the contract parameter.
     * @deprecated
     */
    @Deprecated
    public static ContractParameter hash160(byte[] hash) {
        return hash160(new ScriptHash(hash));
    }

    /**
     * Creates a hash160 parameter from the given script hash.
     *
     * @param hash a script hash
     * @return the contract parameter.
     */
    public static ContractParameter hash160(ScriptHash hash) {
        if (hash.length() != NeoConstants.SCRIPTHASH_LENGHT_BYTES) {
            throw new IllegalArgumentException("A Hash160 parameter expects a value of length " +
                    NeoConstants.SCRIPTHASH_LENGHT_BYTES);
        }
        return new ContractParameter(ContractParameterType.HASH160, hash);
    }

    /**
     * Creates a hash256 parameter from the given hexadecimal string.
     *
     * @param hashHexString a hash256 value as hexadecimal string in big-endian order.
     * @return the contract parameter.
     * @deprecated
     */
    @Deprecated
    public static ContractParameter hash256(String hashHexString) {
        if (!Numeric.isValidHexString(hashHexString)) {
            throw new IllegalArgumentException("Argument is not a valid hex number");
        }
        return hash256(new ScriptHash(hashHexString));
    }

    /**
     * Creates a hash256 parameter from the given hash.
     *
     * @param hash a hash256 value in little-endian order.
     * @return the contract parameter.
     * @deprecated
     */
    @Deprecated
    public static ContractParameter hash256(byte[] hash) {
        return hash256(new ScriptHash(hash));
    }

    /**
     * Creates a hash256 parameter from the given script hash.
     *
     * @param hash a script hash
     * @return the contract parameter.
     */
    public static ContractParameter hash256(ScriptHash hash) {
        if (hash.length() != NeoConstants.ASSET_ID_LENGHT_BYTES) {
            throw new IllegalArgumentException("A Hash256 parameter expects a value of length " +
                    NeoConstants.ASSET_ID_LENGHT_BYTES);
        }
        return new ContractParameter(ContractParameterType.HASH256, hash);
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

        if (paramType == that.paramType &&
                Objects.equals(paramName, that.paramName)) {

            if (paramType.equals(ContractParameterType.BYTE_ARRAY) ||
                    paramType.equals(ContractParameterType.SIGNATURE) ||
                    paramType.equals(ContractParameterType.HASH160) ||
                    paramType.equals(ContractParameterType.HASH256)) {

                return Arrays.equals((byte[]) value, (byte[]) that.value);
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

    @Override
    public String toString() {
        return "ContractParameter{" +
                "paramName='" + paramName + '\'' +
                ", paramType=" + paramType +
                ", value=" + value +
                '}';
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
                case BYTE_ARRAY:
                case SIGNATURE:
                    // Byte array and signature values are byte arrays. It is simply converted to a
                    // hex string. The byte order is not changed. It already has to be correct.
                    gen.writeStringField("value", Numeric.toHexStringNoPrefix((byte[]) p.getValue()));
                    break;
                case BOOLEAN:
                    // Convert to true or false without quotes
                    gen.writeBooleanField("value", (boolean) p.getValue());
                    break;
                case INTEGER:
                    // Convert to a string, i.e. in the final json the number has quotes around it.
                case HASH160:
                case HASH256:
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
                case PUBLIC_KEY:
                    // TODO 30.07.19 claude: Implement public key serialization
                default:
                    throw new UnsupportedOperationException("Parameter type \'" +
                            p.getParamType().toString() + "\' not supported.");
            }
        }

    }

    protected static class ContractParameterDeserializer
            extends ParameterDeserializer<ContractParameter> {

        @Override
        public ContractParameter newInstance(String name, ContractParameterType type, Object value) {
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
                case BYTE_ARRAY:
                case SIGNATURE:
                    // For byte array and signature the data is expected to be a hex string in the
                    // correct ordering. E.g. little-endian for Fixed8 numbers.
                    return Numeric.hexStringToByteArray(value.asText());
                case STRING:
                    return value.asText();
                case BOOLEAN:
                    return value.asBoolean();
                case INTEGER:
                    return new BigInteger(value.asText());
                case HASH160:
                case HASH256:
                    // The script hash value is expected to be a big-endian hex string.
                    return new ScriptHash(value.asText());
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
                case PUBLIC_KEY:
                    // TODO 30.07.19 claude: Implement public key deserialization
                default:
                    throw new UnsupportedOperationException("Parameter type \'" + type +
                            "\' not supported.");
            }
        }
    }

}
