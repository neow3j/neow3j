package io.neow3j.protocol.core.stackitem;

import io.neow3j.crypto.Base64;
import io.neow3j.protocol.ResponseTester;
import io.neow3j.protocol.exceptions.StackItemCastException;
import io.neow3j.types.StackItemType;
import io.neow3j.utils.Numeric;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.neow3j.protocol.ObjectMapperFactory.getObjectMapper;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StackItemTest extends ResponseTester {

    private final static String BYTESTRING_JSON = "{\"type\":\"ByteString\",\"value\":\"V29vbG9uZw==\"}";

    @Test
    public void throwOnCastingToMapFromIllegalType() throws IOException {
        StackItem rawItem = getObjectMapper().readValue("{\"type\":\"Integer\",\"value\":\"1124\"}",
                StackItem.class);

        StackItemCastException thrown = assertThrows(StackItemCastException.class, rawItem::getMap);
        assertThat(thrown.getMessage(), stringContainsInOrder(asList(StackItemType.INTEGER.getValue(), "1124")));
    }

    @Test
    public void throwOnCastingToListFromIllegalType() throws IOException {
        StackItem rawItem = getObjectMapper().readValue("{\"type\":\"Integer\",\"value\":\"1124\"}",
                StackItem.class);

        StackItemCastException thrown = assertThrows(StackItemCastException.class, rawItem::getList);
        assertThat(thrown.getMessage(), stringContainsInOrder(asList(StackItemType.INTEGER.getValue(), "1124")));
    }

    @Test
    public void throwOnCastingToPointerFromIllegalType() throws IOException {
        StackItem rawItem = getObjectMapper().readValue("{\"type\":\"Integer\",\"value\":\"1124\"}", StackItem.class);

        StackItemCastException thrown = assertThrows(StackItemCastException.class, rawItem::getPointer);
        assertThat(thrown.getMessage(), stringContainsInOrder(asList(StackItemType.INTEGER.getValue(), "1124")));
    }

    @Test
    public void throwOnCastingToInteropInterfaceFromIllegalType() throws IOException {
        StackItem rawItem = getObjectMapper().readValue("{\"type\":\"Integer\",\"value\":\"1124\"}", StackItem.class);

        StackItemCastException thrown = assertThrows(StackItemCastException.class, rawItem::getIteratorId);
        assertThat(thrown.getMessage(), stringContainsInOrder(asList(StackItemType.INTEGER.getValue(), "1124")));
    }

    @Test
    public void throwOnCastingToAddressFromIllegalType() throws IOException {
        StackItem rawItem = getObjectMapper().readValue("{\"type\":\"Integer\",\"value\":\"1124\"}", StackItem.class);

        StackItemCastException thrown = assertThrows(StackItemCastException.class, rawItem::getAddress);
        assertThat(thrown.getMessage(), stringContainsInOrder(asList(StackItemType.INTEGER.getValue(), "1124")));
    }

    @Test
    public void throwOnCastingToBooleanFromIllegalType() throws IOException {
        StackItem rawItem = getObjectMapper().readValue("{\"type\":\"Integer\",\"value\":\"1124\"}",
                StackItem.class);

        StackItemCastException thrown = assertThrows(StackItemCastException.class,
                rawItem::getBoolean);
        assertThat(thrown.getMessage(), stringContainsInOrder(
                asList(StackItemType.INTEGER.getValue(), "1124"))
        );
    }

    @Test
    public void throwOnCastingToHexStringFromIllegalType() throws IOException {
        StackItem rawItem = getObjectMapper().readValue("{\"type\":\"Boolean\",\"value\":\"true\"}",
                StackItem.class);

        StackItemCastException thrown = assertThrows(StackItemCastException.class,
                rawItem::getHexString);
        assertThat(thrown.getMessage(), stringContainsInOrder(
                asList(StackItemType.BOOLEAN.getValue(), "true"))
        );
    }

    @Test
    public void throwOnCastingToStringFromIllegalType() throws IOException {
        StackItem rawItem = getObjectMapper().readValue("{\"type\":\"Pointer\",\"value\":\"1124\"}",
                StackItem.class);

        StackItemCastException thrown = assertThrows(StackItemCastException.class,
                rawItem::getString);
        assertThat(thrown.getMessage(), stringContainsInOrder(
                asList(StackItemType.POINTER.getValue(), "1124"))
        );
    }

    @Test
    public void throwOnCastingToByteArrayFromIllegalType() throws IOException {
        StackItem rawItem = getObjectMapper().readValue("{\"type\":\"Boolean\",\"value\":\"true\"}",
                StackItem.class);

        StackItemCastException thrown = assertThrows(StackItemCastException.class,
                rawItem::getByteArray);
        assertThat(thrown.getMessage(), stringContainsInOrder(
                asList(StackItemType.BOOLEAN.getValue(), "true"))
        );
    }

    @Test
    public void throwOnCastingToIntegerFromIllegalType() throws IOException {
        StackItem item = getObjectMapper().readValue("{\n" +
                "    \"type\": \"Array\",\n" +
                "    \"value\": [\n" +
                "        {\n" +
                "            \"type\": \"Boolean\",\n" +
                "            \"value\": \"false\"\n" +
                "        }\n" +
                "    ]\n" +
                "}", StackItem.class);

        StackItemCastException thrown = assertThrows(StackItemCastException.class,
                item::getInteger);
        assertThat(thrown.getMessage(), stringContainsInOrder(
                asList(StackItemType.ARRAY.getValue(), "false"))
        );
    }

    @Test
    public void throwOnGettingValuefromNullIntegerStackItem() throws IOException {
        StackItem item = getObjectMapper().readValue("{\"type\":\"Integer\"," +
                "\"value\":\"\"}", StackItem.class);

        StackItemCastException thrown = assertThrows(StackItemCastException.class, item::getInteger);
        assertThat(thrown.getMessage(), is("Cannot cast stack item because its value is null"));
    }

    @Test
    public void testDeserializeAnyStackItem() throws IOException {
        String json = "{\"type\":\"Any\", \"value\":null}";
        StackItem item = getObjectMapper().readValue(json, StackItem.class);

        assertThat(item.getType(), is(StackItemType.ANY));
        assertThat(item.getValue(), is(nullValue()));

        AnyStackItem other = new AnyStackItem(null);

        assertEquals(other, item);
        assertEquals(other.hashCode(), item.hashCode());
    }

    @Test
    public void testDeserializePointerStackItem() throws IOException {
        String json = "{\"type\":\"Pointer\", \"value\":\"123456\"}";
        StackItem item = getObjectMapper().readValue(json, StackItem.class);

        assertThat(item.getType(), is(StackItemType.POINTER));
        assertThat(item.getPointer(), is(new BigInteger("123456")));

        PointerStackItem other = new PointerStackItem(new BigInteger("123456"));

        assertEquals(other, item);
        assertEquals(other.hashCode(), item.hashCode());
    }

    @Test
    public void testDeserializeByteStringStackItem() throws IOException {
        StackItem item = getObjectMapper().readValue(BYTESTRING_JSON, StackItem.class);

        assertEquals(StackItemType.BYTE_STRING, item.getType());
        assertThat(item.getHexString(), is("576f6f6c6f6e67"));
        assertThat(item.getByteArray(), is(Numeric.hexStringToByteArray("576f6f6c6f6e67")));
        assertThat(item.getString(), is("Woolong"));

        String json = "{\"type\":\"ByteString\", \"value\":\"aWQ=\"}";

        item = getObjectMapper().readValue(json, StackItem.class);

        assertThat(item.getByteArray(), is(Numeric.hexStringToByteArray("6964")));
        assertThat(item.getInteger(), is(new BigInteger("25705")));

        // The script hash hex string in little-endian format
        json = "{\"type\":\"ByteString\", \"value\":\"1Cz3qTHOPEZVD9kN5IJYP8XqcBo=\"}";
        item = getObjectMapper().readValue(json, StackItem.class);

        assertThat(item.getAddress(), is("NfFrJpFaLPCVuRRPhmBYRmZqSQLJ5fPuhz"));
        assertThat(item.getHexString(), is("d42cf7a931ce3c46550fd90de482583fc5ea701a"));
        assertThat(item.getByteArray(),
                is(Numeric.hexStringToByteArray("d42cf7a931ce3c46550fd90de482583fc5ea701a")));

        ByteStringStackItem other = new ByteStringStackItem(
                Numeric.hexStringToByteArray("d42cf7a931ce3c46550fd90de482583fc5ea701a"));

        assertEquals(other, item);
        assertEquals(other.hashCode(), item.hashCode());
    }

    @Test
    public void testDeserializeBufferStackItem() throws IOException {
        String json = "{\"type\":\"Buffer\", \"value\":\"ew==\"}";
        StackItem item = getObjectMapper().readValue(json, StackItem.class);
        assertThat(item.getInteger(), is(new BigInteger("123")));

        json = "{\"type\":\"Buffer\", \"value\":\"V29vbG9uZw==\"}";
        item = getObjectMapper().readValue(json, StackItem.class);
        assertThat(item.getString(), is("Woolong"));

        json = "{\"type\":\"Buffer\", \"value\":\"1Cz3qTHOPEZVD9kN5IJYP8XqcBo=\"}";
        item = getObjectMapper().readValue(json, StackItem.class);
        assertThat(item.getAddress(), is("NfFrJpFaLPCVuRRPhmBYRmZqSQLJ5fPuhz"));

        json = "{\"type\":\"Buffer\", \"value\":\"V29vbG9uZw==\"}";
        item = getObjectMapper().readValue(json, StackItem.class);
        assertThat(item.getByteArray(), is(Numeric.hexStringToByteArray("576f6f6c6f6e67")));

        BufferStackItem other = new BufferStackItem(Numeric.hexStringToByteArray("576f6f6c6f6e67"));
        assertEquals(other, item);
        assertEquals(other.hashCode(), item.hashCode());
    }

    @Test
    public void testDeserializeIntegerStackItem() throws IOException {
        String json = "{\"type\":\"Integer\",\"value\":\"1124\"}";
        StackItem item = getObjectMapper().readValue(json, StackItem.class);
        assertEquals(StackItemType.INTEGER, item.getType());
        assertEquals(new BigInteger("1124"), item.getInteger());

        IntegerStackItem other = new IntegerStackItem(new BigInteger("1124"));
        assertEquals(other, item);
        assertEquals(other.hashCode(), item.hashCode());
    }

    @Test
    public void testDeserializeBooleanStackItem() throws IOException {
        String json = "{\"type\":\"Boolean\", \"value\":\"true\"}";
        StackItem item = getObjectMapper().readValue(json, StackItem.class);
        assertEquals(StackItemType.BOOLEAN, item.getType());
        assertTrue(item.getBoolean());

        json = "{\"type\":\"Boolean\", \"value\":false}";
        item = getObjectMapper().readValue(json, StackItem.class);
        assertFalse(item.getBoolean());

        BooleanStackItem other = new BooleanStackItem(false);
        assertEquals(other, item);
        assertEquals(other.hashCode(), item.hashCode());
    }

    @Test
    public void testDeserializeArrayStackItem() throws IOException {
        String json = ""
                + "{"
                + "  \"type\": \"Array\","
                + "  \"value\": ["
                + "    {"
                + "      \"type\": \"Boolean\","
                + "      \"value\": \"true\""
                + "    },"
                + "    {"
                + "      \"type\": \"Integer\","
                + "      \"value\": \"100\""
                + "    }"
                + "  ]"
                + "}";

        StackItem item = getObjectMapper().readValue(json, StackItem.class);
        assertEquals(StackItemType.ARRAY, item.getType());
        assertEquals(2, item.getList().size());
        assertEquals(StackItemType.BOOLEAN, item.getList().get(0).getType());
        assertEquals(StackItemType.INTEGER, item.getList().get(1).getType());

        List<StackItem> items = new ArrayList<>();
        items.add(new BooleanStackItem(true));
        items.add(new IntegerStackItem(BigInteger.valueOf(100)));
        ArrayStackItem other = new ArrayStackItem(items);
        assertEquals(other, item);
        assertEquals(other.hashCode(), item.hashCode());

        json = "{\"type\":\"Array\", \"value\":[]}";
        item = getObjectMapper().readValue(json, StackItem.class);
        assertEquals(0, item.getList().size());

        other = new ArrayStackItem(new ArrayList<>());
        assertEquals(other, item);
        assertEquals(other.hashCode(), item.hashCode());
    }

    @Test
    public void testDeserializeMapStackItem() throws IOException {
        String base64Data1 = "dGVzdF9rZXlfYQ==";
        String base64Data2 = "dGVzdF9rZXlfYg==";
        String json = ""
                + "{"
                + "  \"type\": \"Map\","
                + "  \"value\": ["
                + "    {"
                + "      \"key\": {"
                + "        \"type\": \"ByteString\","
                + "        \"value\": \"" + base64Data1 + "\""
                + "      },"
                + "      \"value\": {"
                + "        \"type\": \"Boolean\","
                + "        \"value\": \"false\""
                + "      }"
                + "    },"
                + "    {"
                + "      \"key\": {"
                + "        \"type\": \"ByteString\","
                + "        \"value\": \"" + base64Data2 + "\""
                + "      },"
                + "      \"value\": {"
                + "        \"type\": \"Integer\","
                + "        \"value\": \"12345\""
                + "      }"
                + "    }"
                + "  ]"
                + "}";

        StackItem item = getObjectMapper().readValue(json, StackItem.class);
        assertEquals(StackItemType.MAP, item.getType());
        Map<StackItem, StackItem> map = item.getMap();
        assertEquals(2, map.size());
        Set<StackItem> keys = map.keySet();
        ByteStringStackItem key1 = new ByteStringStackItem(Base64.decode(base64Data1));
        ByteStringStackItem key2 = new ByteStringStackItem(Base64.decode(base64Data2));
        assertThat(keys, containsInAnyOrder(key1, key2));
        Collection<StackItem> values = map.values();
        BooleanStackItem val1 = new BooleanStackItem(false);
        IntegerStackItem val2 = new IntegerStackItem(new BigInteger("12345"));
        assertThat(values, containsInAnyOrder(val1, val2));

        map = new HashMap<>();
        map.put(key1, val1);
        map.put(key2, val2);
        MapStackItem other = new MapStackItem(map);
        assertEquals(other, item);
        assertEquals(other.hashCode(), item.hashCode());

        json = "{\"type\":\"Map\", \"value\":[]}";
        item = getObjectMapper().readValue(json, StackItem.class);
        assertTrue(item.getMap().isEmpty());
        other = new MapStackItem(new HashMap<>());
        assertEquals(other, item);
        assertEquals(other.hashCode(), item.hashCode());
    }

    @Test
    public void testDeserializeInteropInterfaceStackItem() throws IOException {
        String json = "{\n" +
                "    \"type\": \"InteropInterface\",\n" +
                "    \"interface\": \"IIterator\",\n" +
                "    \"id\": \"fcf7b800-192a-488f-95d3-c40ac7b30ef1\"\n" +
                "}";
        StackItem item = getObjectMapper().readValue(json, StackItem.class);
        assertThat(item.getType(), is(StackItemType.INTEROP_INTERFACE));
        assertThat(item.getIteratorId(), is("fcf7b800-192a-488f-95d3-c40ac7b30ef1"));
        assertThat(item.getValue(), is("fcf7b800-192a-488f-95d3-c40ac7b30ef1"));
        assertThat(item.valueToString(), is("fcf7b800-192a-488f-95d3-c40ac7b30ef1"));

        InteropInterfaceStackItem interopInterfaceStackItem = getObjectMapper()
                .readValue(json, InteropInterfaceStackItem.class);
        assertThat(interopInterfaceStackItem.getType(), is(StackItemType.INTEROP_INTERFACE));
        assertThat(interopInterfaceStackItem.getInterfaceName(), is("IIterator"));
        assertThat(interopInterfaceStackItem.getIteratorId(), is("fcf7b800-192a-488f-95d3-c40ac7b30ef1"));

        InteropInterfaceStackItem expected = new InteropInterfaceStackItem("IIterator",
                "fcf7b800-192a-488f-95d3-c40ac7b30ef1");
        assertThat(item, is(expected));
        assertThat(interopInterfaceStackItem, is(expected));
    }

    @Test
    public void testDeserializeStructStackItem() throws IOException {
        String json = ""
                + "{"
                + "  \"type\": \"Struct\","
                + "  \"value\": ["
                + "    {"
                + "      \"type\": \"Boolean\","
                + "      \"value\": \"true\""
                + "    },"
                + "    {"
                + "      \"type\": \"Integer\","
                + "      \"value\": \"100\""
                + "    }"
                + "  ]"
                + "}";

        StackItem item = getObjectMapper().readValue(json, StackItem.class);
        assertEquals(StackItemType.STRUCT, item.getType());
        List<StackItem> array = item.getList();
        assertEquals(2, array.size());
        assertEquals(StackItemType.BOOLEAN, array.get(0).getType());
        assertEquals(StackItemType.INTEGER, array.get(1).getType());

        List<StackItem> items = new ArrayList<>();
        items.add(new BooleanStackItem(true));
        items.add(new IntegerStackItem(BigInteger.valueOf(100)));
        StructStackItem other = new StructStackItem(items);
        assertEquals(other, item);
        assertEquals(other.hashCode(), item.hashCode());

        json = "{\"type\":\"Struct\", \"value\":[]}";

        item = getObjectMapper().readValue(json, StackItem.class);
        other = new StructStackItem(new ArrayList<>());
        assertEquals(other, item);
        assertEquals(other.hashCode(), item.hashCode());
    }

    @Test
    public void testStackItemType_ByteValue() {
        StackItemType type = StackItemType.BUFFER;
        assertThat(type.getValue(), is("Buffer"));
        assertThat(type.byteValue(), is((byte) 48));
        assertThat(StackItemType.valueOf((byte) 33), is(StackItemType.INTEGER));
    }

    @Test
    public void testStackItemType_FromJsonValue() {
        assertThat(StackItemType.fromJsonValue("Boolean"), is(StackItemType.BOOLEAN));
    }

    @Test
    public void testStackItemType_InvalidByteValue() {
        IllegalArgumentException thrown =
                assertThrows(IllegalArgumentException.class, () -> StackItemType.valueOf((byte) 31));
        assertThat(thrown.getMessage(), containsString("There exists no stack item with the provided byte value"));
    }

    @Test
    public void testStackItemType_InvalidJsonValue() {
        IllegalArgumentException thrown =
                assertThrows(IllegalArgumentException.class, () -> StackItemType.fromJsonValue("Enum"));
        assertThat(thrown.getMessage(), containsString("There exists no stack item with the provided json value."));
    }

    @Test
    public void anyStackItemValueToString() {
        AnyStackItem item = new AnyStackItem(null);

        assertThat(item.toString(), is("Any{value='null'}"));
    }

    @Test
    public void getIntegerFromBooleanStackItem() {
        BooleanStackItem booleanStackItem = new BooleanStackItem(true);
        assertThat(booleanStackItem.getInteger(), is(BigInteger.ONE));

        booleanStackItem = new BooleanStackItem(false);
        assertThat(booleanStackItem.getInteger(), is(BigInteger.ZERO));
    }

    @Test
    public void getStringFromBooleanStackItem() {
        BooleanStackItem booleanStackItem = new BooleanStackItem(true);
        assertThat(booleanStackItem.getString(), is("true"));

        booleanStackItem = new BooleanStackItem(false);
        assertThat(booleanStackItem.getString(), is("false"));
    }

    @Test
    public void getBooleanFromByteArrayStackItem() {
        BufferStackItem item = new BufferStackItem(Numeric.hexStringToByteArray("0x010203"));
        assertTrue(item.getBoolean());

        item = new BufferStackItem(Numeric.hexStringToByteArray("0x000000"));
        assertFalse(item.getBoolean());

        item = new BufferStackItem();

        StackItemCastException thrown = assertThrows(StackItemCastException.class, item::getBoolean);
        assertThat(thrown.getMessage(), is("Cannot cast stack item because its value is null"));
    }

    @Test
    public void compareTwoBufferStackItems() {
        BufferStackItem item1 = new BufferStackItem(Numeric.hexStringToByteArray("010203"));
        BufferStackItem item2 = new BufferStackItem(Numeric.hexStringToByteArray("010203"));
        ByteStringStackItem item3 = new ByteStringStackItem(Numeric.hexStringToByteArray("010203"));

        assertEquals(item1, item1);
        assertEquals(item1, item2);
        assertNotEquals(item1, item3);
    }

    @Test
    public void compareTwoByteSringStackItems() {
        ByteStringStackItem item1 = new ByteStringStackItem(Numeric.hexStringToByteArray("010203"));
        ByteStringStackItem item2 = new ByteStringStackItem(Numeric.hexStringToByteArray("010203"));
        BufferStackItem item3 = new BufferStackItem(Numeric.hexStringToByteArray("010203"));

        assertEquals(item1, item1);
        assertEquals(item1, item2);
        assertNotEquals(item1, item3);
    }

    @Test
    public void getBooleanFromIntegerStackItem() {
        IntegerStackItem item1 = new IntegerStackItem(BigInteger.ONE);
        assertTrue(item1.getBoolean());

        item1 = new IntegerStackItem(BigInteger.ZERO);
        assertFalse(item1.getBoolean());
    }

    @Test
    public void throwExceptionOnGetAddressFromByteArrayWithIllegalAddress() {
        ByteStringStackItem item = new ByteStringStackItem(Numeric.hexStringToByteArray("010203"));
        assertThrows(StackItemCastException.class, item::getAddress);
    }

    @Test
    public void throwExceptionOnGetIntegerFromEmptyByteArray() {
        ByteStringStackItem item = new ByteStringStackItem(new byte[]{});
        assertThrows(StackItemCastException.class, item::getInteger);
    }

    @Test
    public void getStringFromIntegerStackItem() {
        IntegerStackItem item = new IntegerStackItem(new BigInteger("1000"));
        assertThat(item.getString(), is("1000"));
    }

    @Test
    public void throwOnGetStringFromNullIntegerStackItem() {
        IntegerStackItem item = new IntegerStackItem(null);

        StackItemCastException thrown = assertThrows(StackItemCastException.class, item::getString);
        assertThat(thrown.getMessage(), is("Cannot cast stack item because its value is null"));
    }

    @Test
    public void getHexStringFromIntegerStackItem() {
        IntegerStackItem item = new IntegerStackItem(new BigInteger("1000"));
        assertThat(item.getHexString(), is("e803"));
    }

    @Test
    public void throwOnGetHexStringFromNullIntegerStackItem() {
        IntegerStackItem item = new IntegerStackItem(null);
        StackItemCastException thrown = assertThrows(StackItemCastException.class, item::getHexString);
        assertThat(thrown.getMessage(), is("Cannot cast stack item because its value is null"));
    }

    @Test
    public void getByteArrayFromIntegerStackItem() {
        IntegerStackItem item = new IntegerStackItem(new BigInteger("1000"));
        assertThat(item.getByteArray(), is(Numeric.hexStringToByteArray("e803")));
    }

    @Test
    public void throwOnGetByteArrayFromNullIntegerStackItem() {
        IntegerStackItem item = new IntegerStackItem(null);
        StackItemCastException thrown = assertThrows(StackItemCastException.class, item::getByteArray);
        assertThat(thrown.getMessage(), is("Cannot cast stack item because its value is null"));
    }

    @Test
    public void listLikeStackItemValueToString() {
        ArrayStackItem item = new ArrayStackItem(
                asList(new ByteStringStackItem("word".getBytes(UTF_8)), new IntegerStackItem(BigInteger.ONE)));
        assertThat(item.valueToString(), is("ByteString{value='776f7264'}, Integer{value='1'}"));
    }

    @Test
    public void mapStackItemValueToString() {
        Map<StackItem, StackItem> map = new HashMap<>();
        map.put(new ByteStringStackItem("key1".getBytes(UTF_8)),
                new IntegerStackItem(BigInteger.ONE));
        map.put(new ByteStringStackItem("key2".getBytes(UTF_8)),
                new IntegerStackItem(BigInteger.ZERO));
        MapStackItem item = new MapStackItem(map);

        assertThat(item.valueToString(), containsString(
                "ByteString{value='6b657932'} -> Integer{value='0'}"));
        assertThat(item.valueToString(), containsString(
                "ByteString{value='6b657931'} -> Integer{value='1'}"));
    }

}
