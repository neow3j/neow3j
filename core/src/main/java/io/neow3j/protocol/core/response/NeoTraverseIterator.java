package io.neow3j.protocol.core.response;

import io.neow3j.protocol.core.Response;
import io.neow3j.protocol.core.stackitem.StackItem;

import java.util.List;

public class NeoTraverseIterator extends Response<List<StackItem>> {

    public List<StackItem> getTraverseIterator() {
        return getResult();
    }

}
