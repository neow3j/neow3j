package io.neow3j.contract;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.neow3j.contract.ContractParameter.array;
import static io.neow3j.contract.ContractParameter.integer;
import static io.neow3j.contract.ContractParameter.publicKey;
import static io.neow3j.contract.ContractTestHelper.setUpWireMockForCall;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.Role;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.utils.Numeric;
import io.neow3j.wallet.Account;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.hamcrest.core.StringContains;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class RoleManagementTest {

    private static final String ROLEMANAGEMENT_SCRIPTHASH =
            "49cf4e5378ffcd4dec034fd98a174c5491e395e2";
    private static final String DESIGNATE_AS_ROLE = "designateAsRole";

    private Account account1;
    private Neow3j neow;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().dynamicPort());

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        // Configuring WireMock to use default host and the dynamic port set in WireMockRule.
        int port = wireMockRule.port();
        WireMock.configureFor(port);
        neow = Neow3j.build(new HttpService("http://127.0.0.1:" + port));

        account1 = new Account(ECKeyPair.create(Numeric.hexStringToByteArray(
                "0f7d2f77f3229178650b958eb286258f0e6533d0b86ec389b862c440c6511a4b")));
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

        RoleManagement roleManagement = new RoleManagement(neow);
        List<ECPublicKey> list = roleManagement.getDesignatedByRole(Role.STATE_VALIDATOR, 10);
        assertThat(list, contains(account1.getECKeyPair().getPublicKey()));
    }

    @Test
    public void testGetDesignatedByRole_emptyResponse() throws IOException {
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");
        setUpWireMockForCall("invokefunction", "designation_getByRole_empty.json",
                String.valueOf(Role.STATE_VALIDATOR.byteValue()), "12");

        RoleManagement roleManagement = new RoleManagement(neow);
        List<ECPublicKey> list = roleManagement.getDesignatedByRole(Role.ORACLE, 12);
        assertThat(list, hasSize(0));
    }

    @Test
    public void testGetDesignatedByRole_negativeIndex() throws IOException {
        RoleManagement roleManagement = new RoleManagement(neow);
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(new StringContains("The block index has to be positive."));
        roleManagement.getDesignatedByRole(Role.ORACLE, -1);
    }

    @Test
    public void testGetDesignatedByRole_indexTooHigh() throws IOException {
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");

        RoleManagement roleManagement = new RoleManagement(neow);
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(
                new StringContains("The provided block index (1001) is too high."));
        roleManagement.getDesignatedByRole(Role.ORACLE, 1001);
    }

    @Test
    public void testDesignateAsRole() throws IOException {
        setUpWireMockForCall("invokefunction", "designation_designateAsRole.json");

        byte[] expectedScript = new ScriptBuilder()
                .contractCall(RoleManagement.SCRIPT_HASH, DESIGNATE_AS_ROLE,
                        asList(
                                integer(Role.ORACLE.byteValue()),
                                array(publicKey(
                                        account1.getECKeyPair().getPublicKey().getEncoded(true)))))
                .toArray();

        RoleManagement roleManagement = new RoleManagement(neow);
        ArrayList<ECPublicKey> pubKeys = new ArrayList<>();
        pubKeys.add(account1.getECKeyPair().getPublicKey());
        TransactionBuilder b = roleManagement.designateAsRole(Role.ORACLE, pubKeys);

        assertThat(b.getScript(), is(expectedScript));
    }

    @Test
    public void testDesignate_roleNull() {
        RoleManagement roleManagement = new RoleManagement(neow);
        ArrayList<ECPublicKey> pubKeys = new ArrayList<>();
        pubKeys.add(account1.getECKeyPair().getPublicKey());

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(new StringContains("role cannot be null"));
        roleManagement.designateAsRole(null, pubKeys);
    }

    @Test
    public void testDesignate_pubKeysNull() {
        RoleManagement roleManagement = new RoleManagement(neow);

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(
                new StringContains("one public key is required for designation"));
        roleManagement.designateAsRole(Role.ORACLE, null);
    }

    @Test
    public void testDesignate_pubKeysEmpty() {
        RoleManagement roleManagement = new RoleManagement(neow);
        ArrayList<ECPublicKey> pubKeys = new ArrayList<>();

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(
                new StringContains("one public key is required for designation"));
        roleManagement.designateAsRole(Role.ORACLE, pubKeys);
    }

    @Test
    public void scriptHash() {
        assertThat(new RoleManagement(neow).getScriptHash().toString(),
                is(ROLEMANAGEMENT_SCRIPTHASH));
    }

}
