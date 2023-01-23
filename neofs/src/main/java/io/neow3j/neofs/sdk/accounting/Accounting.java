package io.neow3j.neofs.sdk.accounting;

import com.google.protobuf.InvalidProtocolBufferException;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.neofs.lib.NeoFSLibInterface;
import io.neow3j.neofs.lib.responses.PointerResponse;
import io.neow3j.neofs.lib.responses.ExpectedResponseType;
import neo.fs.v2.accounting.Types;

import static io.neow3j.neofs.lib.NeoFSLibUtils.getResponseBytes;
import static io.neow3j.neofs.sdk.NeoFSClient.throwIfUnexpectedResponseType;

public class Accounting {

    /**
     * Gets the NeoFS balance for the provided account with {@code pubKey}.
     *
     * @param nativeLib the native lib.
     * @param clientId  the client id.
     * @param pubKey    the public key.
     * @return the NeoFS balance.
     * @throws InvalidProtocolBufferException if the response bytes cannot be converted to the decimal protobuf type.
     */
    public static Types.Decimal getBalance(NeoFSLibInterface nativeLib, String clientId, ECKeyPair.ECPublicKey pubKey)
            throws InvalidProtocolBufferException {

        PointerResponse response = nativeLib.GetBalance(clientId, pubKey.getEncodedCompressedHex());
        throwIfUnexpectedResponseType(response, ExpectedResponseType.DECIMAL);
        byte[] responseBytes = getResponseBytes(response);
        return neo.fs.v2.accounting.Types.Decimal.parseFrom(responseBytes);
    }

}
