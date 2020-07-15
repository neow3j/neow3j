package io.neow3j.compiler;

import io.neow3j.devpack.framework.annotations.EntryPoint;
import io.neow3j.devpack.framework.annotations.ManifestExtra;
import io.neow3j.devpack.framework.annotations.ManifestFeature;

@ManifestExtra(key = "author", value = "AxLabs")
@ManifestExtra(key = "name", value = "HelloWorld")
@ManifestExtra(key = "description", value = "Hello world contract")
@ManifestFeature(hasStorage = true)
public class ExampleContract {

    @EntryPoint
    public static byte entryPoint(byte i) {
        return i;
    }

}
