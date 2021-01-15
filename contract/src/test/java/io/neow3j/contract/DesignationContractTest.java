package io.neow3j.contract;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.DesignationRole;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.utils.Numeric;
import io.neow3j.wallet.Account;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.hamcrest.core.StringContains;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.neow3j.contract.ContractParameter.array;
import static io.neow3j.contract.ContractParameter.integer;
import static io.neow3j.contract.ContractParameter.publicKey;
import static io.neow3j.contract.ContractTestHelper.setUpWireMockForCall;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

public class DesignationContractTest {

    private static final ScriptHash DESIGNATION_CONTRACT_SCRIPT_HASH = DesignationContract.SCRIPT_HASH;
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
    public void testGetDesignateByRole() throws IOException {
        setUpWireMockForCall("invokefunction", "designation_getByRole.json");

        DesignationContract designationContract = new DesignationContract(neow);
        List<ECPublicKey> list = designationContract.getDesignatedByRole(DesignationRole.STATE_VALIDATOR, 10);
        assertThat(list, contains(account1.getECKeyPair().getPublicKey()));
    }

    @Test
    public void testDesignateAsRole() throws IOException {
        setUpWireMockForCall("invokefunction", "designation_designateAsRole.json");

        byte[] expectedScript = new ScriptBuilder()
                .contractCall(DESIGNATION_CONTRACT_SCRIPT_HASH, DESIGNATE_AS_ROLE,
                        Arrays.asList(
                                integer(DesignationRole.ORACLE.byteValue()),
                                array(publicKey(account1.getECKeyPair().getPublicKey().getEncoded(true)))))
                .toArray();

        DesignationContract designationContract = new DesignationContract(neow);
        ArrayList<ECPublicKey> pubKeys = new ArrayList<>();
        pubKeys.add(account1.getECKeyPair().getPublicKey());
        TransactionBuilder b = designationContract.designateAsRole(DesignationRole.ORACLE, pubKeys);

        assertThat(b.getScript(), is(expectedScript));
    }

    @Test
    public void testDesignate_roleNull() {
        DesignationContract designationContract = new DesignationContract(neow);
        ArrayList<ECPublicKey> pubKeys = new ArrayList<>();
        pubKeys.add(account1.getECKeyPair().getPublicKey());

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(new StringContains("role cannot be null"));
        designationContract.designateAsRole(null, pubKeys);
    }

    @Test
    public void testDesignate_pubKeysNull() {
        DesignationContract designationContract = new DesignationContract(neow);

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(new StringContains("one public key is required for designation"));
        designationContract.designateAsRole(DesignationRole.ORACLE, null);
    }

    @Test
    public void testDesignate_pubKeysEmpty() {
        DesignationContract designationContract = new DesignationContract(neow);
        ArrayList<ECPublicKey> pubKeys = new ArrayList<>();

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(new StringContains("one public key is required for designation"));
        designationContract.designateAsRole(DesignationRole.ORACLE, pubKeys);
    }
}
