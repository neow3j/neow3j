package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.neow3j.model.types.StackItemType;
import io.neow3j.protocol.core.methods.response.StackItem.StackDeserializer;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonDeserialize(using = StackDeserializer.class)
public class StackItem {

    @JsonProperty("type")
    protected StackItemType type;

    @JsonProperty("value")
    protected Object value;

    public StackItem() {
    }

    public StackItem(StackItemType type) {
        this.type = type;
    }

    public StackItem(StackItemType type, Object value) {
        this.type = type;
        this.value = value;
    }

    public StackItemType getType() {
        return type;
    }

    public Object getValue() {
        return this.value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StackItem stackItem = (StackItem) o;
        return type == stackItem.type &&
                Objects.equals(getValue(), stackItem.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, getValue());
    }

    @Override
    public String toString() {
        return "StackItem{" +
                "type=" + type +
                ", value=" + getValue() +
                '}';
    }

    public static class StackDeserializer extends StdDeserializer<StackItem> {

        protected StackDeserializer() {
            this(null);
        }

        protected StackDeserializer(Class<StackItem> vc) {
            super(vc);
        }

        public StackItem deserialize(JsonParser jp, DeserializationContext ctxt)
                throws IOException {

            JsonNode node = jp.getCodec().readTree(jp);
            return deserializeStackItem(node, jp);
        }

        private StackItem deserializeStackItem(JsonNode item, JsonParser jp)
                throws JsonProcessingException {

            JsonNode typeNode = item.get("type");
            StackItemType type = null;
            if (typeNode != null) {
                type = jp.getCodec().treeToValue(typeNode, StackItemType.class);
            }
            if (type == null) {
                return new StackItem();
            }

            JsonNode valueNode = item.get("value");
            if (valueNode != null) {
                switch (type) {
                    case BYTE_ARRAY:
                        return new ByteArrayStackItem(valueNode.asText());
                    case BOOLEAN:
                        return new BooleanStackItem(valueNode.asBoolean());
                    case INTEGER:
                        return new IntegerStackItem(new BigInteger(valueNode.asText()));
                    case ARRAY:
                        if (valueNode.isArray()) {
                            List<StackItem> items = new ArrayList<>();
                            for (final JsonNode item : valueNode) {
                                items.add(deserializeStackItem(item, jp));
                            }
                            return new ArrayStackItem(items);
                        } else {
                            return new ArrayStackItem(null);
                        }
                    case MAP:
                        Iterator<JsonNode> elements = valueNode.elements();
                        Map<StackItem, StackItem> map = new HashMap<>();
                        while (elements.hasNext()) {
                            JsonNode element = elements.next();
                            StackItem keyItem = deserializeStackItem(element.get("key"), jp);
                            StackItem valueItem = deserializeStackItem(element.get("value"), jp);
                            map.put(keyItem, valueItem);
                        }
                        return new MapStackItem(map);
                    case INTEROP_INTERFACE:
                    case STRUCT:
                    default:
                        throw new UnsupportedOperationException("Parameter type \'" + type +
                                "\' not supported.");
                }
            }
            return new StackItem(type);
        }
    }
}
