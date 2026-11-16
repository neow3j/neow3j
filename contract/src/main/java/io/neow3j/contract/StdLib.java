package io.neow3j.contract;

import io.neow3j.contract.exceptions.UnexpectedReturnTypeException;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.protocol.exceptions.InvocationFaultStateException;
import io.neow3j.types.ContractParameter;
import io.neow3j.types.Hash160;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static io.neow3j.types.ContractParameter.bool;
import static io.neow3j.types.ContractParameter.byteArray;
import static io.neow3j.types.ContractParameter.integer;
import static io.neow3j.types.ContractParameter.mapToContractParameter;
import static io.neow3j.types.ContractParameter.string;
import static io.neow3j.types.StackItemType.ARRAY;
import static io.neow3j.types.StackItemType.BYTE_STRING;

/**
 * Represents the native StdLib contract and provides methods to invoke its functions.
 * <p>
 * Methods execute read-only invocations against the configured Neo node. They throw {@link IOException} when node
 * communication fails and {@link InvocationFaultStateException} when the native contract invocation faults.
 * Methods that convert return values can also throw {@link UnexpectedReturnTypeException} if the node returns a
 * stack item with an unexpected type or a value that cannot be converted.
 */
public class StdLib extends SmartContract {

    /**
     * The native contract name.
     */
    public static final String NAME = "StdLib";

    /**
     * The native contract script hash.
     */
    public static final Hash160 SCRIPT_HASH = calcNativeContractHash(NAME);

    /**
     * The maximum byte length accepted by StdLib methods that have an input length limit.
     */
    public static final int MAX_INPUT_LENGTH = 1024;

    /**
     * Creates a StdLib wrapper that uses the given Neo node for invocations.
     *
     * @param neow3j the Neo node interface to use.
     * @throws IllegalArgumentException if {@code neow3j} is null.
     */
    public StdLib(Neow3j neow3j) {
        super(SCRIPT_HASH, neow3j);
    }

    /**
     * Serializes a supported Java value using the native StdLib contract. Supported values are those accepted by
     * {@link ContractParameter#mapToContractParameter(Object)}. The returned bytes are the native serialized stream
     * and are not byte-reversed.
     *
     * @param source the value to serialize.
     * @return the serialized bytes.
     * @throws IOException              if there was a problem communicating with the Neo node.
     * @throws IllegalArgumentException if the value cannot be mapped to a contract parameter.
     */
    public byte[] serialize(Object source) throws IOException {
        ContractParameter parameter = mapToContractParameter(source);
        return callFunctionReturningByteArray("serialize", parameter);
    }

    /**
     * Deserializes data with the native StdLib contract and returns the resulting VM value as a stack item.
     *
     * @param source the serialized bytes, in their original byte order.
     * @return the deserialized stack item.
     * @throws IOException if there was a problem communicating with the Neo node.
     */
    public StackItem deserialize(byte[] source) throws IOException {
        return callFunctionReturningStackItem("deserialize", byteArray(source));
    }

    /**
     * Serializes a supported Java value to a JSON string using the native StdLib contract.
     *
     * @param obj the value to serialize.
     * @return the JSON string.
     * @throws IOException              if there was a problem communicating with the Neo node.
     * @throws IllegalArgumentException if the value cannot be mapped to a contract parameter.
     */
    public String jsonSerialize(Object obj) throws IOException {
        ContractParameter parameter = mapToContractParameter(obj);
        return callFunctionReturningString("jsonSerialize", parameter);
    }

    /**
     * Deserializes a JSON string with the native StdLib contract.
     *
     * @param json the JSON string to deserialize.
     * @return the deserialized stack item.
     * @throws IOException if there was a problem communicating with the Neo node.
     */
    public StackItem jsonDeserialize(String json) throws IOException {
        return callFunctionReturningStackItem("jsonDeserialize", string(json));
    }

    /**
     * Encodes bytes as Base64 using the native StdLib contract.
     *
     * @param input the bytes to encode.
     * @return the Base64 string.
     * @throws IOException if there was a problem communicating with the Neo node.
     */
    public String base64Encode(byte[] input) throws IOException {
        return callFunctionReturningString("base64Encode", byteArray(input));
    }

