package io.neow3j.devpack.template;

import io.neow3j.devpack.framework.SmartContract;
import io.neow3j.devpack.framework.Storage;
import io.neow3j.devpack.framework.annotations.EntryPoint;
import io.neow3j.devpack.framework.annotations.ManifestExtra;
import io.neow3j.devpack.framework.annotations.ManifestFeature;

@ManifestExtra(key = "author", value = "AxLabs")
@ManifestExtra(key = "name", value = "HelloWorld")
@ManifestExtra(key = "description", value = "Hello world contract")
@ManifestFeature(hasStorage = true)
public class ExampleContract extends SmartContract {

    @EntryPoint
    public static byte[] entryPoint(String key, long value) {
        long l = 9_223_372_036_854_775_807L;
        byte[] current = Storage.get(key);
        Storage.put(key, value);
        return current;
    }

}
