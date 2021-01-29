package io.neow3j.compiler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;

import io.neow3j.devpack.annotations.Permission;
import io.neow3j.devpack.annotations.Permission.Permissions;
import io.neow3j.protocol.core.methods.response.ContractManifest.ContractPermission;
import java.io.IOException;
import java.util.List;
import org.junit.Test;

public class PermissionManifestTest {

    @Test
    public void withPermissionsAnnotation() throws IOException {
        CompilationUnit unit = new Compiler()
                .compileClass(PermissionManifestTestContract.class.getName());
        List<ContractPermission> permissions = unit.getManifest().getPermissions();
        assertThat(permissions, hasSize(2));
        assertThat(permissions.get(0).getContract(), is("contract1"));
        assertThat(permissions.get(0).getMethods(), hasSize(1));
        assertThat(permissions.get(0).getMethods().get(0), is("*"));
        assertThat(permissions.get(1).getContract(), is("contract2"));
        assertThat(permissions.get(1).getMethods().get(0), is("method1"));
        assertThat(permissions.get(1).getMethods().get(1), is("method2"));
    }

    @Test
    public void withPermissionsAnnotationWrapper() throws IOException {
        CompilationUnit unit = new Compiler()
                .compileClass(
                        PermissionManifestTestContractWithPermissionsAnnotation.class.getName());
        List<ContractPermission> permissions = unit.getManifest().getPermissions();
        assertThat(permissions, hasSize(2));
        assertThat(permissions.get(0).getContract(), is("contract1"));
        assertThat(permissions.get(0).getMethods(), hasSize(1));
        assertThat(permissions.get(0).getMethods().get(0), is("*"));
        assertThat(permissions.get(1).getContract(), is("contract2"));
        assertThat(permissions.get(1).getMethods().get(0), is("method1"));
        assertThat(permissions.get(1).getMethods().get(1), is("method2"));
    }

    @Test
    public void withoutPermissionsAnnotation() throws IOException {
        CompilationUnit unit = new Compiler()
                .compileClass(PermissionManifestTestContractWithoutAnnotation.class.getName());
        List<ContractPermission> permissions = unit.getManifest().getPermissions();
        assertThat(permissions, hasSize(1));
        assertThat(permissions.get(0).getContract(), is("*"));
        assertThat(permissions.get(0).getMethods(), hasSize(1));
        assertThat(permissions.get(0).getMethods().get(0), is("*"));
    }

    @Permission(contract = "contract1")
    @Permission(contract = "contract2", methods = {"method1", "method2"})
    static class PermissionManifestTestContract {

        public static void main() {
        }

    }

    @Permissions({
            @Permission(contract = "contract1"),
            @Permission(contract = "contract2", methods = {"method1", "method2"})
    })
    static class PermissionManifestTestContractWithPermissionsAnnotation {

        public static void main() {
        }

    }

    static class PermissionManifestTestContractWithoutAnnotation {

        public static void main() {
        }

    }


}
