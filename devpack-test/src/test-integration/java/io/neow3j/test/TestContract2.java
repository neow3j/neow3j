package io.neow3j.test;

import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.Contract;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.Storage;
import io.neow3j.devpack.StorageContext;
import io.neow3j.devpack.StringLiteralHelper;
import io.neow3j.devpack.annotations.ManifestExtra;
import io.neow3j.devpack.annotations.OnDeployment;
import io.neow3j.devpack.annotations.Permission;
import io.neow3j.devpack.constants.CallFlags;

@ManifestExtra(key = "name", value = "ExampleContractTwo")
@Permission(contract = "<contract_hash>", methods = "*")
public class TestContract2 {

    static final String CHILD_CONTRACT_KEY = "child";
    static StorageContext ctx = Storage.getStorageContext();
    static final Hash160 OWNER = StringLiteralHelper.addressToScriptHash("<owner_address>");

    @OnDeployment
    public static void deploy(Object data, boolean update) throws Exception {
        if (!update) {
            Storage.put(ctx, CHILD_CONTRACT_KEY, (Hash160) data);
            boolean b = (boolean) Contract.call((Hash160) data, "initialize", CallFlags.All,
                    new Object[]{});
            if (!b) {
                throw new Exception("TestContract2: Initializing contract 1 failed.");
            }
        }
    }

    public static ByteString getChildContract() {
        return Storage.get(ctx, CHILD_CONTRACT_KEY);
    }

    public static Hash160 getOwner() {
        return OWNER;
    }

}
