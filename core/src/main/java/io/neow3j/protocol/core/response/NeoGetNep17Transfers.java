package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.neow3j.protocol.exceptions.RpcResponseErrorException;
import io.neow3j.types.Hash160;
import io.neow3j.types.Hash256;

import java.math.BigInteger;
import java.util.List;

public class NeoGetNep17Transfers extends NeoGetTokenTransfers<NeoGetNep17Transfers.Nep17Transfers> {

    /**
     * @return the result.
     * @throws RpcResponseErrorException if the Neo node returned an error.
     */
    public Nep17Transfers getNep17Transfers() {
        return getResult();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Nep17Transfers extends NeoGetTokenTransfers.TokenTransfers<Nep17Transfer> {

        public Nep17Transfers() {
        }

        public Nep17Transfers(List<Nep17Transfer> sent, List<Nep17Transfer> received, String transferAddress) {
            super(sent, received, transferAddress);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Nep17Transfer extends NeoGetTokenTransfers.TokenTransfer {

        public Nep17Transfer() {
        }

        public Nep17Transfer(long timestamp, Hash160 assetHash, String transferAddress, BigInteger amount,
                long blockIndex, long transferNotifyIndex, Hash256 txHash) {
            super(timestamp, assetHash, transferAddress, amount, blockIndex, transferNotifyIndex, txHash);
        }

    }

}
