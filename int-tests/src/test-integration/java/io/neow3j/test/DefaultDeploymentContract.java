package io.neow3j.test;

import io.neow3j.devpack.Storage;
import io.neow3j.devpack.annotations.OnDeployment;

public class DefaultDeploymentContract {

    @OnDeployment
    public static void deploy(Object data, boolean update) {
        Storage.put("key", "value");
    }

    public static String getValue() {
        return Storage.getString("key");
    }

}
