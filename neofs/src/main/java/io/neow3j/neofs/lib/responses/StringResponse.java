package io.neow3j.neofs.lib.responses;

import io.neow3j.neofs.lib.exceptions.NeoFSLibException;

import java.util.Arrays;
import java.util.List;

import static java.lang.String.format;

public class StringResponse extends Response {

    public String value;

    protected List<String> getFieldOrder() {
        return Arrays.asList("type", "value");
    }

    @Override
    public ResponseType getResponseType() {
        try {
            return ResponseType.fromString(type);
        } catch (IllegalArgumentException e) {
            throw new NeoFSLibException(
                    format("No ResponseType match found for '%s'. The response value was '%s'.", type, value));
        }
    }

}
