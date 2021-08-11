package io.neow3j.compiler;

import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.annotations.Trust;
import io.neow3j.wallet.Account;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static io.neow3j.devpack.StringLiteralHelper.addressToScriptHash;
import static org.junit.Assert.assertEquals;

public class PlaceholderSubstitutionTest {

    @Test
    public void testSingleAnnotationReplacement() throws IOException {
        Map<String, String> replaceMap = new HashMap<>();
        replaceMap.put("<INVALID_TRUST_HASH>", "*");
        CompilationUnit res = new Compiler().compile(
                PlaceholderSubstitutionTest.SingleAnnotationReplaceTest.class.getName(),
                replaceMap
        );
        assertEquals(res.getManifest().getTrusts().get(0), "*");
    }

    // Annotations.value = List{key, value}
    @Trust(value = "<INVALID_TRUST_HASH>")
    static class SingleAnnotationReplaceTest {
        public static void main() {
        }
    }

    @Test
    public void testMultiAnnotationReplacement() throws IOException {
        Map<String, String> replaceMap = new HashMap<>();
        replaceMap.put("<INVALID_TRUST_HASH1>", "*");
        replaceMap.put("<INVALID_TRUST_HASH2>", "*");
        CompilationUnit res = new Compiler().compile(
                PlaceholderSubstitutionTest.MultiAnnotationReplaceTest.class.getName(),
                replaceMap
        );
        assertEquals(res.getManifest().getTrusts().get(0), "*");
        assertEquals(res.getManifest().getTrusts().get(1), "*");
    }

    // Annotations.value = List{key, List{trustNode1, trustNode2}}
    @Trust(value = "<INVALID_TRUST_HASH1>")
    @Trust(value = "<INVALID_TRUST_HASH2>")
    static class MultiAnnotationReplaceTest {
        public static void main() {
        }
    }

    @Test
    public void testBytecodeReplacement() throws IOException {
        Map<String, String> replaceMap = new HashMap<>();
        // TODO: This assume that `addressToScriptHash` will cause an exception
        //      when dealing with invalid address, then failed the test.
        //      The best practice should be an integration test which deploy
        //      the modified contract and test it's value, since I cannot setup
        //      the test docker, I won't submit tests I can't run.
        replaceMap.put("<INVALID_HASH>", Account.create().getAddress());

        new Compiler().compile(
                PlaceholderSubstitutionTest.BytecodeReplaceTest.class.getName(),
                replaceMap
        );
    }

    static class BytecodeReplaceTest {
        public static Hash160 contractOwner = addressToScriptHash("<INVALID_HASH>");
    }
}
