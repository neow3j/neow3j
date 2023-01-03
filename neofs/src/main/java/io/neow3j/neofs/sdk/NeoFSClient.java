package io.neow3j.neofs.sdk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.InvalidProtocolBufferException;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.neofs.lib.NeoFSLib;
import io.neow3j.neofs.lib.NeoFSLibInterface;
import io.neow3j.neofs.lib.responses.NeoFSLibError;
import io.neow3j.neofs.lib.responses.PointerResponse;
import io.neow3j.neofs.lib.responses.Response;
import io.neow3j.neofs.lib.responses.ResponseType;
import io.neow3j.neofs.lib.responses.StringResponse;
import io.neow3j.neofs.sdk.accounting.Accounting;
import io.neow3j.neofs.sdk.container.Container;
import io.neow3j.neofs.sdk.dto.EndpointResponse;
import io.neow3j.neofs.sdk.exceptions.NeoFSClientException;
import io.neow3j.neofs.sdk.exceptions.UnexpectedResponseTypeException;
import io.neow3j.neofs.sdk.netmap.Netmap;
import io.neow3j.neofs.sdk.object.NeoFSObject;
import io.neow3j.utils.Numeric;
import io.neow3j.wallet.Account;
import neo.fs.v2.netmap.Types;

import java.io.IOException;
import java.util.List;

import static io.neow3j.neofs.lib.NeoFSLibUtils.getResponseBytes;
import static java.lang.String.format;

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
     * Creates a new NeoFSLib instance with an already loaded native library.
     *
     * @param lib      the loaded library.
     * @param account  the account used for signing when interacting with NeoFS.
     * @param endpoint the gRPC endpoint of the NeoFS node to connect to.
     */
    public static NeoFSClient initialize(NeoFSLib lib, Account account, String endpoint) {
        String clientId = createClient(lib, account, endpoint);
        return new NeoFSClient(lib, clientId);
    }

    /**
     * Loads the native library and initializes a new NeoFSLib instance.
     *
     * @param account  the account used for signing when interacting with NeoFS.
     * @param endpoint the gRPC endpoint of the NeoFS node to connect to.
     * @return an initialized NeoFSClient instance.
     * @throws Exception if there was a problem when loading the native library.
     */
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

    private static String getPrivateKeyForNativeLib(ECKeyPair ecKeyPair) {
        return Numeric.toHexStringNoPrefix(ecKeyPair.getPrivateKey().getBytes());
    }

    public static String getPrivateKeyForNativeLib(Account account) {
        return getPrivateKeyForNativeLib(account.getECKeyPair());
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
     * @throws IOException if the endpoint bytes could not be mapped to an {@link EndpointResponse} object.
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
     * @throws NeoFSLibError                   if the shared library returns an error.
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
     * @throws NeoFSLibError                   if the shared library returns an error.
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
     */
    public boolean deleteContainer(String containerId) {
        return Container.deleteContainer(nativeLib, clientId, containerId);
    }

    /**
     * Gets the {@code containerId}s of the containers that are owned by the provided public key.
     *
     * @param ownerPubKey the owner public key.
     * @return the ids of the owned container.
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

    public static <T> T readJson(String response, Class<T> clazz) throws IOException {
        return new ObjectMapper().readValue(response, clazz);
    }

    public static void throwIfLibError(Response response) {
        if (response.isError()) {
            NeoFSLibError error = response.getError();
            throw new NeoFSClientException(
                    format("The native Lib returned an error with message '%s'.", error.getMessage()));
        }
    }

    public static void throwIfUnexpectedResponseType(Response response, ResponseType expectedType) {
        if (!response.isResponseType(expectedType)) {
            if (response instanceof StringResponse) {
                String responseString = ((StringResponse) response).value;
                throw new UnexpectedResponseTypeException(expectedType, response.type, responseString);
            } else {
                throw new UnexpectedResponseTypeException(expectedType, response.type);
            }
        }
    }

    //endregion helper

}
