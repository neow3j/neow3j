package io.neow3j.test;

import io.neow3j.devpack.annotations.ManifestExtra;

@ManifestExtra(key = "name", value = "Example")
public class ExampleContract {

    public static int method() {
        int i = 0;
        i++;
        return i;
    }
}
