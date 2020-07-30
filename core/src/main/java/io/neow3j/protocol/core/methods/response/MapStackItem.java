package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.neow3j.model.types.StackItemType;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

/**
 * Holds a map in which keys and values are StackItems.
 */
@JsonDeserialize(using = MapStackItem.StackMapDeserializer.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MapStackItem extends StackItem {

    @JsonProperty("value")
    private Map<StackItem, StackItem> value;

    public MapStackItem() {
        super(StackItemType.MAP);
    }

    public MapStackItem(Map<StackItem, StackItem> value) {
        super(StackItemType.MAP);
        this.value = value;
    }

    public Map<StackItem, StackItem> getValue() {
        return this.value;
    }

    /**
     * <p>Gets the item that corresponds to the given key.</p>
     * <br>
     * <p>This method only checks map entries which have a key of type {@link ByteStringStackItem}
     * because this is the usual type of the keys in a map stack item.</p>
     *
     * @param key the key whose associated value is to be returned.
     * @return the value to which the given key is mapped, or null if this map stack item
     * contains no mapping for the key.
     */
    public StackItem get(String key) {
        for (Entry<StackItem, StackItem> e : getValue().entrySet()) {
            if (e.getKey() instanceof ByteStringStackItem &&
                    e.getKey().asByteString().getAsString().equals(key)) {
                return e.getValue();
            }
        }
        return null;
    }

    /**
     * <p>Gets the item that corresponds to the given key.</p>
     * <br>
     * <p>This method only checks map entries which have a key of type {@link ByteStringStackItem}
     * because this is the usual type of the keys in a map stack item.</p>
     *
     * @param key the key whose associated value is to be returned.
     * @return the value to which the given key is mapped, or null if this map stack item
     * contains no mapping for the key.
     */
    public StackItem get(byte[] key) {
        for (Entry<StackItem, StackItem> e : getValue().entrySet()) {
            if (e.getKey() instanceof ByteStringStackItem &&
                    Arrays.equals(e.getKey().asByteString().getValue(), key)) {
                return e.getValue();
            }
        }
        return null;
    }

    /**
     * Gets the stack item that corresponds to the given key.
     *
     * @param key the key whose associated value is to be returned.
     * @return the value to which the given key is mapped, or null if this map stack item
     * contains no mapping for the key.
     */
    public StackItem get(StackItem key) {
        return getValue().get(key);
    }

    /**
     * Returns the number of key-value pairs in this map stack item.
     *
     * @return the number of key-value pairs in this map stack item.
     */
    public int size() {
        return getValue().size();
    }

    /**
     * Returns true if this map contains no entries.
     *
     * @return true if this map contains no entries. False, otherwise.
     */
    public boolean isEmpty() {
        return getValue().isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MapStackItem)) return false;
        MapStackItem other = (MapStackItem) o;
        return getType() == other.getType() &&
                Objects.equals(getValue(), other.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getValue());
    }

    /**
     * This class deserializes the key-value pairs of a MapStackItem.
     */
    public static class StackMapDeserializer extends StdDeserializer<StackItem> {
        private final ObjectMapper objectMapper;

        protected StackMapDeserializer() {
            this(null);
        }

        protected StackMapDeserializer(Class<MapStackItem> vc) {
            super(vc);
            objectMapper = new ObjectMapper();
        }

        public MapStackItem deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {

            JsonNode node = jp.getCodec().readTree(jp);
            return deserializeMapStackItem(node);
        }

        private MapStackItem deserializeMapStackItem(JsonNode itemNode) throws IOException {
            JsonNode valueNode = itemNode.get("value");
            Iterator<JsonNode> elements = valueNode.elements();
            HashMap<StackItem, StackItem> map = new HashMap<>();
            while (elements.hasNext()) {
                JsonNode element = elements.next();
                JsonNode keyStackItem = element.get("key");
                StackItem keyItem = objectMapper.readValue(keyStackItem.toString(), StackItem.class);
                JsonNode valueStackItem = element.get("value");
                StackItem valueItem = objectMapper.readValue(valueStackItem.toString(), StackItem.class);
                map.put(keyItem, valueItem);
            }
            return new MapStackItem(map);
        }
    }

}
