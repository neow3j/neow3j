package io.neow3j.test;

import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.Storage;
import io.neow3j.devpack.StorageContext;
import io.neow3j.devpack.StringLiteralHelper;
import io.neow3j.devpack.annotations.ManifestExtra;
import io.neow3j.devpack.annotations.OnDeployment;
import io.neow3j.devpack.annotations.Permission;

@ManifestExtra(key = "name", value = "ExampleContractTwo")
@Permission(contract = "<contract_hash>", methods = "*")
public class ExampleContract2 {

    static final byte[] DEPLOYER_KEY = new byte[]{0};
    static StorageContext ctx = Storage.getStorageContext();
    static final Hash160 OWNER = StringLiteralHelper.addressToScriptHash("<owner_address>");

    @OnDeployment
    public static void deploy(Object data, boolean update) throws Exception {
        if (!update) {
            Storage.put(ctx, DEPLOYER_KEY, (Hash160) data);
        }
    }

    public static ByteString getDeployer() {
        return Storage.get(ctx, DEPLOYER_KEY);
    }

    public static Hash160 getOwner() {
        return OWNER;
    }
}
