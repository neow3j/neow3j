package io.neow3j.neofs.lib.responses;

import java.util.Arrays;
import java.util.List;

public class StringResponse extends Response {

    public String value;

    protected List<String> getFieldOrder() {
        return Arrays.asList("type", "value");
    }

}
