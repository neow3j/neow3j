package io.neow3j.neofs.sdk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import io.neow3j.crypto.Base58;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.neofs.lib.NeoFSLib;
import io.neow3j.neofs.lib.NeoFSLibInterface;
import io.neow3j.neofs.lib.responses.PointerResponse;
import io.neow3j.neofs.lib.responses.Response;
import io.neow3j.neofs.lib.responses.ResponseType;
import io.neow3j.neofs.lib.responses.StringResponse;
import io.neow3j.neofs.sdk.dto.EndpointResponse;
import io.neow3j.neofs.sdk.exceptions.NeoFSLibraryError;
import io.neow3j.neofs.sdk.exceptions.UnexpectedResponseTypeException;
import io.neow3j.utils.Numeric;
import io.neow3j.wallet.Account;
import neo.fs.v2.netmap.Types;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static io.neow3j.neofs.lib.NeoFSLibUtils.getBoolean;
import static io.neow3j.neofs.lib.NeoFSLibUtils.getResponseBytes;

public class NeoFSClient {

    private final String clientId;
    private final NeoFSLibInterface nativeLib;
    private final NeoFSLib neoFSLib;

    private final ObjectMapper mapper = new ObjectMapper();
    private final int CONTAINER_ID_LENGTH = 32;

    public NeoFSClient(NeoFSLib lib, String clientId) {
        this.neoFSLib = lib;
        this.nativeLib = lib.getNativeLib();
        this.clientId = clientId;
    }

    /**
     * Creates a new NeoFSLib instance with an already loaded native library.
     *
     * @param lib the loaded library.
     */
    public static NeoFSClient initialize(NeoFSLib lib, Account account, String endpoint) {
        String clientId = createClient(lib, account, endpoint);
        return new NeoFSClient(lib, clientId);
    }

    public static NeoFSClient loadAndInitialize(Account account, String endpoint) throws Exception {
        return initialize(new NeoFSLib(), account, endpoint);
    }

    public NeoFSLib getNeoFSLib() {
        return neoFSLib;
    }

    public NeoFSLibInterface getNativeLib() {
        return neoFSLib.getNativeLib();
    }

    public String getClientId() {
        return clientId;
    }

    /**
     * Creates a client to interace with NeoFS.
     *
     * @param account       the account used to sign requests made with this client.
     * @param neofsEndpoint the NeoFS endpoint requests are sent to with this client.
     * @return the client id.
     */
    private static String createClient(NeoFSLib lib, Account account, String neofsEndpoint) {
        String privateKeyHex = Numeric.toHexStringNoPrefix(account.getECKeyPair().getPrivateKey().getBytes());
        PointerResponse response = lib.getNativeLib().CreateClient(privateKeyHex, neofsEndpoint);
        return new String(getResponseBytes(response));
    }

    //region client

    // Todo: Delete a client
//    /**
//     * Deletes this client in memory.
//     * <p>
//     * Note, that after calling this method this NeoFSClient instance will no longer be able to issue requests.
//     */
//    public void deleteClient() {
//        nativeLib.DeleteClient(clientId);
//    }

    //endregion client
    //region accounting

    /**
     * Gets the NeoFS balance for the provided account with {@code pubKey}.
     *
     * @param pubKey the public key.
     * @return the NeoFS balance.
     * @throws InvalidProtocolBufferException if the response bytes cannot be converted to the decimal protobuf type.
     */
    public neo.fs.v2.accounting.Types.Decimal getBalance(ECKeyPair.ECPublicKey pubKey) throws Exception {
        PointerResponse response = nativeLib.GetBalance(clientId, pubKey.getEncodedCompressedHex());
        throwIfUnexpectedResponseType(response, ResponseType.DECIMAL);
        byte[] responseBytes = getResponseBytes(response);
        return neo.fs.v2.accounting.Types.Decimal.parseFrom(responseBytes);
    }

    //endregion accounting
    //region netmap

    /**
     * Gets the endpoint this client is interfacing with.
     *
     * @return the endpoint.
     * @throws IOException if the endpoint bytes could not be mapped to an {@link EndpointResponse} object.
     */
    public EndpointResponse getEndpoint() throws Exception {
        PointerResponse response = nativeLib.GetEndpoint(clientId);
        throwIfUnexpectedResponseType(response, ResponseType.ENDPOINT);
        String endpointJson = new String(getResponseBytes(response));
        return readJson(endpointJson, EndpointResponse.class);
    }

    /**
     * Gets the network information this client is interfacing with.
     *
     * @return the network information.
     * @throws InvalidProtocolBufferException if the response bytes cannot be converted to the network info protobuf
     *                                        type.
     */
    public Types.NetworkInfo getNetworkInfo()
            throws InvalidProtocolBufferException, UnexpectedResponseTypeException, NeoFSLibraryError {
        PointerResponse response = nativeLib.GetNetworkInfo(clientId);
        throwIfUnexpectedResponseType(response, ResponseType.NETWORK);
        return Types.NetworkInfo.parseFrom(getResponseBytes(response));
    }

