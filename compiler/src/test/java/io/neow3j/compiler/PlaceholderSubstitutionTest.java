package io.neow3j.compiler;

import io.neow3j.devpack.annotations.Permission;
import io.neow3j.devpack.annotations.Trust;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    @Trust(contract = "<INVALID_TRUST_HASH>")
    static class SingleAnnotationReplaceTest {
        public static void main() {
        }
    }

    @Test
    public void testMultiAnnotationReplacement() throws IOException {
        Map<String, String> replaceMap = new HashMap<>();
        replaceMap.put("<INVALID_TRUST_HASH1>", "*");
        replaceMap.put("<INVALID_TRUST_HASH2>", "*");
        replaceMap.put("<PERMISSION_HASH>", "*");
        replaceMap.put("<PERMISSION_METHOD>", "*");
        CompilationUnit res = new Compiler().compile(
                PlaceholderSubstitutionTest.MultiAnnotationReplaceTest.class.getName(),
                replaceMap
        );
        assertEquals(res.getManifest().getTrusts().get(0), "*");
        assertEquals(res.getManifest().getTrusts().get(1), "*");
        assertEquals(res.getManifest().getPermissions().get(0).getContract(), "*");
        assertEquals(res.getManifest().getPermissions().get(0).getMethods().get(0), "*");
    }

    // Annotations.value = List{key, List{trustNode1, trustNode2}}
    @Trust(contract = "<INVALID_TRUST_HASH1>")
    @Trust(contract = "<INVALID_TRUST_HASH2>")
    @Permission(contract = "<PERMISSION_HASH>", methods = "<PERMISSION_METHOD>")
    static class MultiAnnotationReplaceTest {
        public static void main() {
        }
    }

    @Test
    public void testAnnotationReplacement() throws IOException {
        Map<String, String> replaceMap = new HashMap<>();
        replaceMap.put("<PERMISSION_HASH>", "*");
        CompilationUnit res = new Compiler().compile(
                AnnotationReplaceTest.class.getName(),
                replaceMap
        );
        assertEquals(res.getManifest().getPermissions().get(0).getContract(), "*");
    }

    @Permission(contract = "<PERMISSION_HASH>")
    static class AnnotationReplaceTest {
        public static void main() {
        }
    }

}
