package io.neow3j.contract;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.neow3j.constants.InteropServiceCode;
import static io.neow3j.contract.ContractTestHelper.setUpWireMockForInvokeFunction;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.http.HttpService;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

public class PolicyContractTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

    private Neow3j neow3j;

    @Before
    public void setUp() {
        // Configuring WireMock to use default host and port "localhost:8080".
        WireMock.configure();
        neow3j = Neow3j.build(new HttpService("http://localhost:8080"));
    }

    @Test
    public void getMaxTransactionsPerBlock() throws IOException {
        setUpWireMockForInvokeFunction("getMaxTransactionsPerBlock",
                "policy_getMaxTxPerBlock.json");
        PolicyContract policyContract = new PolicyContract(this.neow3j);
        assertThat(policyContract.getMaxTransactionsPerBlock(), is(512));
    }

    @Test
    public void getFeePerByte() throws IOException {
        setUpWireMockForInvokeFunction("getFeePerByte", "policy_getFeePerByte.json");
        PolicyContract policyContract = new PolicyContract(this.neow3j);
        assertThat(policyContract.getFeePerByte(), is(1000));
    }

    @Test
    public void getBlockedAccounts_emptyList() throws IOException {
        setUpWireMockForInvokeFunction("getBlockedAccounts",
                "policy_getBlockedAccounts_empty.json");
        PolicyContract policyContract = new PolicyContract(this.neow3j);
        assertNotNull(policyContract.getBlockedAccounts());
        assertThat(policyContract.getBlockedAccounts().size(), is(0));
    }
}