    //endregion netmap
    //region container

    /**
     * Creates a container.
     * <p>
     * The provided container must contain an ownerId.
     *
     * @param container the container.
     * @return the container id.
     * @throws InvalidProtocolBufferException  if the container protobuf type cannot be converted to JSON format.
     * @throws NeoFSLibraryError               if the shared library returns an error.
     * @throws UnexpectedResponseTypeException if the type of the shared library's response is unexpected.
     */
    public String createContainer(neo.fs.v2.container.Types.Container container)
            throws InvalidProtocolBufferException, NeoFSLibraryError, UnexpectedResponseTypeException {
        String containerJson = JsonFormat.printer().print(container);
        StringResponse response = nativeLib.PutContainer(clientId, containerJson);
        throwIfUnexpectedResponseType(response, ResponseType.CONTAINER_ID);
        return response.value;
    }

    /**
     * Gets the container with {@code containerId}.
     *
     * @param containerId the container id.
     * @return the container.
     * @throws NeoFSLibraryError               if the shared library returns an error.
     * @throws UnexpectedResponseTypeException if the type of the shared library's response is unexpected.
     * @throws InvalidProtocolBufferException  if the response bytes cannot be converted to the container protobuf
     *                                         type.
     */
    public neo.fs.v2.container.Types.Container getContainer(String containerId)
            throws NeoFSLibraryError, UnexpectedResponseTypeException, InvalidProtocolBufferException {
        PointerResponse response = nativeLib.GetContainer(clientId, containerId);
        throwIfUnexpectedResponseType(response, ResponseType.CONTAINER);
        return neo.fs.v2.container.Types.Container.parseFrom(getResponseBytes(response));
    }

    /**
     * Deletes a container with {@code containerId}.
     *
     * @param containerId the container id.
     */
    public boolean deleteContainer(String containerId) throws NeoFSLibraryError, UnexpectedResponseTypeException {
        PointerResponse response = nativeLib.DeleteContainer(clientId, containerId);
        throwIfUnexpectedResponseType(response, ResponseType.BOOLEAN);
        return getBoolean(response);
    }

    /**
     * Gets the {@code containerId}s of the containers that are owned by the provided public key.
     *
     * @param ownerPubKey the owner public key.
     * @return the ids of the owned container.
     */
    public List<String> listContainers(ECKeyPair.ECPublicKey ownerPubKey)
            throws InvalidProtocolBufferException {
        // Todo: Consider using the protobuf type for ContainerIDs and providing helper method to retrieve string
        //  values.
        PointerResponse response = nativeLib.ListContainer(clientId, ownerPubKey.getEncodedCompressedHex());
        int n = response.length / CONTAINER_ID_LENGTH;
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            String idBytes = Base58.encode(response.value.getByteArray(i * CONTAINER_ID_LENGTH, CONTAINER_ID_LENGTH));
            list.add(idBytes);
        }
        return list;
    }

    //endregion container
    //region object

    // Todo: Write Object
//    // returns writerId
//    public String initializeObjectWriter() {
//        return null;
//    }
//
//    public void writeObject(String writerId) {
//    }
//
//    public void closeObjectWriter(String writerId) {
//    }

    // Todo: Read Object
//    // returns readerId
//    public void initializeObjectReader() {
//    }
//
//    // returns read bytes
//    public byte[] readObject(String readerId) {
//        return null;
//    }
//
//    public void closeObjectReader() {
//    }

    // Todo: Delete Object
    // returns read tombstone id
//    public String deleteObject(String containerId, String objectId, neo.fs.v2.session.Types.SessionToken sessionToken,
//            neo.fs.v2.acl.Types.BearerToken bearerToken) {
//        return null;
//    }

    //endregion object
    //region helper

    private void throwIfUnexpectedResponseType(Response response, ResponseType expectedType)
            throws UnexpectedResponseTypeException, NeoFSLibraryError {

        if (response.isType(ResponseType.ERROR)) {
            if (response instanceof StringResponse) {
                String errorMsg = ((StringResponse) response).value;
                throw new NeoFSLibraryError(errorMsg);
            } else if (response instanceof PointerResponse) {
                PointerResponse pointerResponse = (PointerResponse) response;
                String errorMsg = new String((pointerResponse).value.getByteArray(0, (pointerResponse).length));
                throw new NeoFSLibraryError(errorMsg);
            } else {
                throw new NeoFSLibraryError("Shared library returned an error that could not be read.");
            }
        }
        if (!response.isType(expectedType)) {
            throw new UnexpectedResponseTypeException(expectedType, response.getType());
        }
    }

    private <T> T readJson(String response, Class<T> clazz) throws IOException {
        return mapper.readValue(response, clazz);
    }

    //endregion helper

}
