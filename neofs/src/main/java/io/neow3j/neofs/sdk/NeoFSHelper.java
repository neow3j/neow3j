package io.neow3j.neofs.sdk;

import com.google.protobuf.ByteString;
import io.neow3j.crypto.Base58;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.utils.Numeric;
import io.neow3j.wallet.Account;
import neo.fs.v2.refs.Types;

import java.nio.ByteBuffer;
import java.util.UUID;

public class NeoFSHelper {

    // region cgo

    public static String getPrivateKeyForNativeLib(ECKeyPair ecKeyPair) {
        return Numeric.toHexStringNoPrefix(ecKeyPair.getPrivateKey().getBytes());
    }

    public static String getPrivateKeyForNativeLib(Account account) {
        return getPrivateKeyForNativeLib(account.getECKeyPair());
    }

    // endregion
    // region neofs-api

    public static Types.OwnerID createOwnerId(Account account) {
        return Types.OwnerID.newBuilder()
                .setValue(ByteString.copyFrom(Base58.decode(account.getAddress())))
                .build();
    }

    public static Types.Version createVersion() {
        return neo.fs.v2.refs.Types.Version.newBuilder()
                .setMajor(2)
                .setMinor(11)
                .build();
    }

    public static ByteString createNonce() {
        UUID uuid = UUID.randomUUID();
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return ByteString.copyFrom(bb.array());
    }

    // endregion

}
