package io.neow3j.neofs.sdk.container;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.neofs.lib.NeoFSLibInterface;
import io.neow3j.neofs.lib.responses.NeoFSLibError;
import io.neow3j.neofs.lib.responses.PointerResponse;
import io.neow3j.neofs.lib.responses.ResponseType;
import io.neow3j.neofs.lib.responses.StringResponse;
import io.neow3j.neofs.sdk.dto.ContainerListResponse;
import io.neow3j.neofs.sdk.exceptions.UnexpectedResponseTypeException;
import neo.fs.v2.container.Types;

import java.io.IOException;
import java.util.List;

import static io.neow3j.neofs.lib.NeoFSLibUtils.getBoolean;
import static io.neow3j.neofs.lib.NeoFSLibUtils.getResponseBytes;
import static io.neow3j.neofs.sdk.NeoFSClient.readJson;
import static io.neow3j.neofs.sdk.NeoFSClient.throwIfLibError;
import static io.neow3j.neofs.sdk.NeoFSClient.throwIfUnexpectedResponseType;

public class Container {

    /**
     * Creates a container.
     * <p>
     * The provided container must contain an ownerId.
     *
     * @param nativeLib     the native lib.
     * @param clientId      the client id.
     * @param container the container.
     * @return the container id.
     * @throws InvalidProtocolBufferException  if the container protobuf type cannot be converted to JSON format.
     * @throws NeoFSLibError                   if the shared library returns an error.
     * @throws UnexpectedResponseTypeException if the type of the shared library's response is unexpected.
     */
    public static String createContainer(NeoFSLibInterface nativeLib, String clientId, Types.Container container)
            throws InvalidProtocolBufferException {

        String containerJson = JsonFormat.printer().print(container);
        StringResponse response = nativeLib.PutContainer(clientId, containerJson);
        throwIfLibError(response);
        throwIfUnexpectedResponseType(response, ResponseType.CONTAINER_ID);
        return response.value;
    }

    /**
     * Gets the container with {@code containerId}.
     *
     * @param nativeLib     the native lib.
     * @param clientId      the client id.
     * @param containerId the container id.
     * @return the container.
     * @throws NeoFSLibError                   if the shared library returns an error.
     * @throws UnexpectedResponseTypeException if the type of the shared library's response is unexpected.
     * @throws InvalidProtocolBufferException  if the response bytes cannot be converted to the container protobuf
     *                                         type.
     */
    public static Types.Container getContainer(NeoFSLibInterface nativeLib, String clientId, String containerId)
            throws InvalidProtocolBufferException {

        PointerResponse response = nativeLib.GetContainer(clientId, containerId);
        throwIfLibError(response);
        throwIfUnexpectedResponseType(response, ResponseType.CONTAINER);
        return Types.Container.parseFrom(getResponseBytes(response));
    }

    /**
     * Deletes a container with {@code containerId}.
     *
     * @param nativeLib     the native lib.
     * @param clientId      the client id.
     * @param containerId the container id.
     */
    public static boolean deleteContainer(NeoFSLibInterface nativeLib, String clientId, String containerId) {
        PointerResponse response = nativeLib.DeleteContainer(clientId, containerId);
        throwIfLibError(response);
        throwIfUnexpectedResponseType(response, ResponseType.BOOLEAN);
        return getBoolean(response);
    }

    /**
     * Gets the {@code containerId}s of the containers that are owned by the provided public key.
     *
     * @param nativeLib     the native lib.
     * @param clientId      the client id.
     * @param ownerPubKey the owner public key.
     * @return the ids of the owned container.
     */
    public static List<String> listContainers(NeoFSLibInterface nativeLib, String clientId,
            ECKeyPair.ECPublicKey ownerPubKey) throws IOException {

        PointerResponse response = nativeLib.ListContainer(clientId, ownerPubKey.getEncodedCompressedHex());
        throwIfLibError(response);
        throwIfUnexpectedResponseType(response, ResponseType.CONTAINER_LIST);
        String containerListJson = new String(getResponseBytes(response));
        ContainerListResponse respDTO = readJson(containerListJson, ContainerListResponse.class);
        return respDTO.getContainerIDs();
    }

}
