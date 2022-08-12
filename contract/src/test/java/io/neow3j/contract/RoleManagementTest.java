package io.neow3j.contract;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.Role;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.script.ScriptBuilder;
import io.neow3j.transaction.TransactionBuilder;
import io.neow3j.types.Hash160;
import io.neow3j.wallet.Account;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static io.neow3j.test.WireMockTestHelper.setUpWireMockForCall;
import static io.neow3j.types.ContractParameter.array;
import static io.neow3j.types.ContractParameter.integer;
import static io.neow3j.types.ContractParameter.publicKey;
import static io.neow3j.utils.Numeric.hexStringToByteArray;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RoleManagementTest {

    private static final Hash160 ROLEMANAGEMENT_HASH =
            new Hash160("49cf4e5378ffcd4dec034fd98a174c5491e395e2");

    private RoleManagement roleManagement;
    private Account account1;

    @RegisterExtension
    static WireMockExtension wireMockExtension = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @BeforeAll
    public void setUp() {
        // Configuring WireMock to use default host and the dynamic port set in WireMockRule.
        int port = wireMockExtension.getPort();
        WireMock.configureFor(port);
        Neow3j neow3j = Neow3j.build(new HttpService("http://127.0.0.1:" + port));
        roleManagement = new RoleManagement(neow3j);

        account1 = new Account(
                ECKeyPair.create(hexStringToByteArray(
                        "0f7d2f77f3229178650b958eb286258f0e6533d0b86ec389b862c440c6511a4b"
                )));
    }

    @Test
    public void testValidateIntegerValueOfRoleByteValue() {
        assertThat((int) Role.STATE_VALIDATOR.byteValue(), is(4));
        assertThat((int) Role.ORACLE.byteValue(), is(8));
        assertThat((int) Role.NEO_FS_ALPHABET_NODE.byteValue(), is(16));
    }

    @Test
    public void testGetDesignateByRole() throws IOException {
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");
        setUpWireMockForCall("invokefunction", "designation_getByRole.json",
                String.valueOf(Role.STATE_VALIDATOR.byteValue()), "10");

        List<ECPublicKey> list =
                roleManagement.getDesignatedByRole(Role.STATE_VALIDATOR, BigInteger.TEN);

        assertThat(list, contains(account1.getECKeyPair().getPublicKey()));
    }

    @Test
    public void testGetDesignatedByRole_emptyResponse() throws IOException {
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");
        setUpWireMockForCall("invokefunction", "designation_getByRole_empty.json",
                String.valueOf(Role.STATE_VALIDATOR.byteValue()), "12");

        List<ECPublicKey> list =
                roleManagement.getDesignatedByRole(Role.ORACLE, new BigInteger("12"));

        assertThat(list, hasSize(0));
    }

    @Test
    public void testGetDesignatedByRole_negativeIndex() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> roleManagement.getDesignatedByRole(Role.ORACLE, new BigInteger("-1")));
        assertThat(thrown.getMessage(), is("The block index has to be positive."));
    }

    @Test
    public void testGetDesignatedByRole_indexTooHigh() throws IOException {
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> roleManagement.getDesignatedByRole(Role.ORACLE, new BigInteger("1001")));
        assertThat(thrown.getMessage(), containsString("The provided block index (1001) is too high."));
    }

    @Test
    public void testDesignateAsRole() throws IOException {
        setUpWireMockForCall("invokefunction", "designation_designateAsRole.json");

        byte[] expectedScript = new ScriptBuilder()
                .contractCall(
                        RoleManagement.SCRIPT_HASH,
                        "designateAsRole",
                        asList(
                                integer(Role.ORACLE.byteValue()),
                                array(publicKey(account1.getECKeyPair().getPublicKey().getEncoded(true)))))
                .toArray();

        ArrayList<ECPublicKey> pubKeys = new ArrayList<>();
        pubKeys.add(account1.getECKeyPair().getPublicKey());
        TransactionBuilder b = roleManagement.designateAsRole(Role.ORACLE, pubKeys);

        assertThat(b.getScript(), is(expectedScript));
    }

    @Test
    public void testDesignate_roleNull() {
        ArrayList<ECPublicKey> pubKeys = new ArrayList<>();
        pubKeys.add(account1.getECKeyPair().getPublicKey());

        IllegalArgumentException thrown =
                assertThrows(IllegalArgumentException.class, () -> roleManagement.designateAsRole(null, pubKeys));
        assertThat(thrown.getMessage(), is("The designation role cannot be null."));
    }

    @Test
    public void testDesignate_pubKeysNull() {
        IllegalArgumentException thrown =
                assertThrows(IllegalArgumentException.class, () -> roleManagement.designateAsRole(Role.ORACLE, null));
        assertThat(thrown.getMessage(), is("At least one public key is required for designation."));
    }

    @Test
    public void testDesignate_pubKeysEmpty() {
        ArrayList<ECPublicKey> pubKeys = new ArrayList<>();

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> roleManagement.designateAsRole(Role.ORACLE, pubKeys));
        assertThat(thrown.getMessage(), is("At least one public key is required for designation."));
    }

    @Test
    public void scriptHash() {
        assertThat(roleManagement.getScriptHash(), is(ROLEMANAGEMENT_HASH));
    }

}
