package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.protocol.core.Response;
import io.neow3j.protocol.exceptions.RpcResponseErrorException;
import io.neow3j.types.Hash160;
import io.neow3j.types.Hash256;

import java.math.BigInteger;
import java.util.Objects;

public class NeoGetNep11Transfers extends Response<NeoGetNep11Transfers.Nep11Transfers> {

    /**
     * @return the result.
     * @throws RpcResponseErrorException if the Neo node returned an error.
     */
    public Nep11Transfers getNep11Transfers() {
        return getResult();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Nep11Transfers extends NeoGetTokenTransfers.TokenTransfers<Nep11Transfer> {

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Nep11Transfer extends NeoGetTokenTransfers.TokenTransfer {

        @JsonProperty("tokenid")
        private String tokenId;

        public Nep11Transfer() {
        }

        public Nep11Transfer(long timestamp, Hash160 assetHash, String transferAddress, BigInteger amount,
                long blockIndex, long transferNotifyIndex, Hash256 txHash, String tokenId) {
            super(timestamp, assetHash, transferAddress, amount, blockIndex, transferNotifyIndex, txHash);
            this.tokenId = tokenId;
        }

        /**
         * @return the ID of the token involved in this transfer as a hexadecimal string.
         */
        public String getTokenId() {
            return tokenId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Nep11Transfer)) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }
            Nep11Transfer that = (Nep11Transfer) o;
            return Objects.equals(tokenId, that.tokenId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), tokenId);
        }

    }

}
