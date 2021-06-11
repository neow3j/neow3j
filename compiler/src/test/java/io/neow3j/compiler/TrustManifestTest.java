package io.neow3j.compiler;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;

import io.neow3j.devpack.annotations.Trust;
import io.neow3j.devpack.annotations.Trust.Trusts;
import java.io.IOException;
import java.util.List;
import org.hamcrest.text.StringContainsInOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TrustManifestTest {

    private static final String CONTRACT_HASH_1 = "0x0f46dc4287b70117ce8354924b5cb3a47215ad93";
    private static final String GROUP_PUBKEY_1 =
            "02163946a133e3d2e0d987fb90cb01b060ed1780f1718e2da28edf13b965fd2b60";
    private static final String CONTRACT_HASH_2 = "0xd6c712eb53b1a130f59fd4e5864bdac27458a509";

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void withTrustsAnnotation() throws IOException {
        CompilationUnit unit = new Compiler()
                .compile(TrustManifestTestContract.class.getName());
        List<String> trusts = unit.getManifest().getTrusts();
        assertThat(trusts, hasSize(2));
        assertThat(trusts, hasItems(CONTRACT_HASH_1, CONTRACT_HASH_1));
    }

    @Test
    public void withTrustsAnnotationSingle() throws IOException {
        CompilationUnit unit = new Compiler()
                .compile(TrustManifestTestContractWithSingleAnnotation.class.getName());
        List<String> trusts = unit.getManifest().getTrusts();
        assertThat(trusts, hasSize(1));
        assertThat(trusts, hasItems(CONTRACT_HASH_1));
    }

    @Test
    public void withTrustsAnnotationWrapper() throws IOException {
        CompilationUnit unit = new Compiler()
                .compile(TrustManifestTestContractWithTrustsAnnotation.class.getName());
        List<String> trusts = unit.getManifest().getTrusts();
        assertThat(trusts, hasSize(2));
        assertThat(trusts, hasItems(CONTRACT_HASH_1, GROUP_PUBKEY_1));
    }

    @Test
    public void withoutTrustsAnnotation() throws IOException {
        CompilationUnit unit = new Compiler()
                .compile(TrustManifestTestContractWithoutAnnotation.class.getName());
        List<String> trusts = unit.getManifest().getTrusts();
        assertThat(trusts, hasSize(0));
    }

    @Test
    public void withTrustsAnnotationButNotValidContracHashNorGroupPubKey() throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage(new StringContainsInOrder(asList(
                "Invalid contract hash or public key:", "invalidContractHashOrPubKey")));
        new Compiler()
                .compile(TrustManifestTestContractWithNotValidContractHashNorGroupKey.class
                        .getName());
    }

    @Trust(CONTRACT_HASH_1)
    @Trust(CONTRACT_HASH_2)
    static class TrustManifestTestContract {

        public static void main() {
        }

    }

    @Trust(CONTRACT_HASH_1)
    static class TrustManifestTestContractWithSingleAnnotation {

        public static void main() {
        }

    }

    @Trusts({
            @Trust(CONTRACT_HASH_1),
            @Trust(GROUP_PUBKEY_1),
    })
    static class TrustManifestTestContractWithTrustsAnnotation {

        public static void main() {
        }

    }

    static class TrustManifestTestContractWithoutAnnotation {

        public static void main() {
        }

    }

    @Trust(CONTRACT_HASH_1)
    @Trust("invalidContractHashOrPubKey")
    static class TrustManifestTestContractWithNotValidContractHashNorGroupKey {

        public static void main() {
        }

    }

}
