package io.neow3j.protocol.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.neow3j.protocol.core.ContractParameterParser;
import io.neow3j.protocol.core.methods.response.notification.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

public class NotificationStateDeserializer extends StdDeserializer<State> {
    private static final Logger log = LoggerFactory.getLogger(NotificationStateDeserializer.class);

    private ObjectMapper objectMapper;

    protected NotificationStateDeserializer() {
        this(null);
    }

    protected NotificationStateDeserializer(Class<?> vc) {
        super(vc);
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public State deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

        JsonNode node = p.getCodec().readTree(p);
        String type = node.get("type").asText();

        try {
            if (NotificationStateType.ARRAY.jsonValue().equals(type)) {
                return objectMapper.treeToValue(node, ArrayState.class);
            }

            if (NotificationStateType.MAP.jsonValue().equals(type)) {
                Iterator<JsonNode> values = node.withArray("value").elements();

                HashMap<String, NotificationParameter> parameters = new HashMap<>();

                while (values.hasNext()) {
                    JsonNode enode = values.next();
                    JsonNode keyNode = enode.get("key");
                    JsonNode valueNode = enode.get("value");

                    NotificationParameter keyParam = objectMapper.treeToValue(keyNode, NotificationParameter.class);
                    String key = ContractParameterParser.readString(keyParam);

                    NotificationParameter value = objectMapper.treeToValue(valueNode, NotificationParameter.class);

                    parameters.put(key, value);
                }

                return new MapState(parameters);
            }

        } catch (Exception e) {
            log.error("Cannot parse notification json", e);
        }

        return null;
    }

}