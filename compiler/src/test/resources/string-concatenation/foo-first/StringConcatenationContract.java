package io.neow3j.compiler;

import io.neow3j.devpack.Storage;

// Compile this file with the Eclipse compiler. Do not edit it, and do not use javac.
// javac 8 compiles this to StringBuilder() and append("foo"). That is not the class file below.
//
// This produces StringConcatenationContract_StringBuilderOneArg_withValueOf.class.
// From the repo root, after ./gradlew :devpack:jar:
//
//   curl -fsSL -o /tmp/ecj.jar https://repo1.maven.org/maven2/org/eclipse/jdt/ecj/3.26.0/ecj-3.26.0.jar
//   mkdir -p /tmp/scc-ecj
//   java -jar /tmp/ecj.jar -g -source 8 -target 8 -cp devpack/build/libs/devpack-3.25.0.jar -d /tmp/scc-ecj compiler/src/test/resources/string-concatenation/foo-first/StringConcatenationContract.java
//
// java --version may be 11. That is the runtime for the jar. -source 8 -target 8 sets the class file.
// -g is required. Without a LocalVariableTable the manifest parameters have no name, and Neo faults while reading the manifest.
// javap -c -p -v /tmp/scc-ecj/io/neow3j/compiler/StringConcatenationContract.class
// must show a LocalVariableTable, ldc of "foo", then StringBuilder."<init>":(Ljava/lang/String;)V, and no String.valueOf.
// Copy that class file to:
//   compiler/src/test/resources/StringConcatenationContract_StringBuilderOneArg_withValueOf.class
//   int-tests/src/test-integration/resources/compiler/StringConcatenationContract_StringBuilderOneArg_withValueOf.class

public class StringConcatenationContract {

    public static String getSomething(String key) {
        return "foo" + Storage.getString(key) + get();
    }

    private static String get() {
        return "hello";
    }

    public static void putSomething(String key, String value) {
        Storage.put(key, value);
    }

}
