package io.neow3j.compiler;

import io.neow3j.devpack.annotations.ManifestExtra;
import io.neow3j.devpack.events.Event1Arg;

@ManifestExtra(key = "name", value = "Example")
@ManifestExtra(key = "author", value = "AxLabs")
public class ExampleContract {

//    static Hash160 hash = new Hash160(StringLiteralHelper.hexToBytes(
//            "f563ea40bc283d4d0e05c48ea305b3f2a07340ef"));

//    private static final StorageContext storageContext = Storage.getStorageContext();

    static Event1Arg<Integer> event;

    public static int method(int i) throws Exception {
        if (i <= 0) {
            throw new Exception("Argument must be greater than zero.");
        }
        return i;
    }
}
