package io.neow3j.protocol.core.response;

import io.neow3j.protocol.core.Response;
import io.neow3j.protocol.exceptions.RpcResponseErrorException;

public class NeoCancelTransaction extends Response<Transaction> {

    /**
     * Gets the transaction object returned by the RPC method. This represents the transaction sent to cancel the other
     * transaction.
     *
     * @return the transaction.
     * @throws RpcResponseErrorException if the Neo node returned an error.
     */
    public Transaction getTransaction() {
        return getResult();
    }
}