    /**
     * Decodes a Base64 string using the native StdLib contract.
     *
     * @param input the Base64 string to decode.
     * @return the decoded bytes.
     * @throws IOException if there was a problem communicating with the Neo node.
     */
    public byte[] base64Decode(String input) throws IOException {
        return callFunctionReturningByteArray("base64Decode", string(input));
    }

    /**
     * Encodes a string as Base64URL using the native StdLib contract.
     *
     * @param data the string to encode.
     * @return the Base64URL string.
     * @throws IOException if there was a problem communicating with the Neo node.
     */
    public String base64UrlEncode(String data) throws IOException {
        return callFunctionReturningString("base64UrlEncode", string(data));
    }

    /**
     * Decodes a Base64URL string using the native StdLib contract.
     *
     * @param input the Base64URL string to decode.
     * @return the decoded string.
     * @throws IOException if there was a problem communicating with the Neo node.
     */
    public String base64UrlDecode(String input) throws IOException {
        return callFunctionReturningString("base64UrlDecode", string(input));
    }

    /**
     * Encodes bytes as Base58 using the native StdLib contract.
     *
     * @param input the bytes to encode.
     * @return the Base58 string.
     * @throws IOException if there was a problem communicating with the Neo node.
     */
    public String base58Encode(byte[] input) throws IOException {
        return callFunctionReturningString("base58Encode", byteArray(input));
    }

    /**
     * Decodes a Base58 string using the native StdLib contract.
     *
     * @param input the Base58 string to decode.
     * @return the decoded bytes.
     * @throws IOException if there was a problem communicating with the Neo node.
     */
    public byte[] base58Decode(String input) throws IOException {
        return callFunctionReturningByteArray("base58Decode", string(input));
    }

    /**
     * Encodes bytes as a checksummed Base58 string using the native StdLib contract.
     *
     * @param input the bytes to encode.
     * @return the checksummed Base58 string.
     * @throws IOException if there was a problem communicating with the Neo node.
     */
    public String base58CheckEncode(byte[] input) throws IOException {
        return callFunctionReturningString("base58CheckEncode", byteArray(input));
    }

    /**
     * Decodes a checksummed Base58 string using the native StdLib contract.
     *
     * @param input the checksummed Base58 string to decode.
     * @return the decoded bytes.
     * @throws IOException if there was a problem communicating with the Neo node.
     */
    public byte[] base58CheckDecode(String input) throws IOException {
        return callFunctionReturningByteArray("base58CheckDecode", string(input));
    }

    /**
     * Encodes bytes as a hexadecimal string using the native StdLib contract.
     *
     * @param input the bytes to encode.
     * @return the hexadecimal string.
     * @throws IOException if there was a problem communicating with the Neo node.
     */
    public String hexEncode(byte[] input) throws IOException {
        return callFunctionReturningString("hexEncode", byteArray(input));
    }

    /**
     * Decodes a hexadecimal string using the native StdLib contract.
     *
     * @param input the hexadecimal string to decode.
     * @return the decoded bytes.
     * @throws IOException if there was a problem communicating with the Neo node.
     */
    public byte[] hexDecode(String input) throws IOException {
        return callFunctionReturningByteArray("hexDecode", string(input));
    }

    /**
     * Converts an integer to a string in the given base using the native StdLib contract.
     *
     * @param i    the integer to convert.
     * @param base the base to use, either 10 or 16.
     * @return the integer string.
     * @throws IOException if there was a problem communicating with the Neo node.
     */
    public String itoa(BigInteger i, int base) throws IOException {
        return callFunctionReturningString("itoa", integer(i), integer(base));
    }

    /**
     * Converts an integer to its decimal string representation using the native StdLib contract.
     *
     * @param i the integer to convert.
     * @return the decimal integer string.
     * @throws IOException if there was a problem communicating with the Neo node.
     */
    public String itoa(BigInteger i) throws IOException {
        return callFunctionReturningString("itoa", integer(i));
    }

    /**
     * Converts a string in the given base to an integer using the native StdLib contract.
     *
     * @param s    the integer string to convert.
     * @param base the base to use, either 10 or 16.
     * @return the converted integer.
     * @throws IOException if there was a problem communicating with the Neo node.
     */
    public BigInteger atoi(String s, int base) throws IOException {
        return callFunctionReturningInt("atoi", string(s), integer(base));
    }

