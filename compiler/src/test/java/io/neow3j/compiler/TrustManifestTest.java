package io.neow3j.compiler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;

import io.neow3j.devpack.annotations.Trust;
import io.neow3j.devpack.annotations.Trust.Trusts;
import java.io.IOException;
import java.util.List;
import org.junit.Test;

public class TrustManifestTest {

    @Test
    public void withTrustsAnnotation() throws IOException {
        CompilationUnit unit = new Compiler()
                .compileClass(TrustManifestTestContract.class.getName());
        List<String> trusts = unit.getManifest().getTrusts();
        assertThat(trusts, hasSize(2));
        assertThat(trusts, hasItems("contract1", "contract2"));
    }

    @Test
    public void withTrustsAnnotationSingle() throws IOException {
        CompilationUnit unit = new Compiler()
                .compileClass(TrustManifestTestContractWithSingleAnnotation.class.getName());
        List<String> trusts = unit.getManifest().getTrusts();
        assertThat(trusts, hasSize(1));
        assertThat(trusts, hasItems("contract1"));
    }

    @Test
    public void withTrustsAnnotationWrapper() throws IOException {
        CompilationUnit unit = new Compiler()
                .compileClass(TrustManifestTestContractWithTrustsAnnotation.class.getName());
        List<String> trusts = unit.getManifest().getTrusts();
        assertThat(trusts, hasSize(2));
        assertThat(trusts, hasItems("contract1", "contract2"));
    }

    @Test
    public void withoutTrustsAnnotation() throws IOException {
        CompilationUnit unit = new Compiler()
                .compileClass(TrustManifestTestContractWithoutAnnotation.class.getName());
        List<String> trusts = unit.getManifest().getTrusts();
        assertThat(trusts, hasSize(0));
    }

    @Trust("contract1")
    @Trust("contract2")
    static class TrustManifestTestContract {

        public static void main() {
        }

    }

    @Trust("contract1")
    static class TrustManifestTestContractWithSingleAnnotation {

        public static void main() {
        }

    }

    @Trusts({
            @Trust("contract1"),
            @Trust("contract2"),
    })
    static class TrustManifestTestContractWithTrustsAnnotation {

        public static void main() {
        }

    }

    static class TrustManifestTestContractWithoutAnnotation {

        public static void main() {
        }

    }

}
