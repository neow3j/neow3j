package io.neow3j.protocol.core.methods.response;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.neow3j.model.types.StackItemType;
import io.neow3j.protocol.ResponseTester;
import io.neow3j.utils.Numeric;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class StackItemTest extends ResponseTester {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final static String BYTESTRING_JSON = ""
            + " {"
            + "   \"type\": \"ByteString\",\n"
            + "   \"value\": \"V29vbG9uZw==\"\n"
            + " }";

    @Test
    public void testThrowOnWrongCast_ByteString() throws IOException {
        StackItem rawItem = OBJECT_MAPPER.readValue("{\"type\":\"Integer\",\"value\":\"1124\"}",
                StackItem.class);
        exceptionRule.expect(IllegalStateException.class);
        exceptionRule.expectMessage("not of type ByteString");
        rawItem.asByteString();
    }

    @Test
    public void testThrowOnWrongCast_Any() throws IOException {
        StackItem rawItem = OBJECT_MAPPER.readValue(BYTESTRING_JSON, StackItem.class);
        exceptionRule.expect(IllegalStateException.class);
        exceptionRule.expectMessage("not of type Any");
        rawItem.asAny();
    }

    @Test
    public void testThrowOnWrongCast_Pointer() throws IOException {
        StackItem rawItem = OBJECT_MAPPER.readValue(BYTESTRING_JSON, StackItem.class);
        exceptionRule.expect(IllegalStateException.class);
        exceptionRule.expectMessage("not of type Pointer");
        rawItem.asPointer();
    }

    @Test
    public void testThrowOnWrongCast_Integer() throws IOException {
        StackItem rawItem = OBJECT_MAPPER.readValue(BYTESTRING_JSON, StackItem.class);
        exceptionRule.expect(IllegalStateException.class);
        exceptionRule.expectMessage("not of type Integer");
        rawItem.asInteger();
    }

    @Test
    public void testThrowOnWrongCast_Boolean() throws IOException {
        StackItem rawItem = OBJECT_MAPPER.readValue(BYTESTRING_JSON, StackItem.class);
        exceptionRule.expect(IllegalStateException.class);
        exceptionRule.expectMessage("not of type Boolean");
        rawItem.asBoolean();
    }

    @Test
    public void testThrowOnWrongCast_Buffer() throws IOException {
        StackItem rawItem = OBJECT_MAPPER.readValue(BYTESTRING_JSON, StackItem.class);
        exceptionRule.expect(IllegalStateException.class);
        exceptionRule.expectMessage("not of type Buffer");
        rawItem.asBuffer();
    }

    @Test
    public void testThrowOnWrongCast_Array() throws IOException {
        StackItem rawItem = OBJECT_MAPPER.readValue(BYTESTRING_JSON, StackItem.class);
        exceptionRule.expect(IllegalStateException.class);
        exceptionRule.expectMessage("not of type Array");
        rawItem.asArray();
    }

    @Test
    public void testThrowOnWrongCast_Map() throws IOException {
        StackItem rawItem = OBJECT_MAPPER.readValue(BYTESTRING_JSON, StackItem.class);
        exceptionRule.expect(IllegalStateException.class);
        exceptionRule.expectMessage("not of type Map");
        rawItem.asMap();
    }

    @Test
    public void testThrowOnWrongCast_Struct() throws IOException {
        StackItem rawItem = OBJECT_MAPPER.readValue(BYTESTRING_JSON, StackItem.class);
        exceptionRule.expect(IllegalStateException.class);
        exceptionRule.expectMessage("not of type Struct");
        rawItem.asStruct();
    }

    @Test
    public void testThrowOnWrongCast_InteropInterface() throws IOException {
        StackItem rawItem = OBJECT_MAPPER.readValue(BYTESTRING_JSON, StackItem.class);
        exceptionRule.expect(IllegalStateException.class);
        exceptionRule.expectMessage("not of type InteropInterface");
        rawItem.asInteropInterface();
    }

    @Test
    public void testDeserializeAnyStackItem() throws IOException {
        String json = ""
                + " {"
                + "   \"type\": \"Any\",\n"
                + "   \"value\": \"dGVzdGluZw==\"\n"
                + " }";
        StackItem rawItem = OBJECT_MAPPER.readValue(json, StackItem.class);
        assertThat(rawItem.getType(), is(StackItemType.ANY));
        AnyStackItem item = rawItem.asAny();
        assertThat(item.getValue(), is("dGVzdGluZw=="));

        AnyStackItem other = new AnyStackItem("dGVzdGluZw==");
        assertEquals(other, item);
        assertEquals(other.hashCode(), item.hashCode());
    }

    @Test
    public void testDeserializePointerStackItem() throws IOException {
        String json = ""
                + " {"
                + "   \"type\": \"Pointer\",\n"
                + "   \"value\": \"123456\"\n"
                + " }";
        StackItem rawItem = OBJECT_MAPPER.readValue(json, StackItem.class);
        assertThat(rawItem.getType(), is(StackItemType.POINTER));
        PointerStackItem item = rawItem.asPointer();
        assertThat(item.getValue(), is(new BigInteger("123456")));

        PointerStackItem other = new PointerStackItem(new BigInteger("123456"));
        assertEquals(other, item);
        assertEquals(other.hashCode(), item.hashCode());
    }

    @Test
    public void testDeserializeByteStringStackItem() throws IOException {
        StackItem rawItem = OBJECT_MAPPER.readValue(BYTESTRING_JSON, StackItem.class);
        assertEquals(StackItemType.BYTE_STRING, rawItem.getType());
        ByteStringStackItem item = rawItem.asByteString();
        assertArrayEquals(Numeric.hexStringToByteArray("576f6f6c6f6e67"), item.getValue());
        assertEquals("Woolong", item.getAsString());

        String json = ""
                + " {"
                + "   \"type\": \"ByteString\",\n"
                + "   \"value\": \"aWQ=\"\n"
                + " }";

        item = OBJECT_MAPPER.readValue(json, StackItem.class).asByteString();
        assertArrayEquals(Numeric.hexStringToByteArray("6964"), item.getValue());
        assertEquals(new BigInteger("25705"), item.getAsNumber());

        json = ""
                + " {"
                + "   \"type\": \"ByteString\",\n"
                + "   \"value\": \"\"\n"
                + " }";
        item = OBJECT_MAPPER.readValue(json, StackItem.class).asByteString();
        assertThat(item.getAsNumber(), is(BigInteger.ZERO));

        json = ""
                + " {"
                + "   \"type\": \"ByteString\",\n"
                // The script hash hex string in little-endian format
                + "   \"value\": \"1Cz3qTHOPEZVD9kN5IJYP8XqcBo=\"\n"
                + " }";

        item = OBJECT_MAPPER.readValue(json, StackItem.class).asByteString();
        assertArrayEquals(
                Numeric.hexStringToByteArray("d42cf7a931ce3c46550fd90de482583fc5ea701a"),
                item.getValue()
        );
        assertThat(item.getAsAddress(), is("Ab7kmZJw2yJDNREnyBByt1QEZGbzj9uBf1"));
        assertThat(item.getAsHexString(), is("d42cf7a931ce3c46550fd90de482583fc5ea701a"));

        ByteStringStackItem other = new ByteStringStackItem(
                Numeric.hexStringToByteArray("d42cf7a931ce3c46550fd90de482583fc5ea701a"));
        assertEquals(other, item);
        assertEquals(other.hashCode(), item.hashCode());
    }

    @Test
    public void testDeserializeByteStringStackItem_getAsJson() throws IOException {
        String json = ""
                + " {"
                + "   \"type\": \"ByteString\",\n"
                + "   \"value\": \"eyJuYW1lIjoidGVzdCBuYW1lIiwiZGVzY3JpcHRpb24iOiJ0ZXN0IGRlc2NyaXB0aW9uIn0=\"\n"
                + " }";
        StackItem rawItem = OBJECT_MAPPER.readValue(json, StackItem.class);
        assertThat(rawItem.getType(), is(StackItemType.BYTE_STRING));
        ByteStringStackItem item = rawItem.asByteString();
        NFTokenProperties properties = item.getAsJson(NFTokenProperties.class);
        assertThat(properties.getName(), is("test name"));
        assertThat(properties.getDescription(), is("test description"));
        assertNull(properties.getImage());
        assertNull(properties.getTokenURI());

        json = ""
                + " {"
                + "   \"type\": \"ByteString\",\n"
                + "   \"value\": \"eyJuYW1lIjoidGVzdCBuYW1lIiwiZGVzY3JpcHRpb24iOiJ0ZXN0IGRlc2NyaXB0aW9uIiwiaW1hZ2UiOiAic29tZSBpbWFnZSBVUkkifQ==\"\n"
                + " }";

        item = OBJECT_MAPPER.readValue(json, StackItem.class).asByteString();
        properties = item.getAsJson(NFTokenProperties.class);
        assertThat(properties.getName(), is("test name"));
        assertThat(properties.getDescription(), is("test description"));
        assertThat(properties.getImage(), is("some image URI"));
        assertNull(properties.getTokenURI());

        json = ""
                + " {"
                + "   \"type\": \"ByteString\",\n"
                + "   \"value\": \"eyJuYW1lIjoidGVzdCBuYW1lIiwiZGVzY3JpcHRpb24iOiJ0ZXN0IGRlc2NyaXB0aW9uIiwidG9rZW5VUkkiOiJzb21lIFVSSSJ9\"\n"
                + " }";

        item = OBJECT_MAPPER.readValue(json, StackItem.class).asByteString();
        properties = item.getAsJson(NFTokenProperties.class);
        assertThat(properties.getName(), is("test name"));
        assertThat(properties.getDescription(), is("test description"));
        assertNull(properties.getImage());
        assertThat(properties.getTokenURI(), is("some URI"));

        json = ""
                + " {"
                + "   \"type\": \"ByteString\",\n"
                + "   \"value\": \"eyJuYW1lIjoidGVzdCBuYW1lIiwiZGVzY3JpcHRpb24iOiJ0ZXN0IGRlc2NyaXB0aW9uIiwiaW1hZ2UiOiJzb21lIGltYWdlIFVSSSIsInRva2VuVVJJIjoic29tZSBVUkkifQ==\"\n"
                + " }";

        item = OBJECT_MAPPER.readValue(json, StackItem.class).asByteString();
        properties = item.getAsJson(NFTokenProperties.class);
        assertThat(properties.getName(), is("test name"));
        assertThat(properties.getDescription(), is("test description"));
        assertThat(properties.getImage(), is("some image URI"));
        assertThat(properties.getTokenURI(), is("some URI"));
    }

    @Test
    public void testDeserializeBufferStackItem() throws IOException {
        String json = ""
                + " {"
                + "   \"type\": \"Buffer\",\n"
                + "   \"value\": \"ew==\"\n"
                + " }";
        BufferStackItem item = OBJECT_MAPPER.readValue(json, StackItem.class).asBuffer();
        assertThat(item.getAsNumber(), is(new BigInteger("123")));

        json = ""
                + " {"
                + "   \"type\": \"Buffer\",\n"
                + "   \"value\": \"\"\n"
                + " }";
        item = OBJECT_MAPPER.readValue(json, StackItem.class).asBuffer();
        assertThat(item.getAsNumber(), is(BigInteger.ZERO));

        json = ""
                + " {"
                + "   \"type\": \"Buffer\",\n"
                + "   \"value\": \"V29vbG9uZw==\"\n"
                + " }";
        item = OBJECT_MAPPER.readValue(json, StackItem.class).asBuffer();
        assertThat(item.getAsString(), is("Woolong"));

        json = ""
                + " {"
                + "   \"type\": \"Buffer\",\n"
                + "   \"value\": \"1Cz3qTHOPEZVD9kN5IJYP8XqcBo=\"\n"
                + " }";
        item = OBJECT_MAPPER.readValue(json, StackItem.class).asBuffer();
        assertThat(item.getAsAddress(), is("Ab7kmZJw2yJDNREnyBByt1QEZGbzj9uBf1"));

        json = ""
                + " {"
                + "   \"type\": \"Buffer\",\n"
                + "   \"value\": \"V29vbG9uZw==\"\n"
                + " }";
        item = OBJECT_MAPPER.readValue(json, StackItem.class).asBuffer();
        assertThat(item.getValue(), is(Numeric.hexStringToByteArray("576f6f6c6f6e67")));

        BufferStackItem other = new BufferStackItem(Numeric.hexStringToByteArray("576f6f6c6f6e67"));
        assertEquals(other, item);
        assertEquals(other.hashCode(), item.hashCode());
    }

    @Test
    public void testDeserializeIntegerStackItem() throws IOException {
        String json = ""
                + " {"
                + "   \"type\": \"Integer\",\n"
                + "   \"value\": \"1124\"\n"
                + " }";

        StackItem rawItem = OBJECT_MAPPER.readValue(json, StackItem.class);
        assertEquals(StackItemType.INTEGER, rawItem.getType());
        IntegerStackItem item = rawItem.asInteger();
        assertEquals(new BigInteger("1124"), item.getValue());

        json = ""
                + " {"
                + "   \"type\": \"Integer\",\n"
                + "   \"value\": \"\"\n"
                + " }";

        item = OBJECT_MAPPER.readValue(json, StackItem.class).asInteger();
        assertEquals(BigInteger.ZERO, item.getValue());

        IntegerStackItem other = new IntegerStackItem(BigInteger.ZERO);
        assertEquals(other, item);
        assertEquals(other.hashCode(), item.hashCode());
    }

    @Test
    public void testDeserializeBooleanStackItem() throws IOException {
        String json = ""
                + " {"
                + "   \"type\": \"Boolean\",\n"
                + "   \"value\": \"true\"\n"
                + " }";

        StackItem rawItem = OBJECT_MAPPER.readValue(json, StackItem.class);
        assertEquals(StackItemType.BOOLEAN, rawItem.getType());
        BooleanStackItem item = rawItem.asBoolean();
        assertTrue(item.getValue());

        json = ""
                + " {"
                + "   \"type\": \"Boolean\",\n"
                + "   \"value\": false\n"
                + " }";

        item = OBJECT_MAPPER.readValue(json, StackItem.class).asBoolean();
        assertFalse(item.getValue());

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

        StackItem rawItem = OBJECT_MAPPER.readValue(json, StackItem.class);
        assertEquals(StackItemType.ARRAY, rawItem.getType());
        ArrayStackItem item = rawItem.asArray();
        assertEquals(2, item.size());
        assertEquals(StackItemType.BOOLEAN, item.get(0).getType());
        assertEquals(StackItemType.INTEGER, item.get(1).getType());

        List<StackItem> items = new ArrayList<>();
        items.add(new BooleanStackItem(true));
        items.add(new IntegerStackItem(BigInteger.valueOf(100)));
        ArrayStackItem other = new ArrayStackItem(items);
        assertEquals(other, item);
        assertEquals(other.hashCode(), item.hashCode());

        json = ""
                + "{"
                + "  \"type\": \"Array\","
                + "  \"value\": []"
                + "}";

        item = OBJECT_MAPPER.readValue(json, StackItem.class).asArray();
        assertTrue(item.isEmpty());

        other = new ArrayStackItem(new ArrayList<>());
        assertEquals(other, item);
        assertEquals(other.hashCode(), item.hashCode());
    }

    @Test
    public void testDeserializeMapStackItem() throws IOException {
        String json = ""
                + "{"
                + "  \"type\": \"Map\","
                + "  \"value\": ["
                + "    {"
                + "      \"key\": {"
                + "        \"type\": \"ByteString\","
                + "        \"value\": \"dGVzdF9rZXlfYQ==\""
                + "      },"
                + "      \"value\": {"
                + "        \"type\": \"Boolean\","
                + "        \"value\": \"false\""
                + "      }"
                + "    },"
                + "    {"
                + "      \"key\": {"
                + "        \"type\": \"ByteString\","
                + "        \"value\": \"dGVzdF9rZXlfYg==\""
                + "      },"
                + "      \"value\": {"
                + "        \"type\": \"Integer\","
                + "        \"value\": \"12345\""
                + "      }"
                + "    }"
                + "  ]"
                + "}";

        StackItem rawItem = OBJECT_MAPPER.readValue(json, StackItem.class);
        assertEquals(StackItemType.MAP, rawItem.getType());
        MapStackItem item = rawItem.asMap();
        assertEquals(2, item.size());
        assertEquals(StackItemType.BOOLEAN, item.get("test_key_a").getType());
        assertFalse(item.get("test_key_a").asBoolean().getValue());
        assertEquals(StackItemType.INTEGER, item.get("test_key_b").getType());
        assertEquals(BigInteger.valueOf(12345), item.get("test_key_b").asInteger().getValue());

        ByteStringStackItem key1 = new ByteStringStackItem(Numeric.hexStringToByteArray("746573745f6b65795f61"));
        assertFalse(item.get(key1).asBoolean().getValue());
        ByteStringStackItem key2 = new ByteStringStackItem(Numeric.hexStringToByteArray("746573745f6b65795f62"));
        assertEquals(BigInteger.valueOf(12345), item.get(key2).asInteger().getValue());

        Map<StackItem, StackItem> map = new HashMap<>();
        map.put(key1, new BooleanStackItem(false));
        map.put(key2, new IntegerStackItem(BigInteger.valueOf(12345)));
        MapStackItem other = new MapStackItem(map);
        assertEquals(other, item);
        assertEquals(other.hashCode(), item.hashCode());

        json = ""
                + "{"
                + "  \"type\": \"Map\","
                + "  \"value\": []"
                + "}";

        item = OBJECT_MAPPER.readValue(json, StackItem.class).asMap();
        assertTrue(item.isEmpty());

        other = new MapStackItem(new HashMap<>());
        assertEquals(other, item);
        assertEquals(other.hashCode(), item.hashCode());
    }

    @Test
    public void testDeserializeMapStackItem_GetByByte() throws IOException {
        String json = ""
                + "{"
                + "  \"type\": \"Map\","
                + "  \"value\": ["
                + "    {"
                + "      \"key\": {"
                + "        \"type\": \"ByteString\","
                + "        \"value\": \"dGVzdF9rZXlfYQ==\""
                + "      },"
                + "      \"value\": {"
                + "        \"type\": \"Boolean\","
                + "        \"value\": \"false\""
                + "      }"
                + "    }"
                + "  ]"
                + "}";

        StackItem rawItem = OBJECT_MAPPER.readValue(json, StackItem.class);
        assertEquals(StackItemType.MAP, rawItem.getType());
        MapStackItem item = rawItem.asMap();
        assertThat(item.get("test_key_c"), is(nullValue()));
        BooleanStackItem other = new BooleanStackItem(false);
        assertThat(item.get(Numeric.hexStringToByteArray("746573745f6b65795f61")), is(other));
    }

    @Test
    public void testDeserializeMapStackItem_GetByByte_Null() throws IOException {
        String json = ""
                + "{"
                + "  \"type\": \"Map\","
                + "  \"value\": ["
                + "    {"
                + "      \"key\": {"
                + "        \"type\": \"ByteString\","
                + "        \"value\": \"dGVzdF9rZXlfYQ==\""
                + "      },"
                + "      \"value\": {"
                + "        \"type\": \"Boolean\","
                + "        \"value\": \"false\""
                + "      }"
                + "    },"
                + "    {"
                + "      \"key\": {"
                + "        \"type\": \"ByteString\","
                + "        \"value\": \"dGVzdF9rZXlfYg==\""
                + "      },"
                + "      \"value\": {"
                + "        \"type\": \"Integer\","
                + "        \"value\": \"12345\""
                + "      }"
                + "    }"
                + "  ]"
                + "}";

        StackItem rawItem = OBJECT_MAPPER.readValue(json, StackItem.class);
        MapStackItem item = rawItem.asMap();
        assertThat(item.get(Numeric.hexStringToByteArray("V29vbG9uZw==")), is(nullValue()));
    }

    @Test
    public void testDeserializeInteropInterfaceStackItem() throws IOException {
        String json = ""
                + " {"
                + "   \"type\": \"InteropInterface\",\n"
                + "   \"value\": \"dGVzdGluZw==\"\n"
                + " }";
        StackItem rawItem = OBJECT_MAPPER.readValue(json, StackItem.class);
        assertThat(rawItem.getType(), is(StackItemType.INTEROP_INTERFACE));
        InteropInterfaceStackItem item = rawItem.asInteropInterface();
        assertThat(item.getValue(), is("dGVzdGluZw=="));

        InteropInterfaceStackItem other = new InteropInterfaceStackItem("dGVzdGluZw==");
        assertEquals(other, item);
        assertEquals(other.hashCode(), item.hashCode());
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

        StackItem rawItem = OBJECT_MAPPER.readValue(json, StackItem.class);
        assertEquals(StackItemType.STRUCT, rawItem.getType());
        StructStackItem item = rawItem.asStruct();
        assertEquals(2, item.size());
        assertEquals(StackItemType.BOOLEAN, item.get(0).getType());
        assertEquals(StackItemType.INTEGER, item.get(1).getType());

        List<StackItem> items = new ArrayList<>();
        items.add(new BooleanStackItem(true));
        items.add(new IntegerStackItem(BigInteger.valueOf(100)));
        StructStackItem other = new StructStackItem(items);
        assertEquals(other, item);
        assertEquals(other.hashCode(), item.hashCode());

        json = ""
                + "{"
                + "  \"type\": \"Struct\","
                + "  \"value\": []"
                + "}";

        item = OBJECT_MAPPER.readValue(json, StackItem.class).asStruct();
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
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("no stack item with the provided byte value");
        StackItemType.valueOf((byte) 31);
    }

    @Test
    public void testStackItemType_InvalidJsonValue() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("no stack item with the provided json value");
        StackItemType.fromJsonValue("Enum");
    }
}
