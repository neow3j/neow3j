package io.neow3j.neofs.lib.responses;

import com.sun.jna.Pointer;

import java.util.Arrays;
import java.util.List;

public class PointerResponse extends Response {

    public Integer length;
    public Pointer value;

    protected List<String> getFieldOrder() {
        return Arrays.asList("type", "length", "value");
    }

}
