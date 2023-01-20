package io.neow3j.neofs;

import io.neow3j.neofs.sdk.BasicACL;
import io.neow3j.neofs.sdk.NeoFSHelper;
import io.neow3j.wallet.Account;
import neo.fs.v2.container.Types;

import java.util.Date;

public class NeoFSIntegrationTestHelper {

    // Integration tests for the neofs module currently only run with a locally running neofs-aio container setup.
    public static final String neofsEndpoint = "grpc://127.0.0.1:8080";

    protected static Types.Container createSimpleContainer(Account ownerAccount) {
        return Types.Container.newBuilder()
                .setVersion(NeoFSHelper.createVersion())
                .setNonce(NeoFSHelper.createNonce())
                .setOwnerId(NeoFSHelper.createOwnerId(ownerAccount))
                .setBasicAcl(BasicACL.PUBLIC_BASIC_NAME.value())
                .setPlacementPolicy(neo.fs.v2.netmap.Types.PlacementPolicy.newBuilder()
                        .setContainerBackupFactor(0)
                        .addReplicas(neo.fs.v2.netmap.Types.Replica.newBuilder()
                                .setCount(1)
                                .build())
                        .build())
                .addAttributes(Types.Container.Attribute.newBuilder()
                        .setKey("CreatedAt")
                        .setValue(new Date().toString())
                        .build())
                .build();
    }

}
