package io.neow3j.protocol.core.response;

import io.neow3j.protocol.core.Response;
import io.neow3j.protocol.exceptions.RpcResponseErrorException;

import java.math.BigInteger;

public class NeoGetTransactionHeight extends Response<BigInteger> {

    /**
     * @return the result.
     * @throws RpcResponseErrorException if the Neo node returned an error.
     */
    public BigInteger getHeight() {
        return getResult();
    }

}
