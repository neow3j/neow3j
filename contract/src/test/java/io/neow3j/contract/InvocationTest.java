package io.neow3j.contract;

import static io.neow3j.constants.NeoConstants.MAX_VALID_UNTIL_BLOCK_INCREMENT;
import static io.neow3j.contract.ContractTestUtils.GETBLOCKCOUNT_RESPONSE;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.neow3j.contract.Invocation.InvocationBuilder;
import io.neow3j.contract.exceptions.InvocationConfigurationException;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.wallet.Account;
import java.io.IOException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class InvocationTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

    private Neow3j neow;

    @Before
    public void setUp() {
        // Configure WireMock to use default host and port "localhost:8080".
        WireMock.configure();
        neow = Neow3j.build(new HttpService("http://localhost:8080"));
    }

    @Test(expected = InvocationConfigurationException.class)
    public void failWithoutSettingSenderAccount() throws IOException {
        ScriptHash sh = new ScriptHash(ContractTestUtils.CONTRACT_1_SCRIPT_HASH);
        String method = "name";
        InvocationBuilder b = new InvocationBuilder(neow, sh, method);
        b.build();
    }

    @Test
    public void testAutomaticSettingOfValidUntilBlockVariable() throws IOException {
        String method = "name";
        ContractTestUtils.setUpWireMockForGetBlockCount();
        ContractTestUtils.setUpWireMockForInvokeFunction(method);
        ScriptHash sh = new ScriptHash(ContractTestUtils.CONTRACT_1_SCRIPT_HASH);
        Account acc = Account.createAccount();
        Invocation i = new InvocationBuilder(neow, sh, method).withAccount(acc).build();
        assertThat(
                i.getTransaction().getValidUntilBlock(),
                is((long)MAX_VALID_UNTIL_BLOCK_INCREMENT + GETBLOCKCOUNT_RESPONSE)
        );
    }

}