package io.neow3j.protocol.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.neow3j.protocol.core.Response;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * A jackson deserializer that sets the rawResponse variable of Response objects.
 */
public class RawResponseDeserializer
        extends StdDeserializer<Response> implements ResolvableDeserializer {

    private final JsonDeserializer<?> defaultDeserializer;

    public RawResponseDeserializer(JsonDeserializer<?> defaultDeserializer) {
        super(Response.class);
        this.defaultDeserializer = defaultDeserializer;
    }

    @Override
    public Response deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        Response deserializedResponse = (Response) defaultDeserializer.deserialize(jp, ctxt);

        deserializedResponse.setRawResponse(getRawResponse(jp));
        return deserializedResponse;
    }

    // Must implement ResolvableDeserializer when modifying BeanDeserializer. Otherwise, deserializing throws a
    // JsonMappingException.
    @Override
    public void resolve(DeserializationContext ctxt) throws JsonMappingException {
        ((ResolvableDeserializer) defaultDeserializer).resolve(ctxt);
    }

    @SuppressWarnings("resource") // Underlying resource is outside of this method's control.
    private String getRawResponse(JsonParser jp) throws IOException {
        final InputStream inputSource = (InputStream) jp.getInputSource();

        if (inputSource == null) {
            return "";
        }

        inputSource.reset();

        return new Scanner(inputSource, StandardCharsets.UTF_8.name()).useDelimiter("\\Z").next();
    }

}
