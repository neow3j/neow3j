package io.neow3j.neofs.sdk.object;

import io.neow3j.neofs.lib.NeoFSLibInterface;
import io.neow3j.neofs.lib.responses.PointerResponse;
import io.neow3j.neofs.lib.responses.ResponseType;
import io.neow3j.neofs.lib.responses.StringResponse;
import io.neow3j.wallet.Account;

import static io.neow3j.neofs.lib.NeoFSLibUtils.getResponseBytes;
import static io.neow3j.neofs.sdk.NeoFSClient.getPrivateKeyForNativeLib;
import static io.neow3j.neofs.sdk.NeoFSClient.throwIfLibError;
import static io.neow3j.neofs.sdk.NeoFSClient.throwIfUnexpectedResponseType;

public class NeoFSObject {

    /**
     * Creates an object (i.e., writes the given {@code fileBytes}) in the container with {@code containerId}
     * within a session opened with the given {@code signerAccount}.
     *
     * @param nativeLib     the native lib.
     * @param clientId      the client id.
     * @param containerId   the container id.
     * @param fileBytes     the file bytes to write.
     * @param signerAccount the signer account.
     * @return the object id.
     */
    public static String createObject(NeoFSLibInterface nativeLib, String clientId, String containerId,
            byte[] fileBytes, Account signerAccount) {

        String signerPrivateKey = getPrivateKeyForNativeLib(signerAccount);
        StringResponse response = nativeLib.CreateObjectWithoutAttributes(clientId, containerId, fileBytes,
                fileBytes.length, signerPrivateKey);
        throwIfLibError(response);
        throwIfUnexpectedResponseType(response, ResponseType.OBJECT_ID);
        return response.value;
    }

    /**
     * Reads the object with {@code objectId} from the container with {@code containerId}.
     *
     * @param nativeLib     the native lib.
     * @param clientId      the client id.
     * @param containerId   the container id.
     * @param objectId      the object id.
     * @param signerAccount the signer account.
     * @return the object id.
     */
    public static byte[] readObject(NeoFSLibInterface nativeLib, String clientId, String containerId, String objectId,
            Account signerAccount) {
        String signerPrivateKey = getPrivateKeyForNativeLib(signerAccount);
        PointerResponse response = nativeLib.ReadObject(clientId, containerId, objectId, signerPrivateKey);
        throwIfLibError(response);
        throwIfUnexpectedResponseType(response, ResponseType.OBJECT);
        return getResponseBytes(response);
    }

    /**
     * Deletes the object with {@code objectId} from the container with {@code containerId}.
     *
     * @param nativeLib     the native lib.
     * @param clientId      the client id.
     * @param containerId   the container id.
     * @param objectId      the object id.
     * @param signerAccount the signer account.
     * @return the tombstone id.
     */
    public static String deleteObject(NeoFSLibInterface nativeLib, String clientId, String containerId, String objectId,
            Account signerAccount) {

        String signerPrivateKey = getPrivateKeyForNativeLib(signerAccount);
        StringResponse response = nativeLib.DeleteObject(clientId, containerId, objectId, signerPrivateKey);
        throwIfLibError(response);
        throwIfUnexpectedResponseType(response, ResponseType.OBJECT_ID);
        return response.value;
    }

}
