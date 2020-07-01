package io.neow3j.devpack.template;

import io.neow3j.devpack.framework.Storage;
import io.neow3j.devpack.framework.annotations.EntryPoint;
import io.neow3j.devpack.framework.annotations.ManifestFeature;
import io.neow3j.devpack.framework.annotations.ManifestExtra;

@ManifestExtra(key = "author", value = "AxLabs")
@ManifestExtra(key = "name", value = "HelloWorld")
@ManifestExtra(key = "description", value = "Hello world contract")
@ManifestFeature(hasStorage = true)
public class HelloWorldContract {

    @EntryPoint
    public static boolean entryPoint(String method, Object[] params) {
        Storage.put("Hello", "World");
        return true;
    }

}