    /**
     * Converts a decimal integer string to an integer using the native StdLib contract.
     *
     * @param s the decimal integer string to convert.
     * @return the converted integer.
     * @throws IOException if there was a problem communicating with the Neo node.
     */
    public BigInteger atoi(String s) throws IOException {
        return callFunctionReturningInt("atoi", string(s));
    }

    /**
     * Compares two byte arrays using the native StdLib contract.
     *
     * @param mem1 the first byte array.
     * @param mem2 the second byte array.
     * @return -1 if {@code mem1} precedes {@code mem2}, 0 if they are equal, or 1 if {@code mem1} follows {@code mem2}.
     * @throws IOException if there was a problem communicating with the Neo node.
     */
    public int memoryCompare(byte[] mem1, byte[] mem2) throws IOException {
        return callFunctionReturningInt("memoryCompare", byteArray(mem1), byteArray(mem2)).intValueExact();
    }

    /**
     * Searches for the first occurrence of {@code value} in {@code mem} using the native StdLib contract.
     *
     * @param mem   the byte array to search.
     * @param value the bytes to find.
     * @return the index of the first occurrence, or -1 if not found.
     * @throws IOException if there was a problem communicating with the Neo node.
     */
    public int memorySearch(byte[] mem, byte[] value) throws IOException {
        return callFunctionReturningInt("memorySearch", byteArray(mem), byteArray(value)).intValueExact();
    }

    /**
     * Searches for the first occurrence of {@code value} in {@code mem}, starting at {@code start}.
     *
     * @param mem   the byte array to search.
     * @param value the bytes to find.
     * @param start the index at which to start searching.
     * @return the index of the first occurrence, or -1 if not found.
     * @throws IOException if there was a problem communicating with the Neo node.
     */
    public int memorySearch(byte[] mem, byte[] value, int start) throws IOException {
        return callFunctionReturningInt("memorySearch", byteArray(mem), byteArray(value),
                integer(start)).intValueExact();
    }

    /**
     * Searches for {@code value} in {@code mem}, starting at {@code start}, using the native StdLib contract.
     *
     * @param mem      the byte array to search.
     * @param value    the bytes to find.
     * @param start    the index at which to start searching; when searching backwards, the upper bound is exclusive.
     * @param backward whether to search backwards from {@code start}.
     * @return the matching index, or -1 if not found.
     * @throws IOException if there was a problem communicating with the Neo node.
     */
    public int memorySearch(byte[] mem, byte[] value, int start, boolean backward) throws IOException {
        return callFunctionReturningInt("memorySearch", byteArray(mem), byteArray(value), integer(start),
                bool(backward)).intValueExact();
    }

    /**
     * Splits a string at each occurrence of {@code separator} using the native StdLib contract.
     *
     * @param str       the string to split.
     * @param separator the separator string.
     * @return the resulting strings, including empty entries where separators are consecutive.
     * @throws IOException if there was a problem communicating with the Neo node.
     */
    public List<String> stringSplit(String str, String separator) throws IOException {
        return callFunctionReturningStringList("stringSplit", string(str), string(separator));
    }

    /**
     * Splits a string at each occurrence of {@code separator} using the native StdLib contract.
     *
     * @param str                the string to split.
     * @param separator          the separator string.
     * @param removeEmptyEntries whether empty entries should be removed.
     * @return the resulting strings.
     * @throws IOException if there was a problem communicating with the Neo node.
     */
    public List<String> stringSplit(String str, String separator, boolean removeEmptyEntries) throws IOException {
        return callFunctionReturningStringList("stringSplit", string(str), string(separator), bool(removeEmptyEntries));
    }

    private List<String> callFunctionReturningStringList(String function, ContractParameter... params)
            throws IOException {
        StackItem item = callFunctionReturningStackItem(function, params);
        List<StackItem> elements = convertReturnValue(item, StackItem::getList, ARRAY);
        List<String> result = new ArrayList<>();
        for (StackItem element : elements) {
            result.add(convertReturnValue(element, StackItem::getString, BYTE_STRING));
        }
        return result;
    }

    /**
     * Returns the native StdLib's character count for a string.
     *
     * @param str the string to measure.
     * @return the number of characters according to the node's active Neo hardfork rules.
     * @throws IOException if there was a problem communicating with the Neo node.
     */
    public int strLen(String str) throws IOException {
        return callFunctionReturningInt("strLen", string(str)).intValueExact();
    }

}
