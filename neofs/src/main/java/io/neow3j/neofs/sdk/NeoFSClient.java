package io.neow3j.neofs.sdk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.InvalidProtocolBufferException;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.neofs.lib.NeoFSLib;
import io.neow3j.neofs.lib.NeoFSLibInterface;
import io.neow3j.neofs.lib.responses.PointerResponse;
import io.neow3j.neofs.lib.responses.Response;
import io.neow3j.neofs.lib.responses.ExpectedResponseType;
import io.neow3j.neofs.sdk.accounting.Accounting;
import io.neow3j.neofs.sdk.container.Container;
import io.neow3j.neofs.sdk.dto.ContainerListResponse;
import io.neow3j.neofs.sdk.dto.EndpointResponse;
import io.neow3j.neofs.sdk.exceptions.NeoFSClientException;
import io.neow3j.neofs.sdk.exceptions.UnexpectedResponseTypeException;
import io.neow3j.neofs.sdk.netmap.Netmap;
import io.neow3j.neofs.sdk.object.NeoFSObject;
import io.neow3j.wallet.Account;
import neo.fs.v2.netmap.Types;

import java.io.IOException;
import java.util.List;

import static io.neow3j.neofs.lib.NeoFSLibUtils.getResponseBytes;
import static io.neow3j.neofs.sdk.NeoFSHelper.getPrivateKeyForNativeLib;

/**
 * Represents a NeoFSClient in the shared-lib.
 * <p>
 * This class can be used to interact with NeoFS through the provided native library.
 */
public class NeoFSClient {

    private final String clientId;
    private final NeoFSLibInterface nativeLib;
    private final NeoFSLib neoFSLib;

    public NeoFSClient(NeoFSLib lib, String clientId) {
        this.neoFSLib = lib;
        this.nativeLib = lib.getNativeLib();
        this.clientId = clientId;
    }

    /**
     * Creates a new NeoFSClientd instance with an already loaded native library.
     *
     * @param lib      the loaded library.
     * @param account  the account used for signing when interacting with NeoFS.
     * @param endpoint the gRPC endpoint of the NeoFS node to connect to.
     * @return an initialized NeoFSClient instance.
     */
    public static NeoFSClient initialize(NeoFSLib lib, Account account, String endpoint) {
        String clientId = createClient(lib, account, endpoint);
        return new NeoFSClient(lib, clientId);
    }

    /**
     * Loads the native library and initializes a new NeoFSClient instance.
     *
     * @param account  the account used for signing when interacting with NeoFS.
     * @param endpoint the gRPC endpoint of the NeoFS node to connect to.
     * @return an initialized NeoFSClient instance.
     * @throws Exception if there was a problem when loading the native library.
     */
    public static NeoFSClient loadAndInitialize(Account account, String endpoint) throws Exception {
        return initialize(new NeoFSLib(), account, endpoint);
    }

    /**
     * @return the NeoFSLib instance.
     */
    public NeoFSLib getNeoFSLib() {
        return neoFSLib;
    }

    /**
     * @return the native library interface.
     */
    public NeoFSLibInterface getNativeLib() {
        return neoFSLib.getNativeLib();
    }

    /**
     * @return the client id of this NeoFSClient instance.
     */
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
    public static String createClient(NeoFSLib lib, Account account, String neofsEndpoint) {
        String privateKeyHex = getPrivateKeyForNativeLib(account);
        PointerResponse response = lib.getNativeLib().CreateClient(privateKeyHex, neofsEndpoint);
        return new String(getResponseBytes(response));
    }

    //region client

    /**
     * Deletes this client in memory.
     * <p>
     * Note, that after calling this method this NeoFSClient instance will no longer be able to issue requests.
     */
    public boolean deleteClient() {
        PointerResponse response = nativeLib.DeleteClient(clientId);
        byte[] responseBytes = getResponseBytes(response);
        return responseBytes.length == 1 && responseBytes[0] == 1;
    }

    //endregion client
    //region accounting

    /**
     * Gets the NeoFS balance for the provided account with {@code pubKey}.
     *
     * @param pubKey the public key.
     * @return the NeoFS balance.
     * @throws InvalidProtocolBufferException if the response bytes cannot be converted to the decimal protobuf type.
     */
    public neo.fs.v2.accounting.Types.Decimal getBalance(ECKeyPair.ECPublicKey pubKey)
            throws InvalidProtocolBufferException {
        return Accounting.getBalance(nativeLib, clientId, pubKey);
    }

    //endregion accounting
    //region netmap

    /**
     * Gets the endpoint this client is interfacing with.
     *
     * @return the endpoint.
     * @throws IOException if the endpoint bytes could not be mapped to an {@link EndpointResponse} instance.
     */
    public EndpointResponse getEndpoint() throws IOException {
        return Netmap.getEndpoint(nativeLib, clientId);
    }

