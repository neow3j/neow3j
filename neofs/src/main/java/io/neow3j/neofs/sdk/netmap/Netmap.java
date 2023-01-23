package io.neow3j.neofs.sdk.netmap;

import com.google.protobuf.InvalidProtocolBufferException;
import io.neow3j.neofs.lib.NeoFSLibInterface;
import io.neow3j.neofs.lib.responses.PointerResponse;
import io.neow3j.neofs.lib.responses.ExpectedResponseType;
import io.neow3j.neofs.sdk.dto.EndpointResponse;
import neo.fs.v2.netmap.Types;

import java.io.IOException;

import static io.neow3j.neofs.lib.NeoFSLibUtils.getResponseBytes;
import static io.neow3j.neofs.sdk.NeoFSClient.readJson;
import static io.neow3j.neofs.sdk.NeoFSClient.throwIfUnexpectedResponseType;

public class Netmap {

    /**
     * Gets the endpoint this client is interfacing with.
     *
     * @param nativeLib the native lib.
     * @param clientId  the client id.
     * @return the endpoint.
     * @throws IOException if the endpoint bytes could not be mapped to an {@link EndpointResponse} instance.
     */
    public static EndpointResponse getEndpoint(NeoFSLibInterface nativeLib, String clientId) throws IOException {
        PointerResponse response = nativeLib.GetEndpoint(clientId);
        throwIfUnexpectedResponseType(response, ExpectedResponseType.ENDPOINT);
        String endpointJson = new String(getResponseBytes(response));
        return readJson(endpointJson, EndpointResponse.class);
    }

    /**
     * Gets the network information this client is interfacing with.
     *
     * @param nativeLib the native lib.
     * @param clientId  the client id.
     * @return the network information.
     * @throws InvalidProtocolBufferException if the response bytes cannot be converted to the network info protobuf
     *                                        type.
     */
    public static Types.NetworkInfo getNetworkInfo(NeoFSLibInterface nativeLib, String clientId)
            throws InvalidProtocolBufferException {

        PointerResponse response = nativeLib.GetNetworkInfo(clientId);
        throwIfUnexpectedResponseType(response, ExpectedResponseType.NETWORK);
        return Types.NetworkInfo.parseFrom(getResponseBytes(response));
    }

}
