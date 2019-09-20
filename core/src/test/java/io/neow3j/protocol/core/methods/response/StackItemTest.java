package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.neow3j.model.types.StackItemType;
import io.neow3j.protocol.ResponseTester;
import io.neow3j.utils.Numeric;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StackItemTest extends ResponseTester {

    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final static String BYTEARRAY_JSON = ""
            + " {"
            + "   \"type\": \"ByteArray\",\n"
            + "   \"value\": \"576f6f6c6f6e67\"\n"
            + " }";

    @Test(expected = IllegalStateException.class)
    public void testThrowOnWrongCastByteArray() throws IOException {
        StackItem rawItem = OBJECT_MAPPER.readValue("{\"type\":\"Integer\",\"value\":\"1124\"}",
                StackItem.class);
        rawItem.asByteArray();
    }

    @Test(expected = IllegalStateException.class)
    public void testThrowOnWrongCastInteger() throws IOException {
        StackItem rawItem = OBJECT_MAPPER.readValue(BYTEARRAY_JSON, StackItem.class);
        rawItem.asInteger();
    }

    @Test(expected = IllegalStateException.class)
    public void testThrowOnWrongCastBoolean() throws IOException {
        StackItem rawItem = OBJECT_MAPPER.readValue(BYTEARRAY_JSON, StackItem.class);
        rawItem.asBoolean();
    }

    @Test(expected = IllegalStateException.class)
    public void testThrowOnWrongCastArray() throws IOException {
        StackItem rawItem = OBJECT_MAPPER.readValue(BYTEARRAY_JSON, StackItem.class);
        rawItem.asArray();
    }

    @Test(expected = IllegalStateException.class)
    public void testThrowOnWrongCastMap() throws IOException {
        StackItem rawItem = OBJECT_MAPPER.readValue(BYTEARRAY_JSON, StackItem.class);
        rawItem.asMap();
    }

    @Test(expected = IllegalStateException.class)
    public void testThrowOnWrongCastStruct() throws IOException {
        StackItem rawItem = OBJECT_MAPPER.readValue(BYTEARRAY_JSON, StackItem.class);
        rawItem.asStruct();
    }

    @Test
    public void testDeserializeByteArrayStackItem() throws IOException {
        StackItem rawItem = OBJECT_MAPPER.readValue(BYTEARRAY_JSON, StackItem.class);
        assertEquals(StackItemType.BYTE_ARRAY, rawItem.getType());
        ByteArrayStackItem item = rawItem.asByteArray();
        assertArrayEquals(Numeric.hexStringToByteArray("576f6f6c6f6e67"), item.getValue());
        assertEquals("Woolong", item.getAsString());

        String json = ""
                + " {"
                + "   \"type\": \"ByteArray\",\n"
                + "   \"value\": \"6964\"\n"
                + " }";

        item = OBJECT_MAPPER.readValue(json, StackItem.class).asByteArray();
        assertArrayEquals(Numeric.hexStringToByteArray("6964"), item.getValue());
        assertEquals(new BigInteger("25705"), item.getAsNumber());

        json = ""
                + " {"
                + "   \"type\": \"ByteArray\",\n"
                // The script hash hex string in littel-endian format
                + "   \"value\": \"d42cf7a931ce3c46550fd90de482583fc5ea701a\"\n"
                + " }";

        item = OBJECT_MAPPER.readValue(json, StackItem.class).asByteArray();
        assertArrayEquals(
                Numeric.hexStringToByteArray("d42cf7a931ce3c46550fd90de482583fc5ea701a"),
                item.getValue()
        );
        assertEquals("Ab7kmZJw2yJDNREnyBByt1QEZGbzj9uBf1", item.getAsAddress());

        ByteArrayStackItem other = new ByteArrayStackItem(
                Numeric.hexStringToByteArray("d42cf7a931ce3c46550fd90de482583fc5ea701a"));
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
                + "        \"type\": \"ByteArray\","
                + "        \"value\": \"746573745f6b65795f61\""
                + "      },"
                + "      \"value\": {"
                + "        \"type\": \"Boolean\","
                + "        \"value\": \"false\""
                + "      }"
                + "    },"
                + "    {"
                + "      \"key\": {"
                + "        \"type\": \"ByteArray\","
                + "        \"value\": \"746573745f6b65795f62\""
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

        ByteArrayStackItem key1 = new ByteArrayStackItem(Numeric.hexStringToByteArray("746573745f6b65795f61"));
        assertFalse(item.get(key1).asBoolean().getValue());
        ByteArrayStackItem key2 = new ByteArrayStackItem(Numeric.hexStringToByteArray("746573745f6b65795f62"));
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
}
