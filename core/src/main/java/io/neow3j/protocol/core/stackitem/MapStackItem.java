package io.neow3j.protocol.core.stackitem;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.neow3j.types.StackItemType;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

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

    @Override
    protected String valueToString() {
        return value.entrySet().stream()
                .map(e -> e.getKey().toString() + " -> " + e.getValue().toString())
                .reduce("", (a, b) -> a + ", " + b)
                .substring(2); // remove the first comma and space.
    }

    @Override
    public Map<StackItem, StackItem> getMap() {
        nullCheck();
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MapStackItem)) {
            return false;
        }
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

        public MapStackItem deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
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
