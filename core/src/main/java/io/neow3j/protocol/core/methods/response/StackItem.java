package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.neow3j.model.types.StackItemType;
import io.neow3j.protocol.core.methods.response.StackItem.StackDeserializer;
import io.neow3j.utils.Numeric;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@JsonDeserialize(using = StackDeserializer.class)
public class StackItem {

    protected StackItemType type;
    protected Object value;

    public StackItem(StackItemType type, Object value) {
        this.type = type;
        this.value = value;
    }

    /**
     * Returns the type of this stack item.
     *
     * @return the type of this stack item.
     */
    public StackItemType getType() {
        return type;
    }

    /**
     * Returns the value of this stack item.
     *
     * @return the value of this stack item.
     */
    public Object getValue() {
        return this.value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StackItem other = (StackItem) o;
        return this.type == other.type && Objects.equals(this.value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.type, this.value);
    }

    @Override
    public String toString() {
        return "StackItem{" +
                "type=" + this.type +
                ", value=" + this.value +
                '}';
    }

    /**
     * Casts this stack item to a {@link ByteArrayStackItem}, if possible, and returns it.
     *
     * @return this stack item as a {@link ByteArrayStackItem}.
     * @throws IllegalStateException if this stack item is not an instance of
     *                               {@link ByteArrayStackItem}.
     */
    public ByteArrayStackItem asByteArray() {
        if (this instanceof ByteArrayStackItem) {
            return (ByteArrayStackItem) this;
        }
        throw new IllegalStateException("This stack item is not of type " +
                StackItemType.BYTE_ARRAY.jsonValue() + " but of " + this.type.jsonValue());
    }

    /**
     * Casts this stack item to a {@link BooleanStackItem}, if possible, and returns it.
     *
     * @return this stack item as a {@link BooleanStackItem}.
     * @throws IllegalStateException if this stack item is not an instance of
     *                               {@link BooleanStackItem}.
     */
    public BooleanStackItem asBoolean() {
        if (this instanceof BooleanStackItem) {
            return (BooleanStackItem) this;
        }
        throw new IllegalStateException("This stack item is not of type " +
                StackItemType.BOOLEAN.jsonValue() + " but of " + this.type.jsonValue());
    }

    /**
     * Casts this stack item to a {@link IntegerStackItem}, if possible, and returns it.
     *
     * @return this stack item as a {@link IntegerStackItem}.
     * @throws IllegalStateException if this stack item is not an instance of
     *                               {@link IntegerStackItem}.
     */
    public IntegerStackItem asInteger() {
        if (this instanceof IntegerStackItem) {
            return (IntegerStackItem) this;
        }
        throw new IllegalStateException("This stack item is not of type " +
                StackItemType.INTEGER.jsonValue() + " but of " + this.type.jsonValue());
    }

    /**
     * Casts this stack item to a {@link ArrayStackItem}, if possible, and returns it.
     *
     * @return this stack item as a {@link ArrayStackItem}.
     * @throws IllegalStateException if this stack item is not an instance of
     *                               {@link ArrayStackItem}.
     */
    public ArrayStackItem asArray() {
        if (this instanceof ArrayStackItem) {
            return (ArrayStackItem) this;
        }
        throw new IllegalStateException("This stack item is not of type " +
                StackItemType.ARRAY.jsonValue() + " but of " + this.type.jsonValue());
    }

    /**
     * Casts this stack item to a {@link MapStackItem}, if possible, and returns it.
     *
     * @return this stack item as a {@link MapStackItem}.
     * @throws IllegalStateException if this stack item is not an instance of
     *                               {@link MapStackItem}.
     */
    public MapStackItem asMap() {
        if (this instanceof MapStackItem) {
            return (MapStackItem) this;
        }
        throw new IllegalStateException("This stack item is not of type " +
                StackItemType.MAP.jsonValue() + " but of " + this.type.jsonValue());
    }

    /**
     * Casts this stack item to a {@link StructStackItem}, if possible, and returns it.
     *
     * @return this stack item as a {@link StructStackItem}.
     * @throws IllegalStateException if this stack item is not an instance of
     *                               {@link StructStackItem}.
     */
    public StructStackItem asStruct() {
        if (this instanceof StructStackItem) {
            return (StructStackItem) this;
        }
        throw new IllegalStateException("This stack item is not of type " +
                StackItemType.STRUCT.jsonValue() + " but of " + this.type.jsonValue());
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

        private StackItem deserializeStackItem(JsonNode itemNode, JsonParser jp)
                throws JsonProcessingException {

            JsonNode typeNode = itemNode.get("type");
            JsonNode valueNode = itemNode.get("value");
            StackItemType type = null;
            if (typeNode != null) {
                type = jp.getCodec().treeToValue(typeNode, StackItemType.class);
            }
            if (valueNode == null) {
                return new StackItem(type, null);
            }
            if (type == null) {
                return new StackItem(null, valueNode.asText());
            }
            switch (type) {
                case BYTE_ARRAY:
                    return new ByteArrayStackItem(Numeric.hexStringToByteArray(valueNode.asText()));
                case BOOLEAN:
                    return new BooleanStackItem(valueNode.asBoolean());
                case INTEGER:
                    if (valueNode.asText().isEmpty()) {
                        return new IntegerStackItem(BigInteger.ZERO);
                    }
                    return new IntegerStackItem(new BigInteger(valueNode.asText()));
                case ARRAY:
                    List<StackItem> items = new ArrayList<>();
                    for (final JsonNode item : valueNode) {
                        items.add(deserializeStackItem(item, jp));
                    }
                    return new ArrayStackItem(items);
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
                case STRUCT:
                    items = new ArrayList<>();
                    for (final JsonNode item : valueNode) {
                        items.add(deserializeStackItem(item, jp));
                    }
                    return new StructStackItem(items);
                case INTEROP_INTERFACE:
                    return new StackItem(type, valueNode.asText());
                default:
                    throw new UnsupportedOperationException("Parameter type \'" + type +
                            "\' not supported.");
            }
        }
    }
}
