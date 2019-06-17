package io.neow3j.protocol.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.neow3j.model.types.StackItemType;
import io.neow3j.protocol.core.StackItemParser;
import io.neow3j.protocol.core.methods.response.stack.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

public class StackDeserializer extends StdDeserializer<Item> {
    private static final Logger log = LoggerFactory.getLogger(StackDeserializer.class);

    private ObjectMapper objectMapper;

    protected StackDeserializer() {
        this(null);
    }

    protected StackDeserializer(Class<?> vc) {
        super(vc);
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Item deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

        JsonNode node = p.getCodec().readTree(p);
        String type = node.get("type").asText();

        try {
            if (StackItemType.ARRAY.jsonValue().equals(type)) {
                return objectMapper.treeToValue(node, ArrayItem.class);
            }

            if (StackItemType.MAP.jsonValue().equals(type)) {
                Iterator<JsonNode> values = node.withArray("value").elements();

                HashMap<String, Item> parameters = new HashMap<>();

                while (values.hasNext()) {
                    JsonNode enode = values.next();
                    JsonNode keyNode = enode.get("key");
                    JsonNode valueNode = enode.get("value");

                    Item keyParam = objectMapper.treeToValue(keyNode, Item.class);
                    String key = StackItemParser.readString(keyParam);

                    Item value = objectMapper.treeToValue(valueNode, Item.class);

                    parameters.put(key, value);
                }

                return new MapItem(parameters);
            }

            // else parse as regular parameter
            return objectMapper.treeToValue(node, Item.class);

        } catch (Exception e) {
            log.error("Cannot parse notification json", e);
        }

        return null;
    }

}