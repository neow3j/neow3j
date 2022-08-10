package io.neow3j.test;

import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.List;
import io.neow3j.devpack.Runtime;
import io.neow3j.devpack.Storage;
import io.neow3j.devpack.StorageContext;
import io.neow3j.devpack.annotations.ManifestExtra;
import io.neow3j.devpack.annotations.OnDeployment;

@SuppressWarnings("unchecked")
@ManifestExtra(key = "name", value = "ExampleContractOne")
public class TestContract1 {

    static final String INTEGER_VALUE_KEY = "value";
    static final String PARENT_CONTRACT_KEY = "parent";
    static final String OWNER_KEY = "owner";
    static StorageContext ctx = Storage.getStorageContext();

    @OnDeployment
    public static void deploy(Object data, boolean update) throws Exception {
        if (!update) {
            List<Object> params = (List<Object>) data;
            Storage.put(ctx, INTEGER_VALUE_KEY, (int) params.get(0));
            Storage.put(ctx, OWNER_KEY, (Hash160) params.get(1));
        }
    }

    public static boolean initialize() {
        if (!Runtime.checkWitness(new Hash160(Storage.get(ctx, OWNER_KEY)))) {
            return false;
        }
        Storage.put(ctx, PARENT_CONTRACT_KEY, Runtime.getCallingScriptHash());
        return true;
    }

    public static int getInt() {
        return Storage.getInt(ctx, INTEGER_VALUE_KEY);
    }

    public static ByteString getParentContract() {
        return Storage.get(ctx, PARENT_CONTRACT_KEY);
    }

    public static ByteString getOwner() {
        return Storage.get(ctx, OWNER_KEY);
    }

}
