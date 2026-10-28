package io.neow3j.compiler;

/**
 * Debug source for the precompiled {@code StringConcatenationContract_*.class} files.
 * <p>
 * Those class files record {@code StringConcatenationContract.java} as their source file name. Debug info
 * looks up {@code io/neow3j/compiler/StringConcatenationContract.java}, so this file has to keep this name.
 * The methods are native because the bytecode the tests transpile is the committed class file. The sources
 * that produce those class files are in {@code compiler/src/test/resources/string-concatenation}.
 */
public class StringConcatenationContract {

    public static native String getSomething(String key);

    private static native String get();

    public static native void putSomething(String key, String value);

}
