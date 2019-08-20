package io.neow3j.protocol.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.neow3j.model.types.StackItemType;
import io.neow3j.protocol.core.methods.response.ArrayStackItem;
import io.neow3j.protocol.core.methods.response.BooleanStackItem;
import io.neow3j.protocol.core.methods.response.ByteArrayStackItem;
import io.neow3j.protocol.core.methods.response.IntegerStackItem;
import io.neow3j.protocol.core.methods.response.MapStackItem;
import io.neow3j.protocol.core.methods.response.StackItem;
import io.neow3j.utils.Numeric;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class StackDeserializer extends StdDeserializer<StackItem> {

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

    private StackItem deserializeStackItem(JsonNode param, JsonParser jp)
            throws JsonProcessingException {

        JsonNode typeNode = param.get("type");
        StackItemType type = null;
        if (typeNode != null) {
            type = jp.getCodec().treeToValue(typeNode, StackItemType.class);
        }
        if (type == null) {
            return new StackItem();
        }

        JsonNode valueNode = param.get("value");
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
