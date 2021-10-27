package io.neow3j.test;

import io.neow3j.devpack.Storage;
import io.neow3j.devpack.StorageContext;
import io.neow3j.devpack.annotations.ManifestExtra;
import io.neow3j.devpack.annotations.OnDeployment;

@ManifestExtra(key = "name", value = "ExampleContractOne")
public class ExampleContract1 {

    static final byte[] KEY = new byte[]{1};
    static StorageContext ctx = Storage.getStorageContext();

    @OnDeployment
    public static void deploy(Object data, boolean update) throws Exception {
        if (!update) {
            Storage.put(ctx, KEY, (int) data);
        }
    }

    public static int getInt() {
        return Storage.getInteger(ctx, KEY);
    }
}
