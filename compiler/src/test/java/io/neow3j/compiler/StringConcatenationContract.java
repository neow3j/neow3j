package io.neow3j.compiler;

import io.neow3j.devpack.StorageContext;

// Dummy Java file for StringConcatenationTest. This is required to mock an existing .java file for the AsmHelper class.
// However, for the tests the precompiled .class files are used. To emphasize this, the methods in this class are
// native, hence, do not contain an implementation.
public class StringConcatenationContract {

    private static StorageContext ctx;
//    private static final StorageContext ctx = Storage.getStorageContext();

    public static native String getSomething(String key);
//    public static String getSomething(String key) {
//        return Storage.getString(ctx, key) + "foo" + get();
//    }

    private static native String get();
//    private static String get() {
//        return "hello";
//    }

    public static native void putSomething(String key, String value);
//    public static void putSomething(String key, String value) {
//        Storage.put(ctx, key, value);
//    }

}
