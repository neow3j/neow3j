package io.neow3j.test;

import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.Storage;
import io.neow3j.devpack.StorageContext;
import io.neow3j.devpack.annotations.OnDeployment;

public class ExampleContract2 {

    static byte[] CONTRACT_HASH_KEY = new byte[]{0};
    static byte[] STORE_VALUE_KEY = new byte[]{1};
    static StorageContext ctx = Storage.getStorageContext();

    @OnDeployment
    public static void deploy(Object data, boolean update) throws Exception {
        if (!update) {
            Storage.put(ctx, CONTRACT_HASH_KEY, (Hash160) data);
        }
    }

    public static void store(String value) {
        Storage.put(ctx, new ByteString("key"), value);
    }

    public static ByteString getStoredContractHash() {
        return Storage.get(ctx, CONTRACT_HASH_KEY);
    }
}
