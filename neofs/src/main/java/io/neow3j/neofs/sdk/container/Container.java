package io.neow3j.neofs.sdk.container;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.neofs.lib.NeoFSLibInterface;
import io.neow3j.neofs.lib.responses.PointerResponse;
import io.neow3j.neofs.lib.responses.ExpectedResponseType;
import io.neow3j.neofs.lib.responses.StringResponse;
import io.neow3j.neofs.sdk.dto.ContainerListResponse;
import io.neow3j.neofs.sdk.exceptions.NeoFSClientException;
import io.neow3j.neofs.sdk.exceptions.UnexpectedResponseTypeException;
import neo.fs.v2.container.Types;

import java.io.IOException;
import java.util.List;

import static io.neow3j.neofs.lib.NeoFSLibUtils.getBoolean;
import static io.neow3j.neofs.lib.NeoFSLibUtils.getResponseBytes;
import static io.neow3j.neofs.sdk.NeoFSClient.readJson;
import static io.neow3j.neofs.sdk.NeoFSClient.throwIfUnexpectedResponseType;

public class Container {

    /**
     * Creates a container.
     * <p>
     * The provided container must contain an ownerId.
     *
     * @param nativeLib the native lib.
     * @param clientId  the client id.
     * @param container the container.
     * @return the container id.
     * @throws InvalidProtocolBufferException  if the container protobuf type cannot be converted to JSON format.
     * @throws NeoFSClientException            if something went wrong interacting with the shared library.
     * @throws UnexpectedResponseTypeException if the type of the shared library's response is unexpected.
     */
    public static String createContainer(NeoFSLibInterface nativeLib, String clientId, Types.Container container)
            throws InvalidProtocolBufferException {

        String containerJson = JsonFormat.printer().print(container);
        StringResponse response = nativeLib.PutContainer(clientId, containerJson);
        throwIfUnexpectedResponseType(response, ExpectedResponseType.CONTAINER_ID);
        return response.value;
    }

    /**
     * Gets the container with {@code containerId}.
     *
     * @param nativeLib   the native lib.
     * @param clientId    the client id.
     * @param containerId the container id.
     * @return the container.
     * @throws NeoFSClientException            if something went wrong interacting with the shared library.
     * @throws UnexpectedResponseTypeException if the type of the shared library's response is unexpected.
     * @throws InvalidProtocolBufferException  if the response bytes cannot be converted to the container protobuf
     *                                         type.
     */
    public static Types.Container getContainer(NeoFSLibInterface nativeLib, String clientId, String containerId)
            throws InvalidProtocolBufferException {

        PointerResponse response = nativeLib.GetContainer(clientId, containerId);
        throwIfUnexpectedResponseType(response, ExpectedResponseType.CONTAINER);
        return Types.Container.parseFrom(getResponseBytes(response));
    }

    /**
     * Deletes a container with {@code containerId}.
     *
     * @param nativeLib   the native lib.
     * @param clientId    the client id.
     * @param containerId the container id.
     * @return if the container has been deleted successfully.
     */
    public static boolean deleteContainer(NeoFSLibInterface nativeLib, String clientId, String containerId) {
        PointerResponse response = nativeLib.DeleteContainer(clientId, containerId);
        throwIfUnexpectedResponseType(response, ExpectedResponseType.BOOLEAN);
        return getBoolean(response);
    }

    /**
     * Gets the {@code containerId}s of the containers that are owned by the provided public key.
     *
     * @param nativeLib   the native lib.
     * @param clientId    the client id.
     * @param ownerPubKey the owner public key.
     * @return the ids of the owned container.
     * @throws IOException if the response bytes could not be mapped to an {@link ContainerListResponse} instance.
     */
    public static List<String> listContainers(NeoFSLibInterface nativeLib, String clientId,
            ECKeyPair.ECPublicKey ownerPubKey) throws IOException {

        PointerResponse response = nativeLib.ListContainer(clientId, ownerPubKey.getEncodedCompressedHex());
        throwIfUnexpectedResponseType(response, ExpectedResponseType.CONTAINER_LIST);
        String containerListJson = new String(getResponseBytes(response));
        ContainerListResponse respDTO = readJson(containerListJson, ContainerListResponse.class);
        return respDTO.getContainerIDs();
    }

}
