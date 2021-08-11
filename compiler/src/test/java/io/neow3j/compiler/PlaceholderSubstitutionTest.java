package io.neow3j.compiler;

import io.neow3j.devpack.annotations.Trust;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
}
