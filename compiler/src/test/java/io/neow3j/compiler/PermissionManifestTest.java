package io.neow3j.compiler;

import io.neow3j.contract.GasToken;
import io.neow3j.devpack.annotations.Permission;
import io.neow3j.devpack.annotations.Permission.Permissions;
import io.neow3j.devpack.constants.NativeContract;
import io.neow3j.protocol.ObjectMapperFactory;
import io.neow3j.protocol.core.response.ContractManifest.ContractPermission;
import io.neow3j.types.Hash160;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PermissionManifestTest {

    private static final String CONTRACT_HASH_1 = "0x0f46dc4287b70117ce8354924b5cb3a47215ad93";
    private static final String GROUP_PUBKEY_1 =
            "02163946a133e3d2e0d987fb90cb01b060ed1780f1718e2da28edf13b965fd2b60";
    private static final String CONTRACT_METHOD_1 = "method1";
    private static final String CONTRACT_METHOD_2 = "method2";
    private static final String CONTRACT_HASH_2 = "0xd6c712eb53b1a130f59fd4e5864bdac27458a509";

    @Test
    public void withPermissionsAnnotation() throws IOException {
        CompilationUnit unit = new Compiler()
                .compile(PermissionManifestTestContract.class.getName());
        List<ContractPermission> permissions = unit.getManifest().getPermissions();
        assertThat(permissions, hasSize(3));
        assertThat(permissions.get(0).getContract(), is(CONTRACT_HASH_1));
        assertThat(permissions.get(0).getMethods(), hasSize(1));
        assertThat(permissions.get(0).getMethods().get(0), is("*"));
        assertThat(permissions.get(1).getContract(), is(CONTRACT_HASH_2));
        assertThat(permissions.get(1).getMethods().get(0), is(CONTRACT_METHOD_1));
        assertThat(permissions.get(1).getMethods().get(1), is(CONTRACT_METHOD_2));
        assertThat(new Hash160(permissions.get(2).getContract()), is(GasToken.SCRIPT_HASH));
        assertThat(permissions.get(2).getMethods(), hasSize(1));
        assertThat(permissions.get(2).getMethods().get(0), is("*"));
    }

    @Test
    public void withPermissionsAnnotationSingleContractHash() throws IOException {
        CompilationUnit unit = new Compiler()
                .compile(PermissionManifestTestContractWithSingleAnnotationContractHash.class
                        .getName());
        List<ContractPermission> permissions = unit.getManifest().getPermissions();
        assertThat(permissions, hasSize(1));
        assertThat(permissions.get(0).getContract(), is(CONTRACT_HASH_1));
        assertThat(permissions.get(0).getMethods(), hasSize(2));
        assertThat(permissions.get(0).getMethods().get(0), is(CONTRACT_METHOD_1));
        assertThat(permissions.get(0).getMethods().get(1), is(CONTRACT_METHOD_2));
    }

    @Test
    public void withPermissionsAnnotationSingleGroupPubKey() throws IOException {
        CompilationUnit unit = new Compiler()
                .compile(PermissionManifestTestContractWithSingleAnnotationGroupPubKey.class
                        .getName());
        List<ContractPermission> permissions = unit.getManifest().getPermissions();
        assertThat(permissions, hasSize(1));
        assertThat(permissions.get(0).getContract(), is(GROUP_PUBKEY_1));
        assertThat(permissions.get(0).getMethods(), hasSize(2));
        assertThat(permissions.get(0).getMethods().get(0), is(CONTRACT_METHOD_1));
        assertThat(permissions.get(0).getMethods().get(1), is(CONTRACT_METHOD_2));
    }

    @Test
    public void withPermissionsAnnotationSingleContractHashAndSingleMethod() throws IOException {
        CompilationUnit unit = new Compiler().compile(
                PermissionManifestTestContractWithSingleAnnotationContractHashAndSingleMethod.class
                        .getName());
        List<ContractPermission> permissions = unit.getManifest().getPermissions();
        assertThat(permissions, hasSize(1));
        assertThat(permissions.get(0).getContract(), is(CONTRACT_HASH_1));
        assertThat(permissions.get(0).getMethods(), hasSize(1));
        assertThat(permissions.get(0).getMethods().get(0), is(CONTRACT_METHOD_1));
    }


    @Test
    public void withPermissionsAnnotationWrapper() throws IOException {
        CompilationUnit unit = new Compiler()
                .compile(
                        PermissionManifestTestContractWithPermissionsAnnotation.class.getName());
        List<ContractPermission> permissions = unit.getManifest().getPermissions();
        assertThat(permissions, hasSize(2));
        assertThat(permissions.get(0).getContract(), is(CONTRACT_HASH_1));
        assertThat(permissions.get(0).getMethods(), hasSize(1));
        assertThat(permissions.get(0).getMethods().get(0), is("*"));
        assertThat(permissions.get(1).getContract(), is(CONTRACT_HASH_2));
        assertThat(permissions.get(1).getMethods().get(0), is(CONTRACT_METHOD_1));
        assertThat(permissions.get(1).getMethods().get(1), is(CONTRACT_METHOD_2));
    }

    @Test
    public void withoutPermissionsAnnotation() throws IOException {
        CompilationUnit unit = new Compiler()
                .compile(PermissionManifestTestContractWithoutAnnotation.class.getName());
        List<ContractPermission> permissions = unit.getManifest().getPermissions();
        assertThat(permissions, hasSize(0));
        String s = ObjectMapperFactory.getObjectMapper().writeValueAsString(unit.getManifest());
        assertThat(s, containsString("\"permissions\":[]"));
    }

    @Test
    public void withPermissionsAnnotationButNotValidContract() {
        CompilerException thrown = assertThrows(CompilerException.class,
                () -> new Compiler().compile(
                        PermissionManifestTestContractWithNotValidContractHashNorGroupKey.class.getName())
        );
        assertThat(thrown.getMessage(), stringContainsInOrder(asList(
                "Invalid contract hash or public key:", "invalidContractHashOrPubKey")));
    }

    @Test
    public void withWildcardPermissionAnnotation() throws IOException {
        CompilationUnit unit = new Compiler()
                .compile(PermissionManifestWithWildCardTestContract.class.getName());
        List<ContractPermission> permissions = unit.getManifest().getPermissions();
        assertThat(permissions, hasSize(1));
        assertThat(permissions.get(0).getContract(), is("*"));
        assertThat(permissions.get(0).getMethods().get(0), is("*"));
    }

    @Test
    public void withBothContractAndNativeContract() {
        CompilerException thrown = assertThrows(CompilerException.class, () -> new Compiler().compile(
                PermissionManifestTestContractWithContractAndNativeContract.class.getName()));
        assertThat(thrown.getMessage(),
                containsString("must either have the attribute 'contract' or 'nativeContract' set but not both"));
    }

    @Test
    public void withoutBothContractAndNativeContract() {
        CompilerException thrown = assertThrows(CompilerException.class,
                () -> new Compiler().compile(PermissionManifestTestContractWithoutContract.class.getName()));
        assertThat(thrown.getMessage(),
                containsString("must either have the attribute 'contract' or 'nativeContract' set but not both"));
    }

    @Test
    public void withNoneNativeContractValue() {
        CompilerException thrown = assertThrows(CompilerException.class, () -> new Compiler().compile(
                PermissionManifestTestContractWithNoneNativeContractValue.class.getName()));
        assertThat(thrown.getMessage(), containsString("The provided native contract does not exist."));
    }

    @Test
    public void nativeContractFromHash() {
        NativeContract nativeContract = NativeContract.valueOf(GasToken.SCRIPT_HASH);
        assertThat(nativeContract, is(NativeContract.GasToken));
    }

    @Test
    public void invalidNativeContract() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> NativeContract.valueOf(new Hash160("6a3828e0378f9f331c69476f016fe91f5bba8dbd")));
        assertThat(thrown.getMessage(), containsString("There exists no native contract with the provided hash"));
    }

    @Permission(contract = CONTRACT_HASH_1)
    @Permission(contract = CONTRACT_HASH_2, methods = {CONTRACT_METHOD_1, CONTRACT_METHOD_2})
    @Permission(nativeContract = NativeContract.GasToken)
    static class PermissionManifestTestContract {

        public static void main() {
        }

    }

    @Permission(contract = CONTRACT_HASH_1, methods = {CONTRACT_METHOD_1, CONTRACT_METHOD_2})
    static class PermissionManifestTestContractWithSingleAnnotationContractHash {

        public static void main() {
        }

    }

    @Permission(contract = GROUP_PUBKEY_1, methods = {CONTRACT_METHOD_1, CONTRACT_METHOD_2})
    static class PermissionManifestTestContractWithSingleAnnotationGroupPubKey {

        public static void main() {
        }

    }

    @Permission(contract = CONTRACT_HASH_1, methods = CONTRACT_METHOD_1)
    static class PermissionManifestTestContractWithSingleAnnotationContractHashAndSingleMethod {

        public static void main() {
        }

    }

    @Permissions({
            @Permission(contract = CONTRACT_HASH_1),
            @Permission(contract = CONTRACT_HASH_2,
                        methods = {CONTRACT_METHOD_1, CONTRACT_METHOD_2})
    })
    static class PermissionManifestTestContractWithPermissionsAnnotation {

        public static void main() {
        }

    }

    static class PermissionManifestTestContractWithoutAnnotation {

        public static void main() {
        }

    }

    @Permission(contract = CONTRACT_HASH_1)
    @Permission(contract = "invalidContractHashOrPubKey",
                methods = {CONTRACT_METHOD_1, CONTRACT_METHOD_2})
    static class PermissionManifestTestContractWithNotValidContractHashNorGroupKey {

        public static void main() {
        }

    }

    @Permission(contract = "*", methods = "*")
    static class PermissionManifestWithWildCardTestContract {

        public static void main() {
        }

    }

    @Permission(contract = CONTRACT_HASH_1, nativeContract = NativeContract.ContractManagement)
    static class PermissionManifestTestContractWithContractAndNativeContract {

        public static void main() {
        }

    }

    @Permission(methods = "transfer")
    static class PermissionManifestTestContractWithoutContract {

        public static void main() {
        }

    }

    @Permission(nativeContract = NativeContract.None)
    static class PermissionManifestTestContractWithNoneNativeContractValue {

        public static void main() {
        }

    }

}
