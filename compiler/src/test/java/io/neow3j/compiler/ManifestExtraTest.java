package io.neow3j.compiler;

import io.neow3j.devpack.annotations.ManifestExtra;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("unchecked")
public class ManifestExtraTest {

    @Test
    public void singleExtraAnnotation() throws IOException {
        CompilationUnit unit = new Compiler()
                .compile(SingleExtraManifestTestContract.class.getName());
        Object extra = unit.getManifest().getExtra();
        assertTrue(extra instanceof Map);
        Map extras = (Map<String, String>) extra;
        assertThat(extras.size(), is(1));
        assertThat((String) (extras.get("Author")), is("AxLabs"));
    }

    @ManifestExtra(key = "Author", value = "AxLabs")
    static class SingleExtraManifestTestContract {

        public static void main() {
        }

    }

    @Test
    public void multipleExtraAnnotations() throws IOException {
        CompilationUnit unit = new Compiler()
                .compile(MultipleSingleExtraManifestTestContract.class.getName());
        Object extra = unit.getManifest().getExtra();
        assertTrue(extra instanceof Map);
        Map extras = (Map<String, String>) extra;
        assertThat(extras.size(), is(3));
        assertThat((String) (extras.get("k")), is("val"));
        assertThat((String) (extras.get("Cool Cat")), is("neow3j"));
        assertThat((String) (extras.get("author")), is("axlabs"));
    }

    @ManifestExtra(key = "k", value = "val")
    @ManifestExtra(key = "author", value = "axlabs")
    @ManifestExtra(key = "Cool Cat", value = "neow3j")
    static class MultipleSingleExtraManifestTestContract {

        public static void main() {
        }

    }

    @Test
    public void extrasAnnotation() throws IOException {
        CompilationUnit unit = new Compiler()
                .compile(ExtrasManifestTestContract.class.getName());
        Object extra = unit.getManifest().getExtra();
        assertTrue(extra instanceof Map);
        Map extras = (Map<String, String>) extra;
        assertThat(extras.size(), is(4));
        assertThat((String) (extras.get("k")), is("val"));
        assertThat((String) (extras.get("Cool cat")), is("Neoooow3j"));
        assertThat((String) (extras.get("author")), is("axlabs"));
        assertThat((String) (extras.get("key")), is("value"));
    }

    @ManifestExtra.ManifestExtras({
            @ManifestExtra(key = "Cool cat", value = "Neoooow3j"),
            @ManifestExtra(key = "k", value = "val"),
            @ManifestExtra(key = "key", value = "value"),
            @ManifestExtra(key = "author", value = "axlabs")
    })
    static class ExtrasManifestTestContract {

        public static void main() {
        }

    }

}
