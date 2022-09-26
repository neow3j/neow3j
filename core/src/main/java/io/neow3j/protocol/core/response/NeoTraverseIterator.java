package io.neow3j.protocol.core.response;

import io.neow3j.protocol.core.Response;
import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.protocol.exceptions.RpcResponseErrorException;

import java.util.List;

public class NeoTraverseIterator extends Response<List<StackItem>> {

    /**
     * @return the result.
     * @throws RpcResponseErrorException if the Neo node returned an error.
     */
    public List<StackItem> getTraverseIterator() {
        return getResult();
    }

}
