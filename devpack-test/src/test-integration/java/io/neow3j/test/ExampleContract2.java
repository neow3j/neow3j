package io.neow3j.test;

import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.Storage;
import io.neow3j.devpack.StorageContext;
import io.neow3j.devpack.annotations.ManifestExtra;
import io.neow3j.devpack.annotations.OnDeployment;

@ManifestExtra(key = "name", value = "ExampleContractTwo")
public class ExampleContract2 {

    static final byte[] OWNER_KEY = new byte[]{0};
    static StorageContext ctx = Storage.getStorageContext();

    @OnDeployment
    public static void deploy(Object data, boolean update) throws Exception {
        if (!update) {
            Storage.put(ctx, OWNER_KEY, (Hash160) data);
        }
    }

    public static ByteString getOwner() {
        return Storage.get(ctx, OWNER_KEY);
    }
}
