package io.neow3j.compiler;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;

import io.neow3j.devpack.annotations.Group;
import io.neow3j.devpack.annotations.Group.Groups;
import io.neow3j.protocol.core.response.ContractManifest.ContractGroup;
import java.io.IOException;
import java.util.List;
import org.hamcrest.text.StringContainsInOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class GroupManifestTest {

    private static final String PUBKEY1 =
            "02163946a133e3d2e0d987fb90cb01b060ed1780f1718e2da28edf13b965fd2b60";
    private static final String SIGNATURE1 = "bAhbpx1J8eIPLb5+fvDIRQTbX0doilPxQO+QKS"
            + "+3fpgyjTwV73UPrv0qsb6I3ZuQjfCA7xoePl5rU508B7k+7w==";
    private static final String PUBKEY2 =
            "02249425a06b5a1f8e6133fc79afa2c2b8430bf9327297f176761df79e8d8929c5";
    private static final String SIGNATURE2 = "bAhbpx1J8eIPLb5+fvDIRQTbX0doilPxQO+QKS"
            + "+3fpgyjTwV73UPrv0qsb6I3ZuQjfCA7xoePl5rU508B7k+7w==";

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void withGroupsAnnotation() throws IOException {
        CompilationUnit unit = new Compiler()
                .compile(GroupManifestTestContract.class.getName());
        List<ContractGroup> groups = unit.getManifest().getGroups();
        assertThat(groups, hasSize(2));
        assertThat(groups.get(0).getPubKey(), is(PUBKEY1));
        assertThat(groups.get(0).getSignature(), is(SIGNATURE1));
        assertThat(groups.get(1).getPubKey(), is(PUBKEY2));
        assertThat(groups.get(1).getSignature(), is(SIGNATURE2));
    }

    @Test
    public void withGroupsAnnotationSingle() throws IOException {
        CompilationUnit unit = new Compiler()
                .compile(GroupManifestTestContractWithSingleAnnotation.class.getName());
        List<ContractGroup> groups = unit.getManifest().getGroups();
        assertThat(groups, hasSize(1));
        assertThat(groups.get(0).getPubKey(), is(PUBKEY1));
        assertThat(groups.get(0).getSignature(), is(SIGNATURE1));
    }

    @Test
    public void withGroupsAnnotationWrapper() throws IOException {
        CompilationUnit unit = new Compiler()
                .compile(GroupManifestTestContractWithGroupsAnnotation.class.getName());
        List<ContractGroup> groups = unit.getManifest().getGroups();
        assertThat(groups, hasSize(2));
        assertThat(groups.get(0).getPubKey(), is(PUBKEY1));
        assertThat(groups.get(0).getSignature(), is(SIGNATURE1));
        assertThat(groups.get(1).getPubKey(), is(PUBKEY2));
        assertThat(groups.get(1).getSignature(), is(SIGNATURE2));
    }

    @Test
    public void withoutGroupsAnnotation() throws IOException {
        CompilationUnit unit = new Compiler()
                .compile(GroupManifestTestContractWithoutAnnotation.class.getName());
        List<ContractGroup> groups = unit.getManifest().getGroups();
        assertThat(groups, hasSize(0));
    }

    @Test
    public void withGroupsAnnotationButNotValidPubKey() throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage(new StringContainsInOrder(asList(
                "Invalid public key:", "invalidPubKey")));
        new Compiler()
                .compile(GroupManifestTestContractWithNotValidPubKey.class.getName());
    }

    @Test
    public void withGroupsAnnotationButNotValidSignature() throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage(new StringContainsInOrder(asList(
                "Invalid signature:", "invalidSignature12345")));
        new Compiler()
                .compile(GroupManifestTestContractWithNotValidSignature.class.getName());
    }

    @Group(pubKey = PUBKEY1, signature = SIGNATURE1)
    @Group(pubKey = PUBKEY2, signature = SIGNATURE2)
    static class GroupManifestTestContract {

        public static void main() {
        }

    }

    @Group(pubKey = PUBKEY1, signature = SIGNATURE1)
    static class GroupManifestTestContractWithSingleAnnotation {

        public static void main() {
        }

    }

    @Groups({
            @Group(pubKey = PUBKEY1, signature = SIGNATURE1),
            @Group(pubKey = PUBKEY2, signature = SIGNATURE2)
    })
    static class GroupManifestTestContractWithGroupsAnnotation {

        public static void main() {
        }

    }

    static class GroupManifestTestContractWithoutAnnotation {

        public static void main() {
        }

    }

    @Group(pubKey = PUBKEY1, signature = SIGNATURE1)
    @Group(pubKey = "invalidPubKey", signature = SIGNATURE2)
    static class GroupManifestTestContractWithNotValidPubKey {

        public static void main() {
        }

    }

    @Group(pubKey = PUBKEY1, signature = SIGNATURE1)
    @Group(pubKey = PUBKEY2, signature = "invalidSignature12345")
    static class GroupManifestTestContractWithNotValidSignature {

        public static void main() {
        }

    }

}
