package io.neow3j.protocol.core.response;

import io.neow3j.protocol.core.Response;
import io.neow3j.protocol.exceptions.RpcResponseErrorException;

import java.math.BigInteger;

public class NeoGetTransactionHeight extends Response<BigInteger> {

    private static final int LEGACY_NOT_FOUND = -100;

    private static final int UNKNOWN_TRANSACTION = -103;

    /**
     * @return true if the node reports that the transaction is unknown. Older nodes use a shared error code
     * and identify the missing resource in the message.
     */
    public boolean reportsUnknownTransaction() {
        Error error = getError();
        return error != null && (error.getCode() == UNKNOWN_TRANSACTION
                || (error.getCode() == LEGACY_NOT_FOUND && "Unknown transaction".equals(error.getMessage())));
    }

    /**
     * @return the result.
     * @throws RpcResponseErrorException if the Neo node returned an error.
     */
    public BigInteger getHeight() {
        return getResult();
    }

}
