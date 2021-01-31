package io.neow3j.compiler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import io.neow3j.devpack.annotations.Group;
import io.neow3j.devpack.annotations.Group.Groups;
import io.neow3j.devpack.annotations.Safe;
import io.neow3j.protocol.core.methods.response.ContractManifest.ContractABI.ContractMethod;
import io.neow3j.protocol.core.methods.response.ContractManifest.ContractGroup;
import java.io.IOException;
import java.util.List;
import org.junit.Test;

public class GroupManifestTest {

    @Test
    public void withGroupsAnnotation() throws IOException {
        CompilationUnit unit = new Compiler().compileClass(GroupManifestTestContract.class.getName());
        List<ContractGroup> groups = unit.getManifest().getGroups();
        assertThat(groups, hasSize(2));
        assertThat(groups.get(0).getPubKey(), is("pubKey1"));
        assertThat(groups.get(0).getSignature(), is("signature1"));
        assertThat(groups.get(1).getPubKey(), is("pubKey2"));
        assertThat(groups.get(1).getSignature(), is("signature2"));
    }

    @Test
    public void withGroupsAnnotationSingle() throws IOException {
        CompilationUnit unit = new Compiler().compileClass(GroupManifestTestContractWithSingleAnnotation.class.getName());
        List<ContractGroup> groups = unit.getManifest().getGroups();
        assertThat(groups, hasSize(1));
        assertThat(groups.get(0).getPubKey(), is("pubKey1"));
        assertThat(groups.get(0).getSignature(), is("signature1"));
    }

    @Test
    public void withGroupsAnnotationWrapper() throws IOException {
        CompilationUnit unit = new Compiler().compileClass(GroupManifestTestContractWithGroupsAnnotation.class.getName());
        List<ContractGroup> groups = unit.getManifest().getGroups();
        assertThat(groups, hasSize(2));
        assertThat(groups.get(0).getPubKey(), is("pubKey1"));
        assertThat(groups.get(0).getSignature(), is("signature1"));
        assertThat(groups.get(1).getPubKey(), is("pubKey2"));
        assertThat(groups.get(1).getSignature(), is("signature2"));
    }

    @Test
    public void withoutGroupsAnnotation() throws IOException {
        CompilationUnit unit = new Compiler().compileClass(GroupManifestTestContractWithoutAnnotation.class.getName());
        List<ContractGroup> groups = unit.getManifest().getGroups();
        assertThat(groups, hasSize(0));
    }

    @Group(pubKey = "pubKey1", signature = "signature1")
    @Group(pubKey = "pubKey2", signature = "signature2")
    static class GroupManifestTestContract {

        public static void main() {
        }

    }

    @Group(pubKey = "pubKey1", signature = "signature1")
    static class GroupManifestTestContractWithSingleAnnotation {

        public static void main() {
        }

    }

    @Groups({
            @Group(pubKey = "pubKey1", signature = "signature1"),
            @Group(pubKey = "pubKey2", signature = "signature2")
    })
    static class GroupManifestTestContractWithGroupsAnnotation {

        public static void main() {
        }

    }

    static class GroupManifestTestContractWithoutAnnotation {

        public static void main() {
        }

    }

}
