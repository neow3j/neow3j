package io.neow3j.neofs.lib;

import com.sun.jna.Library;
import io.neow3j.neofs.lib.responses.PointerResponse;
import io.neow3j.neofs.lib.responses.StringResponse;

public interface NeoFSLibInterface extends Library {

    //region client

    PointerResponse CreateClient(String key, String neoFSEndpoint);

    PointerResponse DeleteClient(String clientId);

//    PointerResponse DeleteClient(String clientId);

    //endregion client
    //region accounting

    PointerResponse GetBalance(String clientId, String publicKey);

    //endregion accounting
    //region netmap

    PointerResponse GetEndpoint(String clientId);

    PointerResponse GetNetworkInfo(String clientId);

    //endregion netmap
    //region container

    StringResponse PutContainer(String clientId, String container);

    PointerResponse GetContainer(String clientId, String containerId);

    PointerResponse DeleteContainer(String clientId, String containerId);

//    PointerResponse DeleteContainerWithinSession(String clientId, String containerId, String sessionToken);

    PointerResponse ListContainer(String clientId, String ownerPubKeyEncodedCompressedHex);

//    PointerResponse SetExtendedACL(String clientId, String table);

//    PointerResponse GetExtendedACL(String clientId, String containerId);

    //endregion container

    //region object

//    PointerResponse GetObjectInit(String clientId, String containerId);

    StringResponse CreateObjectWithoutAttributes(String clientId, String containerId, byte[] fileBytes, int fileSize,
            String sessionSignerPrivateKey);

    PointerResponse ReadObject(String clientId, String cointainerId, String objectId, String signerPrivateKey);

    PointerResponse GetObjectHead(String clientId, String cointainerId, String objectId, String signerPrivateKey);

    StringResponse DeleteObject(String clientId, String cointainerId, String objectId, String signerPrivateKey);

//    PointerResponse PutObjectInit(String clientId, String containerId);

//    PointerResponse DeleteObject(String clientId, String containerId, String objectId, String sessionToken,
//            String bearerToken);

//    PointerResponse GetObjectHead(String clientId, String containerId, String objectId, String sessionToken,
//            String bearerToken);

//    PointerResponse SearchObject(String clientId, String containerId, String sessionToken, String bearerToken,
//            String filters);

//    PointerResponse GetRange(String clientId, String containerId, String sessionToken, String bearerToken,
//            String filters, int length, int offset);

//    PointerResponse GetRangeHash();

    //endregion object
    //region session

//    PointerResponse CreateSession(String clientId, int expiration);

    //endregion session
    //region reputation

//    PointerResponse AnnounceLocalTrust(String clientId, List<Object> trusts, int epoch);

//    PointerResponse AnnounceIntermediateResult(String clientId, Object[] trusts, int epoch, int iteration);

    //endregion reputation

}
