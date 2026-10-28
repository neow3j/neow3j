package io.neow3j.compiler;

import io.neow3j.devpack.Storage;

// Compile this file. Do not edit it first.
//
// Java 8 javac produces StringConcatenationContract_StringBuilderNoArgs.class.
// From the repo root, after ./gradlew :devpack:jar:
//
//   mkdir -p /tmp/scc
//   javac -g -cp devpack/build/libs/devpack-3.25.0.jar -d /tmp/scc compiler/src/test/resources/string-concatenation/no-args/StringConcatenationContract.java
//
// Java 9 and later compile + to invokedynamic, so this has to be a Java 8 javac.
// -g is required. Without a LocalVariableTable the manifest parameters have no name, and Neo faults while reading the manifest.
// javap -c -p -v on the class must show a LocalVariableTable, StringBuilder."<init>":()V, and no String.valueOf.
// Copy the class file to compiler/src/test/resources/StringConcatenationContract_StringBuilderNoArgs.class.
//
// Compile this same file again with the Eclipse compiler. Do not edit it, and do not use javac.
// That produces StringConcatenationContract_StringBuilderOneArg.class.
// From the repo root:
//
//   curl -fsSL -o /tmp/ecj.jar https://repo1.maven.org/maven2/org/eclipse/jdt/ecj/3.26.0/ecj-3.26.0.jar
//   mkdir -p /tmp/scc-ecj
//   java -jar /tmp/ecj.jar -g -source 8 -target 8 -cp devpack/build/libs/devpack-3.25.0.jar -d /tmp/scc-ecj compiler/src/test/resources/string-concatenation/no-args/StringConcatenationContract.java
//
// javap -c -p -v /tmp/scc-ecj/io/neow3j/compiler/StringConcatenationContract.class
// must show a LocalVariableTable, String.valueOf, and StringBuilder."<init>":(Ljava/lang/String;)V.
// Copy that class file to:
//   compiler/src/test/resources/StringConcatenationContract_StringBuilderOneArg.class
//   int-tests/src/test-integration/resources/compiler/StringConcatenationContract_StringBuilderOneArg.class

public class StringConcatenationContract {

    public static String getSomething(String key) {
        return Storage.getString(key) + "foo" + get();
    }

    private static String get() {
        return "hello";
    }

    public static void putSomething(String key, String value) {
        Storage.put(key, value);
    }

}