    /**
     * Gets the network information this client is interfacing with.
     *
     * @return the network information.
     * @throws InvalidProtocolBufferException if the response bytes cannot be converted to the network info protobuf
     *                                        type.
     */
    public Types.NetworkInfo getNetworkInfo() throws InvalidProtocolBufferException {
        return Netmap.getNetworkInfo(nativeLib, clientId);
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
     * @throws NeoFSClientException            if something went wrong interacting with the shared library.
     * @throws UnexpectedResponseTypeException if the type of the shared library's response is unexpected.
     */
    public String createContainer(neo.fs.v2.container.Types.Container container) throws InvalidProtocolBufferException {
        return Container.createContainer(nativeLib, clientId, container);
    }

    /**
     * Gets the container with {@code containerId}.
     *
     * @param containerId the container id.
     * @return the container.
     * @throws NeoFSClientException            if something went wrong interacting with the shared library.
     * @throws UnexpectedResponseTypeException if the type of the shared library's response is unexpected.
     * @throws InvalidProtocolBufferException  if the response bytes cannot be converted to the container protobuf
     *                                         type.
     */
    public neo.fs.v2.container.Types.Container getContainer(String containerId) throws InvalidProtocolBufferException {
        return Container.getContainer(nativeLib, clientId, containerId);
    }

    /**
     * Deletes a container with {@code containerId}.
     *
     * @param containerId the container id.
     * @return if the container has been deleted successfully.
     */
    public boolean deleteContainer(String containerId) {
        return Container.deleteContainer(nativeLib, clientId, containerId);
    }

    /**
     * Gets the {@code containerId}s of the containers that are owned by the provided public key.
     *
     * @param ownerPubKey the owner public key.
     * @return the ids of the owned container.
     * @throws IOException if the response bytes could not be mapped to an {@link ContainerListResponse} instance.
     */
    public List<String> listContainers(ECKeyPair.ECPublicKey ownerPubKey) throws IOException {
        return Container.listContainers(nativeLib, clientId, ownerPubKey);
    }

    //endregion container
    //region object

    /**
     * Creates an object (i.e., writes the given {@code fileBytes}) in the container with {@code containerId}
     * within a session opened with the given {@code signerAccount}.
     *
     * @param containerId   the container id.
     * @param fileBytes     the file bytes to write.
     * @param signerAccount the signer account.
     * @return the object id.
     */
    public String createObject(String containerId, byte[] fileBytes, Account signerAccount) {
        return NeoFSObject.createObject(nativeLib, clientId, containerId, fileBytes, signerAccount);
    }

    /**
     * Reads the object with {@code objectId} from the container with {@code containerId}.
     *
     * @param containerId   the container id.
     * @param objectId      the object id.
     * @param signerAccount the signer account.
     * @return the object id.
     */
    public byte[] readObject(String containerId, String objectId, Account signerAccount) {
        return NeoFSObject.readObject(nativeLib, clientId, containerId, objectId, signerAccount);
    }

    /**
     * Reads the object's header.
     *
     * @param containerId   the container id.
     * @param objectId      the object id.
     * @param signerAccount the signer account.
     * @return an object without its payload.
     * @throws InvalidProtocolBufferException if the response bytes cannot be converted to the object protobuf type.
     */
    public neo.fs.v2.object.Types.Object getObjectHeader(String containerId, String objectId, Account signerAccount)
            throws InvalidProtocolBufferException {
        neo.fs.v2.object.Types.Object objectHeader = NeoFSObject.getObjectHeader(nativeLib, clientId, containerId,
                objectId, signerAccount);
        return objectHeader;
    }

    /**
     * Deletes the object with {@code objectId} from the container with {@code containerId}.
     *
     * @param containerId   the container id.
     * @param objectId      the object id.
     * @param signerAccount the signer account.
     * @return the tombstone id.
     */
    public String deleteObject(String containerId, String objectId, Account signerAccount) {
        return NeoFSObject.deleteObject(nativeLib, clientId, containerId, objectId, signerAccount);
    }

    //endregion object
    //region helper

    /**
     * Deserializes the provided JSON String to the provided class.
     *
     * @param response the JSON content String.
     * @param clazz    the class to deserialise the response into.
     * @param <T>      the type the response value is mapped to.
     * @return the class.
     * @throws IOException if the given JSON content String could not be deserialized into the provided class.
     */
    public static <T> T readJson(String response, Class<T> clazz) throws IOException {
        return new ObjectMapper().readValue(response, clazz);
    }

    /**
     * Throws an {@link UnexpectedResponseTypeException} if the provided response is an unexpected type.
     *
     * @param response     the response from the shared-lib.
     * @param expectedType the expected type for the response.
     */
    public static void throwIfUnexpectedResponseType(Response response, ExpectedResponseType expectedType) {
        if (!response.isResponseType(expectedType)) {
            throw new UnexpectedResponseTypeException(expectedType, response.type,
                    response.getUnexpectedResponseMessage());
        }
    }

    //endregion helper

}
