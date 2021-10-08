package io.neow3j.test;

import io.neow3j.devpack.annotations.ManifestExtra;
import io.neow3j.devpack.annotations.OnDeployment;
import io.neow3j.devpack.events.Event2Args;

@ManifestExtra(key = "name", value = "Example")
public class ExampleContract1 {

    static Event2Args<String, String> onDeploy;

    @OnDeployment
    public static void deploy(Object data, boolean update) throws Exception {
        if (!update) {
            onDeploy.fire("deployed!", (String) data);
        }
    }

    public static int method() {
        int i = 0;
        i++;
        return i;
    }
}
